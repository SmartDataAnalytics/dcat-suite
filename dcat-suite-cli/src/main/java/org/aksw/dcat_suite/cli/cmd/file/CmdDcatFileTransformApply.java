package org.aksw.dcat_suite.cli.cmd.file;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.aksw.commons.io.util.StdIo;
import org.aksw.commons.util.obj.ObjectUtils;
import org.aksw.dcat.jena.domain.api.DcatDataset;
import org.aksw.dcat.jena.domain.api.DcatDistribution;
import org.aksw.dcat.jena.domain.api.MavenEntity;
import org.aksw.dcat_suite.cli.main.DcatOps;
import org.aksw.jena_sparql_api.conjure.datapod.api.RdfDataPod;
import org.aksw.jena_sparql_api.conjure.dataref.rdf.api.DataRef;
import org.aksw.jena_sparql_api.conjure.dataref.rdf.api.DataRefUrl;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.Op;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpDataRefResource;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpJobInstance;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpUnionDefaultGraph;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpVisitor;
import org.aksw.jena_sparql_api.conjure.dataset.engine.OpExecutorDefault;
import org.aksw.jena_sparql_api.conjure.dataset.engine.TaskContext;
import org.aksw.jena_sparql_api.conjure.job.api.Job;
import org.aksw.jena_sparql_api.conjure.job.api.JobInstance;
import org.aksw.jena_sparql_api.conjure.resourcespec.RPIF;
import org.aksw.jena_sparql_api.conjure.utils.ContentTypeUtils;
import org.aksw.jena_sparql_api.http.domain.api.RdfEntityInfo;
import org.aksw.jena_sparql_api.http.repository.impl.HttpResourceRepositoryFromFileSystemImpl;
import org.aksw.jenax.arq.dataset.api.ResourceInDataset;
import org.aksw.jenax.arq.util.lang.RDFLanguagesEx;
import org.aksw.jenax.model.prov.Activity;
import org.aksw.jenax.model.prov.Entity;
import org.aksw.jenax.model.prov.Plan;
import org.aksw.jenax.model.prov.QualifiedDerivation;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.RDFFormatVariant;
import org.apache.jena.riot.RDFWriterRegistry;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFOps;
import org.apache.jena.riot.system.StreamRDFWriter;
import org.apache.jena.system.Txn;
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


    @Option(names={"-u", "--union-default-graph"}, arity="0", description = "Apply the transformation to the union default graph of each input file", fallbackValue = "true")
    public Boolean unionDefaultGraphMode;

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

    @Option(names= {"--virtual"}, description = "Register a distribution that is the result of the transformation but do not generate the result file; default ${DEFAULT-VALUE}", arity="0", fallbackValue = "false")
    public boolean virtualDistribution;

    @Override
    public Integer call() throws Exception {

        DcatRepoLocal repo = DcatRepoLocalUtils.requireLocalRepo(Path.of(""));


        Path transformFilePath = Path.of(transformFile);
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
            if (graphAndDist == null) {
                throw new RuntimeException("No status information available about " + path + "; Use 'dcat file add' first");
            }

            // Create the union model in order to allow traversal among all graphs
            DcatDistribution dist = graphAndDist.inModel(graphAndDist.getDataset().getUnionModel())
                    .as(DcatDistribution.class);


            Path srcFile = Path.of(dist.getDownloadUrl());

            String srcFileName = srcFile.getFileName().toString();

            Resource srcFileNameRes = dist.getModel().createResource(srcFileName);

            Set<DcatDataset> datasets = dist.getDcatDatasets(DcatDataset.class);

            DcatDataset srcDataset = Iterables.getOnlyElement(datasets, null);
            if (srcDataset == null) {
                throw new RuntimeException("File " + path + " must appear in exacly 1 dataset; appears in: " + datasets.size());
            }

            MavenEntity srcEntity = srcDataset.as(MavenEntity.class);


            RdfEntityInfo srcEntityInfo = dist.as(RdfEntityInfo.class);
            String srcBaseName = DcatRepoLocalUtils.deriveBaseName(srcFileName, srcEntityInfo, false);

            String tag = Objects.requireNonNull(ObjectUtils.coalesce(() -> customTag, job::getTag),
                    "Transformation does not specify a tag; a custom one needs to be provided using --tag");

            MavenEntity tgtEntity = targetDataset.as(MavenEntity.class)
                .setGroupId(srcEntity.getGroupId())
                .setArtifactId(srcEntity.getArtifactId())
                .setVersion(srcEntity.getVersion());
            tgtEntity.getClassifiers().addAll(srcEntity.getClassifiers());
            tgtEntity.getClassifiers().add(tag);


            RdfEntityInfo tgtEntityInfo = ContentTypeUtils.deriveHeadersFromFileName(tgtFileExtension);
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


            // String provFileName = tgtFileName + ".prov.ttl";

            if (Files.exists(tgtFile)) {
                // TODO Check whether its part of the catalog
                throw new FileAlreadyExistsException(tgtFile.toString());
            }


            if (!virtualDistribution) {
                Function<OutputStream, OutputStream> encoder = DcatRepoLocalUtils.createOutputStreamEncoder(tgtEntityInfo.getContentEncodings());



                try(OutputStream out = encoder.apply(Files.newOutputStream(tgtFile))) {
                    StreamRDF sink = StreamRDFWriter.getWriterStream(out, rdfFormat);

                    // Op op = job.getOp();
                    DataRef dataRef = job.getModel().createResource().as(DataRefUrl.class)
                            .setDataRefUrl(srcFileName)
                            ;

                    JobInstance jobInst = JobInstance.create(job);
                    jobInst.getEnvMap().put("B", NodeFactory.createURI("http://ex.org/B/"));
                    jobInst.getEnvMap().put("D", NodeFactory.createURI("http://ex.org/D/"));

                    Op inputOp = OpDataRefResource.from(dataRef);
                    if (Boolean.TRUE.equals(unionDefaultGraphMode)) {
                        inputOp = OpUnionDefaultGraph.create(inputOp);
                    }

                    jobInst.getOpVarMap().put("ARG", inputOp);

                    OpJobInstance opJobIsnt = OpJobInstance.create(jobInst.getModel(), jobInst);
                    // job.add

                    // Map<String, Op> dataRefMapping = new HashMap<>();

                    HttpResourceRepositoryFromFileSystemImpl httpRepo = HttpResourceRepositoryFromFileSystemImpl.createDefault();

                    TaskContext taskContext = new TaskContext(srcEntity, new HashMap<>(), new HashMap<>());
                    OpVisitor<RdfDataPod> opExecutor = new OpExecutorDefault(
                            httpRepo,
                            // httpRepo.getCacheStore(),
                            taskContext,
                            new HashMap<>(),
                            // srcFileNameRes,
                            RDFFormat.TURTLE_BLOCKS);

                    RdfDataPod dataPod = opJobIsnt.accept(opExecutor);
//
//                    RdfDataPod dataPod = ExecutionUtils.executeJob(
//                            job,
//                            httpRepo,
//                            // httpRepo.getCacheStore(),
//                            taskContext,
//                            srcFileNameRes,
//                            RDFFormat.TURTLE_BLOCKS);

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

            Txn.executeWrite(repo.getDataset(), () -> {
                createProvenanceData(repo.getDataset(), srcFile, Arrays.asList(transformFilePath), tgtFile);
            });

            RDFDataMgr.write(StdIo.openStdOutWithCloseShield(), targetDataset.getModel(), RDFFormat.TURTLE_PRETTY);

        }
        return 0;
    }

    public static Function<Object, Resource> anyToResource(Model model) {
        return item -> model.createResource(item.toString());
    }

    public static Resource createProvenanceData(
            Dataset dataset,
            Path inputFileRelPath,
            List<Path> transformFileRelPaths,
            Path outputFileRelPath) {

        // Get the model of the target entity
        Model model = dataset.getNamedModel(outputFileRelPath.toString());

        Function<Object, Resource> mapper = anyToResource(model);

        return createProvenanceData(
                mapper.apply(inputFileRelPath),
                transformFileRelPaths.stream().map(mapper).collect(Collectors.toList()),
                mapper.apply(outputFileRelPath));
    }

    public static Entity createProvenanceData(
            Resource inputEntity,
            List<Resource> transformFileRelPaths,
            Resource outputEntity) {

        Entity derivedEntity = outputEntity.as(Entity.class);

        QualifiedDerivation qd = derivedEntity.addNewQualifiedDerivation();
        qd.setEntity(inputEntity);
        Activity activity = qd.getOrSetHadActivity();

        Plan plan = activity.getOrSetHadPlan();

        return derivedEntity;
    }

    public static Property prov(String name) {
        return ResourceFactory.createProperty("http://www.w3.org/ns/prov#" + name);
    }
}
