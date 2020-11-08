package org.aksw.dcat_suite.cli.cmd;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.aksw.dcat_suite.cli.main.DcatOps;
import org.apache.jena.ext.com.google.common.collect.Iterables;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;

import com.beust.jcommander.Parameter;
import com.github.jsonldjava.shaded.com.google.common.collect.Maps;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "transform", separator = "=", description="Transform DCAT model and data")
public class CmdTransform
    implements Callable<Integer>
{
    @Parameters(description = "Non option args")
    public List<String> nonOptionArgs;

    @Option(names={"-t", "--transform"})
    public List<String> transforms = new ArrayList<>();

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
        CmdTransform cmTransform = this;

        // TODO Find some expectOne() args
        List<String> transforms = cmTransform.transforms;
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

        if (!transforms.isEmpty()) {
            Consumer<Resource> distTransform = DcatOps.createDistTransformer(
                    transforms, env, Paths.get("target"));

            DcatOps.transformAllDists(dcatModel, distTransform);
        }

        if(materalize) {
            Path path = Paths.get("target");
            Files.createDirectories(path);
            Consumer<Resource> materializer = DcatOps.createDistMaterializer(path);
            DcatOps.transformAllDists(dcatModel, materializer);
        }

        RDFDataMgr.write(System.out, dcatModel, RDFFormat.TURTLE_PRETTY);
        return 0;
    }
}
