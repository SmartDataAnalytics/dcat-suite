package org.aksw.dcat_suite.cli.cmd;

import picocli.CommandLine.Command;

@Command(name = "transform", separator = "=", description = "Manage and apply dataset transformations", subcommands = {
        CmdTransformCreate.class,
        CmdTransformApply.class
})
public class CmdTransform {

}
