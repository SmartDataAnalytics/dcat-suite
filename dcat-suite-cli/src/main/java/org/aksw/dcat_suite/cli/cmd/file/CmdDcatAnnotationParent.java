package org.aksw.dcat_suite.cli.cmd.file;

import picocli.CommandLine.Command;

@Command(name = "annotation", separator = "=", description = "File-based operations", subcommands = {
        // CmdDcatFileTransformAdd.class,
        CmdDcatFileAnnotationApply.class
})
public class CmdDcatAnnotationParent {

}
