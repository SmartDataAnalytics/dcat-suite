package org.aksw.dcat_suite.cli.cmd.file;

import picocli.CommandLine.Command;

@Command(name = "file", separator = "=", description = "File-based operations", subcommands = {
        CmdDcatFileProbe.class,
})
public class CmdDcatFileParent {

}
