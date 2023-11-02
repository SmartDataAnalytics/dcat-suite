package org.aksw.dcat_suite.cli.cmd;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.aksw.commons.io.util.StdIo;
import org.aksw.dcat.jena.domain.api.DcatDataset;
import org.aksw.dcat.jena.domain.api.DcatDistribution;
import org.aksw.dcat.jena.domain.api.MavenEntity;
import org.aksw.dcat_suite.cli.main.DcatOps;
import org.aksw.jena_sparql_api.conjure.job.api.Job;
import org.aksw.jena_sparql_api.conjure.resourcespec.RpifTerms;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.vocabulary.DCAT;
import org.apache.jena.vocabulary.RDF;

import com.beust.jcommander.Parameter;
import com.github.jsonldjava.shaded.com.google.common.collect.Maps;
import com.google.common.collect.Iterables;

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
@Command(name = "apply", separator = "=", description="Transform DCAT model and data")
public class CmdTransformApply
    implements Callable<Integer>
{
    @Parameters(description = "Non option args")
    public List<String> nonOptionArgs;

    @Option(names={"--tg", "--transform-groupId"}, description = "The group id of the transformation to apply")
    public String transformGroupId;

    @Option(names={"--ta", "--transform-artifactId"}, description = "The artifact id of the transformation to apply")
    public String transformArtifactId;

    @Option(names={"--tv", "--transform-version"}, description = "The version of the transformation to apply")
    public String transformVersion;

    @Option(names={"-t", "--transform-file"}, description = "Use transformation(s) from a specific file. Makes group/artifact/version optional if the file contains only one transform.")
    public String transformFile;

    @Option(names= {"-g", "--groupId"}, arity = "1")
    public String groupId;

    /** Artifact id for the transform to be created */
    @Option(names= {"-a", "--artifactId"}, arity = "1")
    public String artifactId;

    /** Version for the transform to be created */
    @Option(names= {"-v", "--version"}, arity = "1")
    public String version;


//    public List<String> transforms = new ArrayList<>();

    /**
     * Environment variable assignments using the syntax
     * -D foo=bar -D 'moo=mar'
     *
     */
    @Option(names={"-D"})
    public List<String> envVars = new ArrayList<>();

    @Option(names= {"-m", "--materialize"}, arity="0")
    public boolean materialize = false;

    @Parameter(names="--help", help=true)
    public boolean help = false;

    @Override
    public Integer call() throws Exception {
        CmdTransformApply cmTransform = this;

        // TODO Load the default catalog if no file is given
        Model transformModel = RDFDataMgr.loadModel(transformFile);
        Resource xjob = ResourceFactory.createResource(RpifTerms.NS + "Job");

        List<Job> jobs = transformModel.listResourcesWithProperty(RDF.type, xjob)
            .mapWith(r -> r.as(Job.class))
            .toList();

        Job job = Iterables.getOnlyElement(jobs);

        // TODO Find some expectOne() args
        String dcatFile = Iterables.getOnlyElement(cmTransform.nonOptionArgs);
        Model dcatModel = RDFDataMgr.loadModel(dcatFile);

        boolean materalize = cmTransform.materialize;

        //Map<String, String> env = Collections.emptyMap();
        Map<String, Node> env = cmTransform.envVars.stream()
            .map(DcatOps::parseEntry)
            .map(e -> Maps.immutableEntry(e.getKey(), NodeFactory.createLiteral(e.getValue())))
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue));


//		Properties p = new Properties();
//		for(String env : cmTransform.envVars) {
//			p.load(new ByteArrayInputStream(env.getBytes()));
//		}
        //SparqlStmtUtils.processFile(pm, filenameOrURI)
        // cmTransform.nonOptionArgs

        Function<DcatDistribution, DcatDistribution> distTransform = DcatOps.createDistTransformerNew(
                job, env);

        DcatDataset targetDataset = ModelFactory.createDefaultModel().createResource().as(DcatDataset.class);

        MavenEntity mavenEntity = targetDataset.as(MavenEntity.class)
            .setGroupId(groupId)
            .setArtifactId(artifactId)
            .setVersion(version);

        List<DcatDataset> sourceDatasets = dcatModel.listSubjectsWithProperty(RDF.type, DCAT.Dataset)
            .mapWith(r -> r.as(DcatDataset.class))
            .toList();

        for (DcatDataset sourceDataset : sourceDatasets) {
            Collection<? extends DcatDistribution> dists = sourceDataset.getDistributions();

            for (DcatDistribution dist : dists) {
                DcatDistribution targetDist = distTransform.apply(dist);

                if (targetDist != null) {
                    targetDataset.getModel().add(targetDist.getModel());
                    targetDataset.getDistributionsAs(DcatDistribution.class).add(targetDist);
                }
            }
        }
//        DcatOps.transformAllDists(dcatModel, distTransform);

        if(materalize) {
            Path path = Paths.get("target");
            Files.createDirectories(path);
            Function<DcatDistribution, DcatDistribution> materializer = DcatOps.createDistMaterializerNew(path);

            // TODO Copy all attributes on the target dataset
            DcatDataset tmp = DcatOps.transformAllDists(targetDataset, materializer);

            for (Statement stmt : targetDataset.listProperties().toList()) {
                if (!stmt.getPredicate().equals(DCAT.distribution)) {
                    tmp.addProperty(stmt.getPredicate(), stmt.getObject());
                }
            }

            targetDataset = tmp;
        }

        RDFDataMgr.write(StdIo.openStdOutWithCloseShield(), targetDataset.getModel(), RDFFormat.TURTLE_PRETTY);
        return 0;
    }
}
