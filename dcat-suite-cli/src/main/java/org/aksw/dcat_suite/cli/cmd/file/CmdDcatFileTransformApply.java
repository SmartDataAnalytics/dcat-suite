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
import org.aksw.commons.util.string.Envsubst;
import org.aksw.commons.util.string.FileNameUtils;
import org.aksw.dcat.jena.domain.api.DcatDataset;
import org.aksw.dcat.jena.domain.api.DcatDistribution;
import org.aksw.dcat.jena.domain.api.DcatIdType;
import org.aksw.dcat.jena.domain.api.DcatUtils;
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
import org.aksw.jena_sparql_api.conjure.fluent.JobUtils;
import org.aksw.jena_sparql_api.conjure.job.api.Job;
import org.aksw.jena_sparql_api.conjure.job.api.JobInstance;
import org.aksw.jena_sparql_api.conjure.job.api.JobParam;
import org.aksw.jena_sparql_api.conjure.noderef.NodeRef;
import org.aksw.jena_sparql_api.conjure.utils.ContentTypeUtils;
import org.aksw.jena_sparql_api.http.domain.api.RdfEntityInfo;
import org.aksw.jena_sparql_api.http.repository.impl.HttpResourceRepositoryFromFileSystemImpl;
import org.aksw.jena_sparql_api.rx.RDFLanguagesEx;
import org.aksw.jenax.arq.dataset.api.ResourceInDataset;
import org.aksw.jenax.arq.util.binding.BindingUtils;
import org.aksw.jenax.arq.util.streamrdf.StreamRDFWriterEx;
import org.aksw.jenax.model.prov.Activity;
import org.aksw.jenax.model.prov.Entity;
import org.aksw.jenax.model.prov.Plan;
import org.aksw.jenax.model.prov.QualifiedDerivation;
import org.aksw.jenax.reprogen.core.MapperProxyUtils;
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
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.util.ExprUtils;
import org.apache.jena.system.Txn;
import org.apache.jena.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final Logger logger = LoggerFactory.getLogger(CmdDcatFileTransformApply.class);


    @Parameters(arity="1..*", description = "File list on which to apply the transformation")
    public List<String> filePaths;

    @Option(names={"-t"}, description = "The file containing the transformation which to apply")
    public String transformFileOrId;

    @Option(names={"--ti"}, description = "The identifier to use for referring to the transformation in the provenance (dataset, distribution or file); default: ${DEFAULT-VALUE} ", defaultValue="dataset")
    public String transformIdType;

    @Option(names={"-u", "--union-default-graph"}, arity="0", description = "Apply the transformation to the union default graph of each input file", fallbackValue = "true")
    public Boolean unionDefaultGraphMode;

    @Option(names={"-i"}, arity="0", description = "Controls which identifier to use for references to data content. May be 'file', 'distribution' or 'dataset'; default: ${DEFAULT-VALUE}", defaultValue = "dataset")
    public String inputIdType;

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



//
//        String str = String.join("\n",
//            "PREFIX dcat: <http://www.w3.org/ns/dcat> .",
//            "SELECT ?dataset ?distribution ?downloadUrl {",
//            "  ?distribution dcat:downloadURL ?downloadUrl",
//            "  OPTONAL { ?dataset dcat:distribution ?distribution }",
//            "}");
//
//        Query q = QueryFactory.create(str);
//        SparqlQueryConnection conn;
//
//        // url to distribution to dataset
//        Table<Node, Node, Set<Node>> result = HashBasedTable.create();
//
//        try (QueryExecution qe = conn.query(q)) {
//            ResultSet rs = qe.execSelect();
//            while (rs.hasNext()) {
//                Binding b = rs.nextBinding();
//
//                Node a = b.get(colName1);
//                Node b = b.get(colName2);
//                Node c = b.get(colName3);
//                result.put(a, b, c);
//            }
//        }
//
//        return result;


    public static Model loadDistributionAsModel(Path basePath, DcatDistribution dist) {
        String url = Iterables.getOnlyElement(dist.getDownloadUrls());
        Model result = RDFDataMgr.loadModel(url);
        return result;
    }

    @Override
    public Integer call() throws Exception {

        DcatRepoLocal repo = DcatRepoLocalUtils.findLocalRepo(Path.of(""));

        // Try to resolve the reference to the transform to the resource that contains it

        Path transformFilePath = DcatRepoLocalUtils.normalizeRelPath(repo.getBasePath(), transformFileOrId);
        Resource transformRes = repo.getDataset().getUnionModel().createResource(transformFilePath.toString());

        DcatDistribution transformDist = DcatUtils.resolveDistribution(transformRes);
        DcatIdType transformIt = DcatIdType.of(transformIdType);

        Resource transformId = DcatUtils.getRelatedId(transformDist, transformIt);



        Model transformModel = loadDistributionAsModel(repo.getBasePath(), transformDist); // RDFDataMgr.loadModel(transformFileOrId);

        Job job = JobUtils.getOnlyJob(transformModel);



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

            String inputVal = DcatRepoLocalUtils.getDcatId(repo, srcFile, inputIdType);
            logger.info("Using content identifier " + inputVal);

            Set<DcatDataset> datasets = dist.getDcatDatasets(DcatDataset.class);

            DcatDataset srcDataset = Iterables.getOnlyElement(datasets, null);
            if (srcDataset == null) {
                throw new RuntimeException("File " + path + " must appear in exacly 1 dataset; appears in: " + datasets.size());
            }

            MavenEntity srcEntity = srcDataset.as(MavenEntity.class);


            RdfEntityInfo srcEntityInfo = dist.as(RdfEntityInfo.class);
            String srcBaseName = FileNameUtils.deriveFileName(srcFileName, srcEntityInfo).getBaseName();

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
                    StreamRDF sink = StreamRDFWriterEx.getWriterStream(out, rdfFormat, null);  // StreamRDFWriter.getWriterStream(out, rdfFormat);

                    Model jobInstModel = ModelFactory.createDefaultModel();

                    // Op op = job.getOp();
                    DataRef dataRef = jobInstModel.createResource().as(DataRefUrl.class)
                            .setDataRefUrl(srcFileName)
                            ;


                    // Resource transformFileRes = transformFile.toString();
                    // NodeRef jobRef = NodeRef.createForFile(jobInstModel, transformFileOrId.toString(), null, null);
                    NodeRef jobRef = NodeRef.createForDcatEntity(jobInstModel, transformRes.asNode(), null, null);
                    // NodeRef jobRef = jobInstanceModel.createResource()

                    JobInstance jobInst = jobInstModel.createResource().as(JobInstance.class);
                    jobInst.setJobRef(jobRef);

                    Map<String, Node> envMap = jobInst.getEnvMap();

//                    envMap.put("INPUT", NodeFactory.createLiteral(srcFile.toString() + "#content"));
                    envMap.put("INPUT", NodeFactory.createLiteral(inputVal));
                    envMap.put("CLASSIFIER", NodeFactory.createLiteral(tag));

//                    jobInst.getEnvMap().put("B", NodeFactory.createURI("http://ex.org/B/"));
//                    jobInst.getEnvMap().put("D", NodeFactory.createURI("http://ex.org/D/"));

                    // Try to bind yet unbound params
                    for (JobParam param : job.getParams()) {
                        String name = param.getParamName();
                        if (!envMap.containsKey(name)) {
                            Expr expr = param.getDefaultValueExpr();

                            if (expr != null) {
                                String str = expr.toString();
                                String str2 = Envsubst.envsubst(str, x -> envMap.get(x).toString(false));
                                expr = ExprUtils.parse(str2);
                                Map<Var, Node> tmp = envMap.entrySet().stream().collect(Collectors
                                        .toMap(e -> Var.alloc(e.getKey()), Entry::getValue));
                                Binding b = BindingUtils.fromMap(tmp);
                                //Set<Var> vars = expr.getVarsMentioned();

                                NodeValue nv = ExprUtils.eval(expr, b);
                                envMap.put(name, NodeFactory.createLiteral(nv.getNode().toString(false)));
                            }
                        }
                    }


                    Op inputOp = OpDataRefResource.from(dataRef);
                    if (Boolean.TRUE.equals(unionDefaultGraphMode)) {
                        inputOp = OpUnionDefaultGraph.create(inputOp);
                    }

                    jobInst.getOpVarMap().put("ARG", inputOp);



                    Dataset repoDataset = repo.getDataset();
                    Txn.executeWrite(repoDataset, () -> {
                        Entity res = createProvenanceData(repoDataset, srcFile, jobInst, tgtFile);

                        MapperProxyUtils.skolemize("", res,
                                map -> map.remove(RDF.nil));

                        // createProvenanceData(repo.getDataset(), srcFile, Arrays.asList(transformFilePath), tgtFile);
                    });

                    OpJobInstance opJobInst = OpJobInstance.create(jobInst.getModel(), jobInst);
                    // job.add

                    // Map<String, Op> dataRefMapping = new HashMap<>();

                    HttpResourceRepositoryFromFileSystemImpl httpRepo = HttpResourceRepositoryFromFileSystemImpl.createDefault();

                    TaskContext taskContext = new TaskContext(srcEntity, new HashMap<>(), new HashMap<>());

                    Model repoUnionModel = repoDataset.getUnionModel();
                    taskContext.getCtxModels().put("thisCatalog", repoUnionModel);

                    OpVisitor<RdfDataPod> opExecutor = new OpExecutorDefault(
                            httpRepo,
                            // httpRepo.getCacheStore(),
                            taskContext,
                            new HashMap<>(),
                            // srcFileNameRes,
                            RDFFormat.TURTLE_BLOCKS);

                    RdfDataPod dataPod = opJobInst.accept(opExecutor);
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

            RDFDataMgr.write(StdIo.openStdOutWithCloseShield(), targetDataset.getModel(), RDFFormat.TURTLE_PRETTY);

        }
        return 0;
    }

    public static Function<Object, Resource> anyToResource(Model model) {
        return item -> model.createResource(item.toString());
    }

    public static Entity createProvenanceData(
            Dataset dataset,
            Path inputFileRelPath,
            // List<Path> transformFileRelPaths,
            JobInstance jobInstance,
            Path outputFileRelPath) {

        // Get the model of the target entity
        Model model = dataset.getNamedModel(outputFileRelPath.toString());

        Function<Object, Resource> mapper = anyToResource(model);
        Resource tgtRes = mapper.apply(outputFileRelPath);
        tgtRes.addLiteral(ResourceFactory.createProperty("https://rpif.aksw.org/substitute"), true);

        return createProvenanceData(
                mapper.apply(inputFileRelPath),
                jobInstance,
                // transformFileRelPaths.stream().map(mapper).collect(Collectors.toList()),
                tgtRes);
    }

    public static Entity createProvenanceData(
            Resource inputEntity,
            JobInstance jobInstance,
            Resource outputEntity) {

        Model outModel = outputEntity.getModel();
        outModel.add(jobInstance.getModel());
        Entity derivedEntity = outputEntity.as(Entity.class);

        QualifiedDerivation qd = derivedEntity.addNewQualifiedDerivation();
        qd.setEntity(inputEntity);
        Activity activity = qd.getOrSetHadActivity();

        activity.setHadPlan(jobInstance);
        // Plan plan = activity.getOrSetHadPlan();
        // JobInstance ji = plan.as(JobInstance.class);

        // FIXME Get the job iris from the list; requires a bit of refactoring
//        String jobIri = transformFileRelPaths.get(0).getURI();
//        NodeRef jobRef = NodeRef.createForFile(outModel, jobIri, null, null);
//        ji.setJobRef(jobRef);

        return derivedEntity;
    }
    public static Entity createProvenanceDataOld(
            Resource inputEntity,
            List<Resource> transformFileRelPaths,
            Resource outputEntity) {

        Model outModel = outputEntity.getModel();
        Entity derivedEntity = outputEntity.as(Entity.class);

        QualifiedDerivation qd = derivedEntity.addNewQualifiedDerivation();
        qd.setEntity(inputEntity);
        Activity activity = qd.getOrSetHadActivity();

        Plan plan = activity.getOrSetHadPlan();
        JobInstance ji = plan.as(JobInstance.class);

        // FIXME Get the job iris from the list; requires a bit of refactoring
        String jobIri = transformFileRelPaths.get(0).getURI();
        NodeRef jobRef = NodeRef.createForFile(outModel, jobIri, null, null);
        ji.setJobRef(jobRef);

        return derivedEntity;
    }

    public static Property prov(String name) {
        return ResourceFactory.createProperty("http://www.w3.org/ns/prov#" + name);
    }
}
