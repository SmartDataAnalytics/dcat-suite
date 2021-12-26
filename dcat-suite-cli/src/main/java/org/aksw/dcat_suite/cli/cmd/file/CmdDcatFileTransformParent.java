package org.aksw.dcat_suite.cli.cmd.file;

import picocli.CommandLine.Command;

@Command(name = "transform", separator = "=", description = "File-based operations", subcommands = {
        // CmdDcatFileTransformAdd.class,
        CmdDcatFileTransformApply.class
})
public class CmdDcatFileTransformParent {

}
