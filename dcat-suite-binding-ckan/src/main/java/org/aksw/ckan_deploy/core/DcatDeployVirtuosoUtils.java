package org.aksw.ckan_deploy.core;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import org.aksw.dcat.jena.domain.api.DcatDataset;
import org.aksw.dcat.jena.domain.api.DcatDistribution;
import org.aksw.dcat.repo.api.DatasetResolver;
import org.aksw.jena_sparql_api.conjure.entity.utils.PathCoder;
import org.aksw.jena_sparql_api.ext.virtuoso.VirtuosoBulkLoad;
import org.aksw.jena_sparql_api.rdf.collections.ResourceUtils;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.vocabulary.DCAT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.LogStream;
import com.spotify.docker.client.messages.ExecCreation;

public class DcatDeployVirtuosoUtils {
    private static final Logger logger = LoggerFactory.getLogger(DcatDeployVirtuosoUtils.class);



    /**
     * This is a compound transformation doing:
     *
     * TODO Create beans so the transformation parameters can be adjusted
     *
     * - Apply mapping of geosparql to virtuoso
     * - Filter long literals
     * - Discard invalid geometries
     *
     * @param triple
     * @return
     */
    public static Optional<Triple> transformForVirtuoso(Triple triple) {
        return null;
    }

    public static final Property dcatDefaultGraphGroup = ResourceFactory.createProperty(DCAT.NS + "defaultGraphGroup");
    public static final Property dcatDefaultGraph = ResourceFactory.createProperty(DCAT.NS + "defaultGraph");

    // Used by dataid
    public static final Property sdDefaultGraph = ResourceFactory.createProperty("http://www.w3.org/ns/sparql-service-description#defaultGraph");


    public static final Set<Property> defaultGraphGroupProperties = new LinkedHashSet<>(Arrays.asList(dcatDefaultGraphGroup));
    public static final Set<Property> defaultGraphProperties = new LinkedHashSet<>(Arrays.asList(dcatDefaultGraph, sdDefaultGraph));


    public static Optional<String> findUri(Resource r, Collection<Property> searchProperties) {

        Optional<String> result = searchProperties.stream().flatMap(p -> ResourceUtils.listPropertyValues(r, p).toSet().stream())
            .filter(RDFNode::isURIResource)
            .map(RDFNode::asResource)
            .map(Resource::getURI)
            .findFirst();

        return result;
    }

    public static void deploy(
            //DcatRepository dcatRepository,
            //DcatDataset dcatDataset,
            DatasetResolver dr,
            Function<String, String> iriResolver,
            //IRIResolver iriResolver,
            DockerClient dockerClient,
            String dockerContainerId,
            Path unzipFolder,
            Path allowedFolder,
            boolean noSymlink,
            Connection conn) throws SQLException, IOException, URISyntaxException {

        PackerRegistry packerRegistry = PackerRegistry.createDefault();

        //Set<Path> toDelete = new HashSet<>();

        DcatDataset dcatDataset = dr.getDataset();

        if(unzipFolder == null) {
            if(dockerContainerId == null) {
                unzipFolder = allowedFolder;
            }
        }

        // Create a sub-folder in the unzip folder for bulk-copying into the container

        // TODO Should we ensure a fresh folder? Probably...
        if(dockerContainerId != null) {
            unzipFolder = unzipFolder.resolve(dockerContainerId);
//			toDelete.add(unzipFolder);
        }

        if(unzipFolder != null) {
            Files.createDirectories(unzipFolder);
        }

        String datasetDefaultGraph = DcatCkanRdfUtils.getUri(dcatDataset, dcatDefaultGraph).orElse(null);

        String defaultGraphGroupIri = DcatCkanRdfUtils.getUri(dcatDataset, dcatDefaultGraphGroup).orElse(null);

        // If both are specified, the graph group takes precedence by default - the interpretation is:
        // If possible, create the group group, but should this be not supported, use the given graph instead

        // Issue what if dataset and distribution specify a defaultGraph?
        // Right now the dataset level default graph takes precedence


        Multimap<Path, String> fileToGraph = LinkedHashMultimap.create();

        try {
            for(DcatDistribution dcatDistribution : dcatDataset.getDistributions2()) {

                // If there is a group group, but the dataset does not declare a default graph, it is skipped
                String distributionGraphIri = DcatCkanRdfUtils.getUri(dcatDistribution, dcatDefaultGraph).orElse(null);

                String effectiveGraphIri = defaultGraphGroupIri != null
                        // If there is a graph group, we enforce the graph on the distribution
                        ? distributionGraphIri
                        // Otherwise, use the dataset graph first, and fallback to the one on the distribution
                        : Optional.ofNullable(datasetDefaultGraph).orElse(distributionGraphIri)
                        ;



                if(effectiveGraphIri != null) {
                    Collection<Path> dataUris;
                    try {
                        dataUris = dr.resolveDistributions()//dcatDistribution.asNode().toString())
                                .flatMap(distributionResolver -> distributionResolver.resolveDownload().toFlowable())
                                .map(URL::toURI)
                                .map(Paths::get)
                                //.map(DistributionResolver::getPath)
//								.map(xxx -> {
//									Path p = xxx.getPath();
//									System.out.println(p);
//									return p;
//								})
                                //.map(xxx -> xxx.getDistribution().getDownloadURL())
                                //.map(DcatCkanDeployUtils::newURI)
                                .toList()
                                .blockingGet()
                                ;

                        System.out.println(dataUris);
//								.stream()
//								.map(DcatDistribution::getDownloadURL)
//								.map(DcatCkanDeployUtils::newURI)
//								.collect(Collectors.toSet());
                        //dataUris = dcatRepository.resolveDistribution(dcatDistribution, iriResolver);
                    } catch(Exception e) {
                        logger.warn("Error resolving distribution" + dcatDistribution, e);
                        // TODO Require permissive=true flag to proceed
                        continue;
                    }

//					String downloadURL = ResourceUtils.getPropertyValue(dcatDistribution, DCAT.downloadURL)
//							.filter(RDFNode::isURIResource)
//							.map(RDFNode::asResource)
//							.map(Resource::getURI)
//							.orElse(null);

                    for(Path dataUri : dataUris) {

                        //String url = iriResolver.resolveToStringSilent(downloadURL);
                        Path path = dataUri;//Paths.get(dataUri);


                        String contentType = Files.probeContentType(path);

                        logger.info("Content type " + contentType + " detected on " + path.toAbsolutePath());

                        String filename = path.getFileName().toString();

                        Path actualFile;
                        //"application/x-partial-download"
                        if("application/x-bzip".equals(contentType)) {

                            PathCoder unzipper = packerRegistry.getMap().get(contentType);

                            String unzippedFilename = com.google.common.io.Files.getNameWithoutExtension(filename);

                            actualFile = unzipFolder.resolve(".tmp-load-" + unzippedFilename);

                            if(!Files.exists(actualFile)) {
                                Path tmpFile = unzipFolder.resolve(".tmp-unzip-" + unzippedFilename);
                                Files.deleteIfExists(tmpFile);

                                logger.info("bzip archive detected, unzipping to " + tmpFile.toAbsolutePath());

                                try {
                                    unzipper.decode(path, tmpFile);
                                } catch(Exception e) {
                                    logger.warn("Failed to unzip " + path, e);
                                }

                                Files.move(tmpFile, actualFile);
                            }

                            fileToGraph.put(actualFile, effectiveGraphIri);
                            //toDelete.add(actualFile);
                        } else {
                            actualFile = allowedFolder.resolve(".tmp-load-" + filename);

                            if(!Files.exists(actualFile)) {
                                if(noSymlink) {
                                    Files.copy(actualFile, path);
                                } else {
                                    Files.createSymbolicLink(actualFile, path);
                                }
                            }

                            fileToGraph.put(actualFile, effectiveGraphIri);
                            //toDelete.add(actualFile);
                        }
                    }

                }
            }



            if(dockerContainerId != null) {
                try {
                    logger.info("Docker container [" + dockerContainerId + "]: Copying folder");
                    logger.info("  Source path on host     : " + unzipFolder);
                    logger.info("  Target path in container: " + allowedFolder);
                    //Path tgtPath = allowedFolder.resolve(actualFile.getFileName());
                    //dockerClient.copyToContainer(actualFile.getParent(), dockerContainerId, tgtPath.toString());
                    dockerClient.copyToContainer(unzipFolder, dockerContainerId, allowedFolder.toString());
                } catch(Exception e) {
                    throw new RuntimeException(e);
                }
            }


            if(defaultGraphGroupIri != null) {
                logger.info("Creating graph group <" + defaultGraphGroupIri + ">");
                VirtuosoBulkLoad.graphGroupCreate(conn, defaultGraphGroupIri, 1);

            }


            for(Entry<Path, String> e : fileToGraph.entries()) {

                Path actualFile = e.getKey();
                String effectiveGraphIri = e.getValue();
                //actualFile : toDelete


                logger.info("Preparing Virtuoso Bulk load:\n  File: " + actualFile.toAbsolutePath() + "\n  Graph: " + effectiveGraphIri + "\n");

                //logger.info("Registering " + actualFile.toAbsolutePath() + " with Virtuoso RDF Loader");

                String allowedDir = allowedFolder.toString();
                String actualFilename = actualFile.getFileName().toString();

                VirtuosoBulkLoad.ldDir(conn, allowedDir, actualFilename, effectiveGraphIri);
            }

            logger.info("Virtuoso RDF Loader started ...");
            VirtuosoBulkLoad.rdfLoaderRun(conn);
            logger.info("Virtuoso RDF Loader finished.");
        }
        finally {
            if(dockerContainerId != null) {
                for(Path path : fileToGraph.keySet()) {

                    Path actualFilename = path.getFileName();
                    Path fileInContainer = allowedFolder.resolve(actualFilename);


                    String execOutput = "";
                    try {
                        logger.info("Docker container [" + dockerContainerId + "]: Removing file " + fileInContainer);
                        String[] command = {"rm", escapeCliArg("" + fileInContainer)};
                        ExecCreation execCreation = dockerClient.execCreate(
                            dockerContainerId, command, DockerClient.ExecCreateParam.attachStdout(),
                        DockerClient.ExecCreateParam.attachStderr());
                        LogStream output = dockerClient.execStart(execCreation.id());
                        execOutput = output.readFully();
                    } catch(Exception f) {
                        logger.warn("Could not remove file inside container: " + fileInContainer + "\n" + execOutput, f);
                    }
                }
            }

            for(Path path : fileToGraph.keySet()) { //toDelete) {
                try {
                    if(Files.exists(path)) {
                        logger.info("Deleting file " + path.toAbsolutePath());
                        Files.delete(path);
                    }
                } catch(IOException e) {
                    logger.warn("Failed to clean up file " + path.toAbsolutePath());
                }
            }
        }
    }

    public static String escapeCliArg(String arg) {
        String result = "'" + arg == null ? "" : arg.replace("'", "\\'") + "'";
        return result;
    }
}
