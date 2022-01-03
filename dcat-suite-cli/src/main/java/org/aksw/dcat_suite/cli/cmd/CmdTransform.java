package org.aksw.dcat_suite.cli.cmd;

import picocli.CommandLine.Command;

@Command(name = "transform", separator = "=", description = "Manage and apply dataset transformations", subcommands = {
        CmdDcatTransformCreate.class,
        CmdTransformApply.class
})
public class CmdTransform {

}
