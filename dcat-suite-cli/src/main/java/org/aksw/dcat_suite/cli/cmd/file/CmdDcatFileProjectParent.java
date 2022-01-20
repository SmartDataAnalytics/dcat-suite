package org.aksw.dcat_suite.cli.cmd.file;

import picocli.CommandLine.Command;

@Command(name = "project", separator = "=", description = "File-based project operations", subcommands = {
        CmdDcatFileProjectAdd.class,
})
public class CmdDcatFileProjectParent {

}
