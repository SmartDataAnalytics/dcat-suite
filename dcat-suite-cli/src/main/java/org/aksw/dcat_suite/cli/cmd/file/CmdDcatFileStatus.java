package org.aksw.dcat_suite.cli.cmd.file;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import picocli.CommandLine.Parameters;

/** Add a file to the current dcat catalog */
public class CmdDcatFileStatus
    implements Callable<Integer>
{
    @Parameters(description = "Files to add as datasets to the current dcat repository")
    public List<String> files = new ArrayList<>();

    public Integer call() {

        for (String file : files) {

        }

        return 0;
    }

}
