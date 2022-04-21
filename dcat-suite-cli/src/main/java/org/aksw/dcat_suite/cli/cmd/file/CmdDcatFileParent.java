package org.aksw.dcat_suite.cli.cmd.file;

import picocli.CommandLine.Command;

@Command(name = "file", separator = "=", description = "File-based operations", subcommands = {
        CmdDcatFileProjectParent.class,
        CmdDcatFileFinalize.class,
        CmdDcatFileTransformParent.class,
        CmdDcatFileProbe.class,
        CmdDcatFileStatus.class
})
public class CmdDcatFileParent {

}
