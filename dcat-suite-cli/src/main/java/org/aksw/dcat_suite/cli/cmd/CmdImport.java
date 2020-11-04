package org.aksw.dcat_suite.cli.cmd;

import picocli.CommandLine.Command;

@Command(name = "import", description = "Retrieve DCAT descriptions", subcommands = {
        CmdImportCkan.class
})
public class CmdImport {
}