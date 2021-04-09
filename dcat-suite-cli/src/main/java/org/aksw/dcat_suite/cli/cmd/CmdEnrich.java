package org.aksw.dcat_suite.cli.cmd;

import picocli.CommandLine.Command;

@Command(name = "enrich", separator = "=", description = "Manage and apply dataset enrichments", subcommands = {
        CmdEnrichGTFS.class
})
public class CmdEnrich {

}

