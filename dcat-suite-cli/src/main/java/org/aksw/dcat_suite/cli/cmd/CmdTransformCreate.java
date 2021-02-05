package org.aksw.dcat_suite.cli.cmd;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
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
import org.aksw.jena_sparql_api.rx.SparqlScriptProcessor;
import org.aksw.jena_sparql_api.stmt.SparqlStmt;
import org.aksw.jena_sparql_api.stmt.SparqlStmtParserImpl;
import org.aksw.jena_sparql_api.stmt.SparqlStmtUtils;
import org.aksw.jena_sparql_api.utils.NodeUtils;
import org.aksw.jena_sparql_api.utils.transform.NodeTransformCollectNodes;
import org.apache.jena.query.Syntax;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.shared.PrefixMapping;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "create", separator = "=", description="Create a new dataset transformation")
public class CmdTransformCreate
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

    @Option(names="--help", usageHelp=true)
    public boolean help = false;


    @Override
    public Integer call() throws Exception {
        CmdTransformCreate cmTransformCreate = this;

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
        Job job = fromSparqlStmts(sparqlStmts, optionalArgSet);


        job.setJobName(jobName);
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
            .map(NodeUtils::getEnvKey)
            .filter(Objects::nonNull)
            .map(Entry::getKey)
            .distinct()
            .collect(Collectors.toSet());

        return usedEnvVarNames;
    }

    public static Job fromSparqlStmts(
            List<SparqlStmt> stmts,
            Set<String> optionalArgs
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
                .setDeclaredVars(envVars)
                .setOpVars(Collections.singleton(opVarName));

        result.setDeclaredVars(mentionedEnvVars);


        return result;
    }

}

