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
import org.aksw.dcat.jena.conf.api.DcatRepoConfig;
import org.aksw.difs.system.domain.StoreDefinition;
import org.aksw.jena_sparql_api.http.domain.api.RdfEntityInfo;
import org.aksw.jenax.arq.dataset.api.ResourceInDataset;
import org.aksw.jenax.arq.dataset.impl.ResourceInDatasetImpl;
import org.aksw.jenax.arq.util.irixresolver.IRIxResolverUtils;
import org.aksw.jenax.arq.util.streamrdf.StreamRDFUtils;
import org.aksw.jenax.arq.util.streamrdf.StreamRDFWriterEx;
import org.aksw.jenax.arq.util.streamrdf.WriterStreamRDFBaseUtils;
import org.aksw.jenax.sparql.query.rx.RDFDataMgrEx;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFOps;
import org.apache.jena.riot.system.StreamRDFWriter;
import org.apache.jena.riot.system.SyntaxLabels;
import org.apache.jena.riot.writer.WriterStreamRDFBase;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.util.ResourceUtils;
import org.apache.jena.util.iterator.WrappedIterator;
import org.apache.jena.vocabulary.DCAT;
import org.apache.jena.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

public class DcatRepoLocalUtils {

    private static final Logger logger = LoggerFactory.getLogger(DcatRepoLocalUtils.class);

    public static final String DEFAULT_REPO_CONF_FILENAME = "dcat.repo.conf.ttl";

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
        return result;
    }

    public static Path getDcatRepoConfig(Path path) {
        return FileUtils.findInAncestors(path.toAbsolutePath(), DEFAULT_REPO_CONF_FILENAME);
    }


    public static DcatRepoLocal requireLocalRepo(Path searchStartPath) {
        Path configFile = getDcatRepoConfig(searchStartPath);

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
        // Path path = StandardSystemProperty.USER_DIR.
        Path currentFolder = Path.of("").toAbsolutePath();

        Path conf = getDcatRepoConfig(currentFolder);
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


    /**
     * Removes all encoding parts from the filename; optionally also removes the content type part.
     *
     * Given a file name and its detected encodings and content type,
     * assume that the base name can be obtained by removing that many trailing file extensions.
     *
     * For example, if for the file foo.bar.tar.gz the content type tar and encoding gz were
     * recognized then 2 trailing parts will be removed and the base name becomes foo.bar.
     *
     * At present the removal does not check whether the trailing parts actually correspond
     * to the detected encodings / content type - so if foo.bar.x.y is also probed to a gzipped tar
     * then the base name is also foo.bar.
     *
     * @param fileName
     * @param entityInfo
     * @return
     */
    public static String deriveBaseName(String fileName, EntityInfo entityInfo, boolean removeContentType) {
        int numExpectedExtensions = entityInfo.getContentEncodings().size() + (removeContentType ? 1 : 0);
        List<String> parts = Arrays.asList(fileName.split("\\."));
        int baseNameParts = Math.max(parts.size() - numExpectedExtensions, 1);
        String result = parts.subList(0, baseNameParts).stream().collect(Collectors.joining("."));
        return result;
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
}
