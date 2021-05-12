package org.aksw.dcat_suite.cli.cmd;

import java.util.List;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "dcat", separator = "=", description = "Show DCAT information", subcommands = {
    CmdDeploy.class,
    CmdEnrich.class,
    CmdExpand.class,
    CmdSearch.class,
    CmdData.class,
    CmdImport.class,
    CmdInstall.class,
    CmdIntegrate.class,
    CmdService.class,
    CmdShow.class,
    CmdTransform.class
})
public class CmdDcatSuiteMain {
    @Parameters(description = "Non option args")
    protected List<String> nonOptionArgs;

    @Option(names = "--help", usageHelp = true)
    protected boolean help = false;
}