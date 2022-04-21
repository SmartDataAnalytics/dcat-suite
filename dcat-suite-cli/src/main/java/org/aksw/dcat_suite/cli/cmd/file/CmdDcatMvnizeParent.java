package org.aksw.dcat_suite.cli.cmd.file;

import picocli.CommandLine.Command;

@Command(name = "mvnize", separator = "=", description = "Maven-based operations", subcommands = {
        CmdDcatMvnizeContent.class,
        CmdDcatMvnizeMetadata.class,
        CmdDcatMvnizeUrls.class
})
public class CmdDcatMvnizeParent {

}
