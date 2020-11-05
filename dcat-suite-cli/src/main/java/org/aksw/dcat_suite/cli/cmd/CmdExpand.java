package org.aksw.dcat_suite.cli.cmd;


import java.util.concurrent.Callable;

import org.aksw.dcat_suite.cli.main.MainCliDcatSuite;

import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = "expand", separator = "=", description = "Expand quad datasets")
public class CmdExpand
    implements Callable<Integer>
{

    @Parameters(description = "Quad-based RDF dataset")
    protected String file;

    @Override
    public Integer call() throws Exception {
        CmdExpand cmExpand = this;

        MainCliDcatSuite.processExpand(cmExpand.file);
        return 0;
    }
}