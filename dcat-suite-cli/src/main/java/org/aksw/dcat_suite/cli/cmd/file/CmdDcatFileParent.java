package org.aksw.dcat_suite.cli.cmd.file;

import picocli.CommandLine.Command;

@Command(name = "file", separator = "=", description = "File-based operations", subcommands = {
        CmdDcatFileInit.class,
        CmdDcatFileAdd.class,
        CmdDcatFileTransformParent.class,
        CmdDcatFileProbe.class
})
public class CmdDcatFileParent {

}
