package org.aksw.dcat_suite.cli.cmd.file;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.aksw.commons.collections.IterableUtils;
import org.aksw.commons.io.util.FileUtils;
import org.aksw.commons.util.entity.EntityInfo;
import org.aksw.commons.util.string.FileNameUtils;
import org.aksw.dcat.jena.conf.api.DcatRepoConfig;
import org.aksw.dcat.jena.domain.api.DcatDataset;
import org.aksw.dcat.jena.domain.api.DcatDistribution;
import org.aksw.dcat.jena.domain.api.DcatDownloadUrl;
import org.aksw.dcat.jena.domain.api.DcatIdType;
import org.aksw.dcat.mgmt.api.DataProject;
import org.aksw.dcat.mgmt.vocab.DCATX;
import org.aksw.difs.system.domain.StoreDefinition;
import org.aksw.jena_sparql_api.http.domain.api.RdfEntityInfo;
import org.aksw.jenax.arq.dataset.api.ResourceInDataset;
import org.aksw.jenax.arq.dataset.impl.ResourceInDatasetImpl;
import org.aksw.jenax.arq.util.streamrdf.StreamRDFWriterEx;
import org.aksw.jenax.sparql.query.rx.RDFDataMgrEx;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.util.ResourceUtils;
import org.apache.jena.util.iterator.WrappedIterator;
import org.apache.jena.vocabulary.DCAT;
import org.apache.jena.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

public class DcatRepoLocalUtils {

    private static final Logger logger = LoggerFactory.getLogger(DcatRepoLocalUtils.class);

    public static final String DEFAULT_REPO_CONF_FILENAME = "dcat.repo.conf.ttl";


    public static List<DataProject> listDataProjects(Model model) {
        return model.listResourcesWithProperty(RDF.type, DCATX.DataProject)
                .mapWith(r -> r.as(DataProject.class))
                .toList();

//        List<ResourceInDataset> tmp = WrappedIterator.create(
//                dataset.asDatasetGraph().find(Node.ANY, Node.ANY, RDF.type.asNode(), DcatXVocab.DataProject.asNode()))
//        .mapWith(quad -> (ResourceInDataset)new ResourceInDatasetImpl(dataset, quad.getGraph().getURI(), quad.getSubject()))
//        .toList();
    }

    public static Path normalizeRelPath(Path basePath, String relPath) {
        return normalizeRelPath(basePath, Path.of(relPath));
    }

    public static Path normalizeRelPath(Path basePath, Path relPath) {
        Path absPath = basePath.resolve(relPath).normalize();
        Path result = basePath.relativize(absPath);
        return result;
    }

    /**
     * Find the graph that has the same graph as the file.
     *
     * @param repo
     * @param relPath
     * @return
     */
    public static ResourceInDataset getFileStatus(DcatRepoLocal repo, Path relPath) {
        relPath = normalizeRelPath(repo.getBasePath(), relPath);
        Node pathIri = NodeFactory.createURI(relPath.toString());

        Dataset dataset = repo.getDataset();

        List<ResourceInDataset> tmp = WrappedIterator.create(
                dataset.asDatasetGraph().find(pathIri, Node.ANY, DCAT.downloadURL.asNode(), pathIri))
        .mapWith(quad -> (ResourceInDataset)new ResourceInDatasetImpl(dataset, quad.getGraph().getURI(), quad.getSubject()))
        .toList();


        return IterableUtils.expectZeroOrOneItems(tmp);
    }

    /**
     *
     *
     * @param basePath The base path (will be made absolute) against which to resolve relPath
     * @param relPath The path to the file which also becomes the IRI of a graph
     * @return
     */
    public static RdfEntityInfo probeFile(Path basePath, Path relPath) {

        Path tgtPath = basePath.toAbsolutePath().resolve(relPath);

        RdfEntityInfo info;
        try (InputStream in = Files.newInputStream(tgtPath)) {
            List<Lang> PROBE_LANGS = Collections.unmodifiableList(Arrays.asList(
                    RDFLanguages.NTRIPLES,
                    RDFLanguages.TURTLE,
                    RDFLanguages.NQUADS,
                    RDFLanguages.TRIG,
                    RDFLanguages.JSONLD,
                    RDFLanguages.RDFXML,
                    RDFLanguages.RDFTHRIFT
                    // RDFLanguages.TRIX
            ));
            info = RDFDataMgrEx.probeEntityInfo(in, PROBE_LANGS);
        } catch (IOException e1) {
            throw new RuntimeException(e1);
        }


        RdfEntityInfo result = ResourceUtils.renameResource(info, relPath.toString()).as(RdfEntityInfo.class);

        try {
            long size = Files.size(tgtPath);
            result.setByteSize(size);
        } catch (IOException e) {
            logger.warn("Failed to obtain file size for " + tgtPath);
        }



        return result;
    }

    public static Path findDcatRepoConfig(Path path) {
        return FileUtils.findInAncestors(path.toAbsolutePath(), DEFAULT_REPO_CONF_FILENAME);
    }

    /** Test for whether the given path holds a repository */
    public static boolean isRepository(Path path) {
        Path p = path.resolve(DEFAULT_REPO_CONF_FILENAME);
        boolean result = Files.exists(p);
        return result;
    }

    /** Find a repo in the current working directory or the first suitable ancestor */
    public static DcatRepoLocal findLocalRepo() {
        return findLocalRepo(Path.of(""));
    }

    /** Find a repo in the given folder or the first suitable ancestor */
    public static DcatRepoLocal findLocalRepo(Path searchStartPath) {
        Path configFile = findDcatRepoConfig(searchStartPath);
        return loadLocalRepo(configFile);
    }

    /** Look for the default repo config file in a specific folder */
    public static DcatRepoLocal getLocalRepo(Path folder) {
        Path configFile = folder.resolve(DEFAULT_REPO_CONF_FILENAME);
        return loadLocalRepo(configFile);
    }


    /** Create a repo object from a specific config file */
    public static DcatRepoLocal loadLocalRepo(Path configFile) {

        if (configFile == null) {
            throw new RuntimeException("No local dcat repository detected in any parent folders");
        }

        Path repoRootFolder = configFile.getParent();
        logger.info("Using dcat repository from " + configFile);

        DcatRepoLocal result;

        try {
            result = new DcatRepoLocalImpl(configFile, repoRootFolder, null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return result;
    }

    public static void init() throws Exception {
        init(Path.of("").toAbsolutePath());
    }

    public static void init(Path currentFolder) throws Exception {
        // Path path = StandardSystemProperty.USER_DIR.
        // Path currentFolder = Path.of("").toAbsolutePath();

        Path conf = findDcatRepoConfig(currentFolder);
        if (conf != null) {
            if (currentFolder.equals(conf.getParent())) {
                throw new RuntimeException("Configuration already exists in this folder: " + conf);
            } else {
                throw new RuntimeException("A file already exists (TODO force flag not yet implemented) at " + conf);
            }
        } else {
            conf = currentFolder.resolve(DEFAULT_REPO_CONF_FILENAME);
        }

        // Path confAbsPath = conf.toAbsolutePath();
        logger.info("Writing to config to  " + conf);

        Model repoConfModel = ModelFactory.createDefaultModel();
        DcatRepoConfig repoConf = repoConfModel.createResource(DEFAULT_REPO_CONF_FILENAME).as(DcatRepoConfig.class);
        repoConf.addProperty(RDF.type, DcatRepoLocalImpl.RepoConfig);
        repoConf.setEngine("difs");


        StoreDefinition storeConf = repoConfModel.createResource().as(StoreDefinition.class)
                .setSingleFile(true)
                .setStorePath("dcat.ttl");

        repoConf.setEngineConf(storeConf);


        try (OutputStream out = Files.newOutputStream(conf)) {
            StreamRDFWriterEx.writeAsGiven(
                    repoConf.getModel().getGraph(),
                    out, RDFFormat.TURTLE_BLOCKS, null, null);

//            StreamRDF sink = StreamRDFWriter.getWriterAsGiven(out, RDFFormat.TURTLE_BLOCKS, null);
//            sink.start();
//            StreamRDFOps.sendGraphToStream(repoConf.getModel().getGraph(), sink);
//            sink.finish();
        }
    }

    public static Function<OutputStream, OutputStream> createOutputStreamEncoder(List<String> encoderNames) {
        CompressorStreamFactory csf = new CompressorStreamFactory();

        Set<String> required = new LinkedHashSet<>(encoderNames);
        Set<String> missing = Sets.difference(required, csf.getOutputStreamCompressorNames());

        if (!missing.isEmpty()) {
            throw new RuntimeException("Unknown encoders requested; don't know how to handle: " + missing);
        }

        return out -> {
            for (String name : encoderNames) {
                try {
                    out = csf.createCompressorOutputStream(name, out);
                } catch (CompressorException e) {
                    throw new RuntimeException(e);
                }
            }
            return out;
        };
    }

    public static String getDcatId(DcatRepoLocal repo, Path path, String inputIdType) {
        DcatIdType idType = DcatIdType.of(inputIdType);

        Set<Resource> candidates = getRelatedDcatIds(repo, path, idType);

        Resource r = Iterables.getOnlyElement(candidates);
        return r.getURI();
    }

    public static Set<Resource> getRelatedDcatIds(DcatRepoLocal repo, Path filePath, DcatIdType idType) {
        Dataset ds = repo.getDataset();
        Model m = ds.getUnionModel();
        Path relPath = DcatRepoLocalUtils.normalizeRelPath(repo.getBasePath(), filePath);

        Set<Resource> result = Collections.singleton(m.createResource(relPath.toString()));

        if (!DcatIdType.FILE.equals(idType)) {

            result = result.stream().flatMap(r -> r.as(DcatDownloadUrl.class).getDistributions().stream())
                    .collect(Collectors.toSet());

            if (!DcatIdType.DISTRIBUTION.equals(idType)) {

                result = result.stream().flatMap(dist -> dist.as(DcatDistribution.class).getDcatDatasets(DcatDataset.class).stream())
                        .collect(Collectors.toSet());

                if (!DcatIdType.DATASET.equals(idType)) {
                    throw new RuntimeException("Unknown id type: " + idType);
                }
            }
        }

        return result;
    }

}
