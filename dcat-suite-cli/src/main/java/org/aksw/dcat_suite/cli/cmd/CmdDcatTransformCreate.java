package org.aksw.dcat_suite.cli.cmd;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import org.aksw.dcat.jena.domain.api.MavenEntity;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.Op;
import org.aksw.jena_sparql_api.conjure.fluent.ConjureBuilder;
import org.aksw.jena_sparql_api.conjure.fluent.ConjureBuilderImpl;
import org.aksw.jena_sparql_api.conjure.job.api.Job;
import org.aksw.jena_sparql_api.conjure.job.api.JobParam;
import org.aksw.jena_sparql_api.rx.script.SparqlScriptProcessor;
import org.aksw.jenax.arq.util.node.NodeEnvsubst;
import org.aksw.jenax.arq.util.node.NodeTransformCollectNodes;
import org.aksw.jenax.stmt.core.SparqlStmt;
import org.aksw.jenax.stmt.core.SparqlStmtParserImpl;
import org.aksw.jenax.stmt.util.SparqlStmtUtils;
import org.apache.jena.query.Syntax;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.E_Equals;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.util.ExprUtils;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "create", separator = "=", description="Create a new dataset transformation")
public class CmdDcatTransformCreate
    implements Callable<Integer>
{
    @Parameters(description = "SPARQL Statements (inline arguments or files)")
    public List<String> sparqlStmtRefs = new ArrayList<>();

    @Option(names="-o", arity="0..*")
    public List<String> optionalArgs = new ArrayList<>();

    /** Group id for the transform to be created */
    @Option(names= {"-g", "--groupId"}, arity = "1")
    public String groupId;

    /** Artifact id for the transform to be created */
    @Option(names= {"-a", "--artifactId"}, arity = "1")
    public String artifactId;

    /** Version for the transform to be created */
    @Option(names= {"-v", "--version"}, arity = "1")
    public String version;

    @Option(names= {"-c", "--conformance"}, description = "IRIs of specifications to which the output conforms to")
    public List<String> conformances = new ArrayList<>();

    @Option(names= {"-t", "--tag"}, description = "A short handle to add to e.g. filenames of output files ")
    public String tag;

    @Option(names= {"-b", "--bind"}, description = "Provide an expression to compute the default value of a placeholder")
    public List<String> defaultBindings = new ArrayList<>();

    @Option(names="--help", usageHelp=true)
    public boolean help = false;


    @Override
    public Integer call() throws Exception {

//        JobParam test = ModelFactory.createDefaultModel().createResource().as(JobParam.class);
//        test.setDefaultValueExpr(ExprUtils.parse("<http://foo>"));
//        System.out.println(test.getDefaultValueExpr());
//
//        if (true) {
//            return 0;
//        }

        CmdDcatTransformCreate cmTransformCreate = this;

        PrefixMapping prefixMapping = RDFDataMgr.loadModel("rdf-prefixes/prefix.cc.2019-12-17.ttl");
        SparqlScriptProcessor scriptProcessor = new SparqlScriptProcessor(
                prologue -> SparqlStmtParserImpl.create(Syntax.syntaxARQ, prologue, false),
                prefixMapping);
        // new SparqlScriptProcessor(SparqlStmtParserImpl.create(prefixMapping), prefixMapping);

        scriptProcessor.process(sparqlStmtRefs);

//        Model model = ModelFactory.createDefaultModel();
        String jobName = Arrays.asList(groupId, artifactId, version).stream()
                .collect(Collectors.joining(":"));
        // Job job = Job.create(model, jobName);

        // TODO Differ between mandatory and optional vars
        List<SparqlStmt> sparqlStmts = scriptProcessor.getSparqlStmts().stream()
                .map(Entry::getKey).collect(Collectors.toList());

        Set<String> optionalArgSet = new LinkedHashSet<>(optionalArgs);

        Map<Var, Expr> varToExpr = new HashMap<>();
        for (String str : defaultBindings) {
            Expr expr = ExprUtils.parse(str);
            if (!(expr instanceof E_Equals)) {
                throw new IllegalArgumentException("Equality expression expected of the form ?var = ");
            }

            E_Equals e = (E_Equals)expr;

            Expr arg1 = e.getArg1();
            if (!arg1.isVariable()) {
                throw new IllegalArgumentException("Left hand side of expression must be a variable");
            }

            varToExpr.put(arg1.asVar(), e.getArg2());
        }

        Job job = fromSparqlStmts(sparqlStmts, optionalArgSet, varToExpr);


        job.setJobName(jobName);

        job.setTag(tag);
        job.getConformances().addAll(conformances);

        MavenEntity mvnEntity = job.as(MavenEntity.class);
        mvnEntity.setGroupId(groupId);
        mvnEntity.setVersion(version);
        mvnEntity.setArtifactId(artifactId);


        RDFDataMgr.write(System.out, job.getModel(), RDFFormat.TURTLE_BLOCKS);

        return 0;
    }


    public static Set<String> getMentionedEnvVars(Collection<? extends SparqlStmt> sparqlStmts) {
        NodeTransformCollectNodes collector = new NodeTransformCollectNodes();

        for (SparqlStmt sparqlStmt : sparqlStmts) {
            SparqlStmtUtils.applyNodeTransform(sparqlStmt, collector);
        }

        // Get all environment references
        // TODO Make this a util function
        Set<String> usedEnvVarNames = collector.getNodes().stream()
            .map(NodeEnvsubst::getEnvKey)
            .filter(Objects::nonNull)
            .map(Entry::getKey)
            .distinct()
            .collect(Collectors.toSet());

        return usedEnvVarNames;
    }

    public static Job fromSparqlStmts(
            List<SparqlStmt> stmts,
            Set<String> optionalArgs,
            Map<Var, Expr> varToExpr
        ) {


        Set<String> mentionedEnvVars = getMentionedEnvVars(stmts);

// TODO Add API for Query objects to fluent
//		List<SparqlStmt> stmts = Streams.stream(SparqlStmtUtils.processFile(DefaultPrefixes.prefixes, path))
//				.collect(Collectors.toList());

        List<String> stmtStrs = stmts.stream()
                .map(Object::toString)
                .collect(Collectors.toList());

//
//		List<String> queries = RDFDataMgrEx.loadQueries(path, DefaultPrefixes.prefixes).stream()
//				.map(Object::toString)
//				.collect(Collectors.toList());
        ConjureBuilder cj = new ConjureBuilderImpl();

        String opVarName = "ARG";
//        Op op = cj.fromVar(opVarName).stmts(stmtStrs).getOp();
        Op op = cj.fromVar(opVarName).construct(stmtStrs).getOp();

//		Set<String> vars = OpUtils.mentionedVarNames(op);
//		for(SparqlStmt stmt : stmts) {
//			System.out.println("Env vars: " + SparqlStmtUtils.mentionedEnvVars(stmt));
//		}

        Map<String, Boolean> combinedMap = stmts.stream()
            .map(SparqlStmtUtils::mentionedEnvVars)
            .map(Map::entrySet)
            .flatMap(Collection::stream)
            .distinct()
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue));

        Set<String> envVars = combinedMap.keySet();
//		System.out.println("All env vars: " + combinedMap);


//		System.out.println("MentionedVars: " + vars);

        Job result = Job.create(cj.getContext().getModel())
                .setOp(op)
                // .setDeclaredVars(envVars)
                .setOpVars(Collections.singleton(opVarName));

        for (String varName : mentionedEnvVars) {
            JobParam param = result.addNewParam();
            param.setParamName(varName);

            Var v = Var.alloc(varName);
            Expr expr = varToExpr.get(v);
            param.setDefaultValueExpr(expr);
        }
        // result.setDeclaredVars(mentionedEnvVars);


        return result;
    }

}

