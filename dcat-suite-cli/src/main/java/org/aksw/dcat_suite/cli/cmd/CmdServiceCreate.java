package org.aksw.dcat_suite.cli.cmd;

import java.util.ArrayList;
import java.util.List;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "craete", separator = "=", description="Service Creation")
public class CmdServiceCreate {
    @Parameters(description="Non option args")
    public List<String> nonOptionArgs;

    @Option(names={"-t", "--tag"}, description="A name for the service")
    public List<String> transforms = new ArrayList<>();

    @Option(names="--help", usageHelp=true)
    public boolean help = false;
}