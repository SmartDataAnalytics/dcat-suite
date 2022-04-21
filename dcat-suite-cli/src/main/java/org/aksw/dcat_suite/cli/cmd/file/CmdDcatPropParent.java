package org.aksw.dcat_suite.cli.cmd.file;

import picocli.CommandLine.Command;

@Command(name = "prop", separator = "=", description = "Get or set properties of the local dcat repo", subcommands = {
        CmdDcatPropSet.class,
})
public class CmdDcatPropParent {

}
