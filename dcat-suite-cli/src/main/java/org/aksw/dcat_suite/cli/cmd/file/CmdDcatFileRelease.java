package org.aksw.dcat_suite.cli.cmd.file;

import java.nio.file.Path;
import java.util.concurrent.Callable;

import org.apache.jena.query.Dataset;

import picocli.CommandLine.Command;

/**
 * Process a local dcat catalog in order to finalize identifiers.
 * Non-download file IRIs will be substituted with distribution identifiers.
 * Substitution will also be applied to all files marked with '<> substitution true'.
 *
 *
 * @author raven
 *
 */
@Command(name = "release", separator = "=", description="Create a release from a local dcat repository", mixinStandardHelpOptions = true)
public class CmdDcatFileRelease
    implements Callable<Integer>
{

    @Override
    public Integer call() throws Exception {
        DcatRepoLocal repo = DcatRepoLocalUtils.findLocalRepo(Path.of(""));

        Dataset repoDataset = repo.getDataset();





        return 0;
    }

}
