package org.aksw.dcat_suite.cli.cmd.file;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "version", description="Get or set versions of dcat entities", mixinStandardHelpOptions = true)
public class CmdDcatVersion
    implements Callable<Integer>
{

    @Option(names= { "-s", "--set" })
    public String version;

    @Parameters
    protected List<String> artifacts;

    // protected boolean

    @Override
    public Integer call() throws Exception {



        return 0;
    }
}
