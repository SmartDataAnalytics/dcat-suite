package org.aksw.dcat_suite.cli.cmd.file;

import picocli.CommandLine.Command;

@Command(name = "mvn", separator = "=", description = "Maven-based operations", subcommands = {
        CmdDcatMvnGet.class,
        CmdDcatMvnConvert.class
})
public class CmdDcatMvnParent {

}
