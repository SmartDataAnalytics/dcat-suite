package org.aksw.dcat_suite.cli.cmd;

import org.aksw.dcat_suite.cli.cmd.file.CmdDcatFileTransformAdd;
import org.aksw.dcat_suite.cli.cmd.file.CmdDcatFileTransformApply;

import picocli.CommandLine.Command;

@Command(name = "transform", separator = "=", description = "Manage and apply dataset transformations", subcommands = {
        CmdDcatTransformCreate.class,
        CmdDcatFileTransformApply.class
        // CmdTransformApply.class
})
public class CmdTransform {

}
