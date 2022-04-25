package org.aksw.dcat_suite.cli.cmd;

import java.util.List;

import org.aksw.dcat_suite.cli.cmd.catalog.CmdDcatCatalogEnrich;
import org.aksw.dcat_suite.cli.cmd.file.CmdDcatAnnotationParent;
import org.aksw.dcat_suite.cli.cmd.file.CmdDcatFileAdd;
import org.aksw.dcat_suite.cli.cmd.file.CmdDcatFileInit;
import org.aksw.dcat_suite.cli.cmd.file.CmdDcatFileParent;
import org.aksw.dcat_suite.cli.cmd.file.CmdDcatFileRm;
import org.aksw.dcat_suite.cli.cmd.file.CmdDcatMvnParent;
import org.aksw.dcat_suite.cli.cmd.file.CmdDcatMvnizeParent;
import org.aksw.dcat_suite.cli.cmd.file.CmdDcatPropParent;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "dcat", separator = "=", description = "Show DCAT information", subcommands = {
    CmdDcatFileInit.class,
    CmdDcatPropParent.class,
    CmdDcatFileAdd.class,
    CmdDcatFileRm.class,
    CmdDcatMvnizeParent.class,
    CmdDcatAnnotationParent.class,
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
    CmdTransform.class,
    CmdDcatCatalogEnrich.class,
    CmdDcatFileParent.class,
    CmdDcatMvnParent.class
})
public class CmdDcatSuiteMain {
    @Parameters(description = "Non option args")
    protected List<String> nonOptionArgs;

    @Option(names = "--help", usageHelp = true)
    protected boolean help = false;
}