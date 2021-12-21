package org.aksw.dcat_suite.cli.cmd.file;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.aksw.commons.io.util.StdIo;
import org.aksw.dcat.jena.domain.api.DcatDataset;
import org.aksw.dcat.jena.domain.api.DcatDistribution;
import org.aksw.dcat.jena.domain.api.MavenEntity;
import org.aksw.dcat.util.ObjectUtils;
import org.aksw.dcat_suite.cli.main.DcatOps;
import org.aksw.jena_sparql_api.conjure.datapod.api.RdfDataPod;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.Op;
import org.aksw.jena_sparql_api.conjure.dataset.engine.ExecutionUtils;
import org.aksw.jena_sparql_api.conjure.job.api.Job;
import org.aksw.jena_sparql_api.conjure.resourcespec.RPIF;
import org.aksw.jena_sparql_api.conjure.utils.ContentTypeUtils;
import org.aksw.jena_sparql_api.http.domain.api.RdfEntityInfo;
import org.aksw.jenax.arq.dataset.api.ResourceInDataset;
import org.aksw.jenax.arq.util.lang.RDFLanguagesEx;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.RDFFormatVariant;
import org.apache.jena.riot.RDFWriterRegistry;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFOps;
import org.apache.jena.riot.system.StreamRDFWriter;
import org.apache.jena.vocabulary.DCAT;
import org.apache.jena.vocabulary.RDF;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

/**
 * Apply a transformation from the artifact registry or a file.
 *
 *
 *
 * @author raven
 *
 */
@Command(name = "apply", separator = "=", description="Transform DCAT model and data", mixinStandardHelpOptions = true)
public class CmdDcatFileTransformApply
    implements Callable<Integer>
{
    @Parameters(arity="1..*", description = "File list on which to apply the transformation")
    public List<String> filePaths;

    @Option(names={"-t"}, description = "The file containing the transformation which to apply")
    public String transformFile;


    @Option(names={"--tag"}, description = "Custom tag (overrides a transformation's one if present)")
    public String customTag = null;

    @Option(names={"-e", "--db-engine"}, description = "The (sparql) engine to use for running the workflow; default ${DEFAULT-VALUE}", defaultValue = "mem")
    public String dbEngine;


    @Option(names={"-x", "--file-extension"}, description = "The type of file to generate; e.g. ttl.bz2. default ${DEFAULT-VALUE}", defaultValue = "ttl")
    public String tgtFileExtension;


    /**
     * Environment variable assignments using the syntax
     * -D foo=bar -D 'moo=mar'
     *
     */
    @Option(names={"-D"})
    public List<String> envVars = new ArrayList<>();

    @Option(names= {"-v", "--virtual"}, description = "Register a distribution that is the result of the transformation but do not generate the result file; default ${DEFAULT-VALUE}", arity="0", defaultValue = "false")
    public boolean virtualDistribution;

    @Override
    public Integer call() throws Exception {

        DcatRepoLocal repo = DcatRepoLocalUtils.requireLocalRepo(Path.of(""));


        Model transformModel = RDFDataMgr.loadModel(transformFile);
        Resource xjob = ResourceFactory.createResource(RPIF.ns + "Job");

        List<Job> jobs = transformModel.listResourcesWithProperty(RDF.type, xjob)
            .mapWith(r -> r.as(Job.class))
            .toList();

        Job job = Iterables.getOnlyElement(jobs);

        // TODO Find some expectOne() args

        Map<String, Node> env = envVars.stream()
            .map(DcatOps::parseEntry)
            .map(e -> Maps.immutableEntry(e.getKey(), NodeFactory.createLiteral(e.getValue())))
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue));

//        Function<DcatDistribution, DcatDistribution> distTransform = DcatOps.createDistTransformerNew(
//                job, env);

        DcatDataset targetDataset = ModelFactory.createDefaultModel().createResource().as(DcatDataset.class);


        for (String filePath : filePaths) {
            Path path = Path.of(filePath).normalize();

            ResourceInDataset graphAndDist = DcatRepoLocalUtils.getFileStatus(repo, path);

            // Create the union model in order to allow traversal among all graphs
            DcatDistribution dist = graphAndDist.inModel(graphAndDist.getDataset().getUnionModel())
                    .as(DcatDistribution.class);


            Path srcFile = Path.of(dist.getDownloadUrl());

            String srcFileName = srcFile.getFileName().toString();

            Set<DcatDataset> datasets = dist.getDcatDatasets(DcatDataset.class);

            DcatDataset srcDataset = Iterables.getOnlyElement(datasets, null);
            if (srcDataset == null) {
                throw new RuntimeException("File " + path + " must appear in exacly 1 dataset; appears in: " + datasets.size());
            }

            MavenEntity srcEntity = srcDataset.as(MavenEntity.class);


            RdfEntityInfo srcEntityInfo = dist.as(RdfEntityInfo.class);
            String srcBaseName = DcatRepoLocalUtils.deriveBaseName(srcFileName, srcEntityInfo);

            String tag = Objects.requireNonNull(ObjectUtils.coalesce(() -> customTag, job::getTag),
                    "Transformation does not specify a tag; a custom one needs to be provided using --tag");

            MavenEntity tgtEntity = targetDataset.as(MavenEntity.class)
                .setGroupId(srcEntity.getGroupId())
                .setArtifactId(srcEntity.getArtifactId())
                .setVersion(srcEntity.getVersion());
            tgtEntity.getClassifiers().addAll(srcEntity.getClassifiers());
            tgtEntity.getClassifiers().add(tag);


            RdfEntityInfo tgtEntityInfo = ContentTypeUtils.deriveHeadersFromFileExtension(tgtFileExtension);
            String tgtContentType = tgtEntityInfo.getContentType();

            Table<Lang, RDFFormatVariant, RDFFormat> cands = HashBasedTable.create();
            RDFWriterRegistry.registered().stream()
                .filter(rdfFormat -> RDFLanguagesEx.matchesContentType(rdfFormat.getLang(), tgtContentType))
                .forEach(rdfFormat -> cands.put(rdfFormat.getLang(), rdfFormat.getVariant(), rdfFormat));

            Map<RDFFormatVariant, RDFFormat> variantToFormat = Iterables.getOnlyElement(cands.rowMap().values());
            RDFFormat rdfFormat = variantToFormat.get(RDFFormat.BLOCKS);

            List<String> elements = Arrays.asList(srcBaseName, tag, tgtFileExtension).stream()
                    .filter(item -> !item.isEmpty())
                    .collect(Collectors.toList());

            String tgtFileName = String.join(".", elements);

            Path tgtFile = srcFile.resolveSibling(tgtFileName);

            if (Files.exists(tgtFile)) {
                // TODO Check whether its part of the catalog
                throw new FileAlreadyExistsException(tgtFile.toString());
            }


            if (!virtualDistribution) {
                Function<OutputStream, OutputStream> encoder = DcatRepoLocalUtils.createOutputStreamEncoder(tgtEntityInfo.getContentEncodings());



                try(OutputStream out = encoder.apply(Files.newOutputStream(tgtFile))) {
                    StreamRDF sink = StreamRDFWriter.getWriterStream(out, rdfFormat);

                    Op op = job.getOp();
                    RdfDataPod dataPod = ExecutionUtils.executeJob(op);
                    Model model = dataPod.getModel();
                    sink.start();
                    StreamRDFOps.sendGraphToStream(model.getGraph(), sink);
                    sink.finish();
                    out.flush();
                    // RDFDataMgr.write(out, model, RDFFormat.TURTLE_PRETTY);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            }

            RDFDataMgr.write(StdIo.openStdOutWithCloseShield(), targetDataset.getModel(), RDFFormat.TURTLE_PRETTY);

        }
        return 0;
    }

}
