package org.aksw.dcat_suite.cli.cmd.file;

import picocli.CommandLine.Command;

@Command(name = "mvn", separator = "=", description = "Maven-based operations", subcommands = {
        CmdDcatFileMvnContent.class,
        CmdDcatFileMvnMetadata.class,
        CmdDcatFileMvnizeMetadata.class
})
public class CmdDcatFileMvnParent {

}
