package org.aksw.dcat_suite.cli.cmd.file;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Callable;

import org.aksw.dcat.jena.domain.api.DcatDataset;
import org.aksw.jenax.arq.dataset.api.DatasetOneNg;
import org.aksw.jenax.arq.dataset.impl.DatasetOneNgImpl;
import org.aksw.jenax.arq.util.node.NodeTransformLib2;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.graph.NodeTransform;
import org.apache.jena.sparql.syntax.syntaxtransform.NodeTransformSubst;
import org.apache.jena.vocabulary.DCAT;
import org.apache.jena.vocabulary.RDF;

import picocli.CommandLine.Command;

@Command(name = "finalize", separator = "=", description="Create a dcat.ttl file with final URIs", mixinStandardHelpOptions = true)
public class CmdDcatFileFinalize
    implements Callable<Integer>
{
    @Override
    public Integer call() throws Exception {
        DcatRepoLocal repo = DcatRepoLocalUtils.findLocalRepo(Path.of(""));

        Dataset result = DatasetFactory.create();
        Dataset repoDs = repo.getDataset();
        repoDs.begin(ReadWrite.READ);
        result.asDatasetGraph().addAll(repoDs.asDatasetGraph());
        repoDs.commit();


        Iterator<Node> it = result.asDatasetGraph().listGraphNodes();
        Map<Node, Node> renames = new HashMap<>();
        while (it.hasNext()) {
            Node g = it.next();

            DatasetOneNg dong = DatasetOneNgImpl.create(repoDs, g);
            Resource r = dong.getSelfResource();

            if (r.hasProperty(RDF.type, DCAT.Dataset)) {
                DcatDataset dcatDataset = r.as(DcatDataset.class);

                String id = DcatIdUtils.createDatasetId(dcatDataset);
                dcatDataset.setIdentifier(id);

                renames.put(r.asNode(), NodeFactory.createURI("#" + id));
            }
        }

        NodeTransformLib2.applyNodeTransform(NodeTransformLib2.wrapWithNullAsIdentity(renames::get), result);

        RDFDataMgr.write(System.out, result, RDFFormat.TRIG);
        return 0;
    }

    //@Parameters(description = "Files to add as datasets to the current dcat repository")
    // public List<String> files = new ArrayList<>();
}
