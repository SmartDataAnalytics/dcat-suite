package org.aksw.dcat_suite.cli.cmd;

import java.util.concurrent.Callable;

import org.aksw.ckan_deploy.core.DcatCkanRdfUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;

import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = "show", description = "Show DCAT information")
public class CmdShow
    implements Callable<Integer>
{

    @Parameters(description = "Any RDF file")
    protected String file;

    @Override
    public Integer call() throws Exception {
        CmdShow cmShow = this;

        Model dcatModel = DcatCkanRdfUtils.createModelWithNormalizedDcatFragment(cmShow.file);
        RDFDataMgr.write(System.out, dcatModel, RDFFormat.TURTLE_PRETTY);

        return 0;
    }
}