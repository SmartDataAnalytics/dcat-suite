package org.aksw.dcat_suite.cli.cmd.file;

import java.util.concurrent.Callable;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * Initialize a dcat repository in a directory.
 *
 */
@Command(name = "init", separator = "=", description="Initialize a dcat repository in a directory", mixinStandardHelpOptions = true)
public class CmdDcatFileInit
    implements Callable<Integer>
{

    /** Default group id can be read from the dcat repo conf file */
    @Option(names= {"-g", "--groupId"}, description = "Default group id to assign to files if no other group id is specified")
    public String groupId;

    /** Version for the file being added - if absent use its modified date */
    @Option(names= {"-v", "--version"}, description = "Default version to assign to files. If unspecified then their modified timestamp is used by default")
    public String version;


    @Override
    public Integer call() throws Exception {
        DcatRepoLocalUtils.init();
        return 0;
    }

}
