package org.aksw.dcat_suite.cli.cmd;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;


@Command(name = "service", separator = "=", description = "Service Management", subcommands = {
        CmdServiceCreate.class
})
public class CmdService {
    @Option(names="--help", usageHelp=true)
    public boolean help = false;
}
