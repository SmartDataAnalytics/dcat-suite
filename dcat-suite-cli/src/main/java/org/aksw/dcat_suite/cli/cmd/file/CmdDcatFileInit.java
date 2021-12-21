package org.aksw.dcat_suite.cli.cmd.file;

import java.util.concurrent.Callable;

import picocli.CommandLine.Command;

/**
 * Initialize a dcat repository in a directory.
 *
 */
@Command(name = "init", separator = "=", description="Initialize a dcat repository in a directory", mixinStandardHelpOptions = true)
public class CmdDcatFileInit
    implements Callable<Integer>
{

    @Override
    public Integer call() throws Exception {
        DcatRepoLocalUtils.init();
        return 0;
    }

}
