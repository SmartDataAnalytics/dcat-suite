package org.aksw.dcat_suite.cli.cmd;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import org.aksw.dcat.jena.domain.api.MavenEntity;
import org.aksw.jena_sparql_api.common.DefaultPrefixes;
import org.aksw.jena_sparql_api.conjure.fluent.JobUtils;
import org.aksw.jena_sparql_api.conjure.job.api.Job;
import org.aksw.jena_sparql_api.rx.script.SparqlScriptProcessor;
import org.aksw.jenax.stmt.core.SparqlStmt;
import org.aksw.jenax.stmt.core.SparqlStmtParserImpl;
import org.apache.jena.query.Syntax;
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

        PrefixMapping prefixMapping = DefaultPrefixes.get(); // RDFDataMgr.loadModel("rdf-prefixes/prefix.cc.2019-12-17.ttl");
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

        Job job = JobUtils.fromSparqlStmts(sparqlStmts, optionalArgSet, varToExpr);


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

    
    public static Map<Var, Expr> parseStringMap(Map<String, String> map) {
        Map<Var, Expr> result = new HashMap<>();
        for (Entry<String, String> e : map.entrySet()) {
        	Var v = Var.alloc(e.getKey());
        	Expr expr = ExprUtils.parse(e.getValue());

        	result.put(v, expr);
        }
        return result;
    }
    



}

