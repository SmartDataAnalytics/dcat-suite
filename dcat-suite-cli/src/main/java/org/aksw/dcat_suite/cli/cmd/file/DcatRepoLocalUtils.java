package org.aksw.dcat_suite.cli.cmd.file;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.aksw.commons.io.util.FileUtils;
import org.aksw.commons.io.util.symlink.SymbolicLinkStrategies;
import org.aksw.dcat.jena.conf.api.DcatRepoConfig;
import org.aksw.dcat.jena.domain.api.DcatDataset;
import org.aksw.dcat.jena.domain.api.DcatDistribution;
import org.aksw.dcat.jena.domain.api.DcatDownloadUrl;
import org.aksw.dcat.jena.domain.api.DcatIdType;
import org.aksw.dcat.mgmt.api.DataProject;
import org.aksw.dcat.mgmt.vocab.DCATX;
import org.aksw.difs.builder.DifsFactory;
import org.aksw.difs.system.domain.StoreDefinition;
import org.aksw.jena_sparql_api.conjure.datapod.api.RdfDataPod;
import org.aksw.jena_sparql_api.conjure.dataref.core.api.DataRefVisitor;
import org.aksw.jena_sparql_api.conjure.dataref.rdf.api.RdfDataRef;
import org.aksw.jena_sparql_api.conjure.dataref.rdf.api.RdfDataRefGraph;
import org.aksw.jena_sparql_api.conjure.dataref.rdf.api.RdfDataRefUrl;
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
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.util.ResourceUtils;
import org.apache.jena.vocabulary.RDF;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
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

    /** Resolve a relative path first against the current working directory and then
     * relativize it against the repository root */
    public static Path normalizeRelPath(Path basePath, Path filePath) {
        Path fileAbsPath = filePath.toAbsolutePath().normalize();
        Path result = basePath.relativize(fileAbsPath);

        // Path absPath = basePath.resolve(relPath).normalize();
        // Path result = basePath.relativize(absPath);
        return result;
    }

    /**
     * For a given path, find the graph with the same name and return the resource
     * in it which has that path as the download URL
     *
     * ?file {
     *   ?someResource dcat:downloadURL ?file
     * }
     *
     * @param repo
     * @param relPath
     * @return
     */
    public static ResourceInDataset getFileStatus(DcatRepoLocal repo, Path relPath) {
        relPath = normalizeRelPath(repo.getBasePath(), relPath);
        String pathStr = relPath.toString();
        Node pathNode = NodeFactory.createURI(pathStr);

        Dataset dataset = repo.getDataset();
//        ResourceInDataset result = new ResourceInDatasetImpl(dataset, pathStr, pathNode);
//        // DatasetOneNg ds = DatasetOneNgImpl.create(dataset, pathIri);
//
//        return result;

        ResourceInDataset rid = new ResourceInDatasetImpl(dataset, pathStr, pathNode);
        if (rid.getModel().isEmpty()) {
            rid = null;
        }
        return rid;
//        List<ResourceInDataset> tmp = WrappedIterator.create(
//                dataset.asDatasetGraph().find(pathNode, Node.ANY, DCAT.downloadURL.asNode(), pathNode))
//        .mapWith(quad -> (ResourceInDataset)new ResourceInDatasetImpl(dataset, quad.getGraph().getURI(), quad.getSubject()))
//        .toList();


        // return IterableUtils.expectZeroOrOneItems(tmp);
    }

    /**
     *
     * Very similar to RDFDataMgrEx.probeEntityInfo but relies on Files.probeContentType
     *
     * @param basePath The base path (will be made absolute) against which to resolve relPath
     * @param relPath The path to the file which also becomes the IRI of a graph
     * @return
     * @throws IOException
     */
    public static RdfEntityInfo probeFile(Path basePath, Path relPath) throws IOException {
        Path tgtPath = basePath.toAbsolutePath().resolve(relPath);

        List<String> encodings = new ArrayList<>();
        try (InputStream in = RDFDataMgrEx.probeEncodings(new BufferedInputStream(Files.newInputStream(tgtPath)), encodings)) {
        }

        String contentType = Files.probeContentType(tgtPath);
                // String charset = tis.getCharset();

        RdfEntityInfo info = ModelFactory.createDefaultModel().createResource().as(RdfEntityInfo.class);
        info.getContentEncodings().addAll(encodings);
        info.setContentType(contentType);
        // result.setCharset(charset);

        RdfEntityInfo result = ResourceUtils.renameResource(info, relPath.toString()).as(RdfEntityInfo.class);
        try {
            long size = Files.size(tgtPath);
            result.setByteSize(size);
        } catch (IOException e) {
            logger.warn("Failed to obtain file size for " + tgtPath);
        }

        return result;

//        RdfEntityInfo info;
//        try (InputStream in = Files.newInputStream(tgtPath)) {
//            List<Lang> PROBE_LANGS = Collections.unmodifiableList(Arrays.asList(
//                    RDFLanguages.NTRIPLES,
//                    RDFLanguages.TURTLE,
//                    RDFLanguages.NQUADS,
//                    RDFLanguages.TRIG,
//                    RDFLanguages.JSONLD,
//                    RDFLanguages.RDFXML,
//                    RDFLanguages.RDFTHRIFT
//                    // RDFLanguages.TRIX
//            ));
//            info = RDFDataMgrEx.probeEntityInfo(in, PROBE_LANGS);
//        } catch (IOException e1) {
//            throw new RuntimeException(e1);
//        }
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


    /**
     * Create a difs dataset from a given file with locks and transactions
     * managed in sibling directories of that file.
     *
     * @param datasetFile
     * @return
     * @throws IOException
     */
    public static Dataset createDifsFromFile(Path datasetFile) throws IOException {
        StoreDefinition d = ModelFactory.createDefaultModel().createResource().as(StoreDefinition.class)
                .setSingleFile(true)
                .setStorePath(datasetFile.toString())
                ;

        Dataset result = DifsFactory.newInstance().setStoreDefinition(d)
            .setSymbolicLinkStrategy(SymbolicLinkStrategies.FILE)
            .setRepoRootPath(datasetFile.getParent())
            .setUseJournal(true)
            .setCreateIfNotExists(false)
            .connectAsDataset();
        return result;
    }

    /** Create a repo object from a specific config file */
    public static DcatRepoLocal loadLocalRepo(Path configFile) {

        if (configFile == null) {
            throw new RuntimeException("No local dcat repository detected in any parent folders");
        }

        Path repoRootFolder = configFile.getParent();
        logger.info("Using dcat repository from " + configFile);

        DcatRepoLocal result;


        logger.debug("Checking for git repository");
        FileRepositoryBuilder repositoryBuilder = new FileRepositoryBuilder();
        repositoryBuilder.setMustExist(true);
        // TODO Make the ".git" configurable
        repositoryBuilder.setGitDir(repoRootFolder.resolve(".git").toFile());
        Repository repository = null;
        try {
            repository = repositoryBuilder.build();
        } catch (IOException e1) {
            logger.info("No git repository found - git functionality not available");
        }


        try {
            result = new DcatRepoLocalImpl(configFile, repoRootFolder, null, repository);
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

        Path conf = currentFolder.toAbsolutePath().resolve(DEFAULT_REPO_CONF_FILENAME);

        Path existingConf = findDcatRepoConfig(currentFolder);
        if (existingConf != null) {
            if (existingConf.equals(conf)) {
                throw new RuntimeException("Configuration already exists in the current folder: " + conf);
            } else {
                logger.warn("Creating a store nested inside another one because"
                        + "there already exists a config at this parent folder: " + existingConf);
                // throw new RuntimeException("A file already exists (TODO force flag not yet implemented) at " + conf);
            }
        }

        // Path confAbsPath = conf.toAbsolutePath();
        logger.info("Writing config to  " + conf);

        Model repoConfModel = ModelFactory.createDefaultModel();
        DcatRepoConfig repoConf = repoConfModel.createResource(DEFAULT_REPO_CONF_FILENAME).as(DcatRepoConfig.class);
        repoConf.addProperty(RDF.type, DcatRepoLocalImpl.RepoConfig);
        repoConf.setEngine("difs");


        StoreDefinition storeConf = repoConfModel.createResource().as(StoreDefinition.class)
                .setSingleFile(true)
                .setStorePath("dcat.trig");

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



    public static Entry<RdfDataRef, Model> resolveContent(String input, DataRefVisitor<? extends RdfDataPod> dataEngineCreator) {
//        IRIxResolver resolver = IRIxResolver.create().allowRelative(true).build();
//        IRIx irix = resolver.resolve(input);
//        String iriStr = irix.toString();

//        RdfDataRef dataRef;
//        RdfDataPod rdfDataEngine;

        RdfDataRef dataRef = RdfDataRefUrl.create(ModelFactory.createDefaultModel(), input);
        RdfDataPod rdfDataEngine = dataRef.accept(dataEngineCreator);

        if (rdfDataEngine == null) {
            dataRef = RdfDataRefGraph.create(ModelFactory.createDefaultModel(), input);
            rdfDataEngine = dataRef.accept(dataEngineCreator);
        }

        Model model = rdfDataEngine.getModel();

        return new SimpleEntry<>(dataRef, model);

//        Resource transformRes = repo.getDataset().getUnionModel().createResource(transformFilePath.toString());
//
//
//        // Load the transformation
//        Model transformModel;
//        DcatDistribution transformDist = null;
//        if (Files.exists(transformFilePath)) {
//             transformModel = RDFDataMgr.loadModel(transformFilePath.toString());
//        } else {
//            transformDist = DcatUtils.resolveDistribution(transformRes);
//            DcatIdType transformIt = DcatIdType.of(transformIdType);
//
//            Resource transformId = DcatUtils.getRelatedId(transformDist, transformIt);
//
//            transformModel = loadDistributionAsModel(repo.getBasePath(), transformDist); // RDFDataMgr.loadModel(transformFileOrId);
//        }
//
    }
}
