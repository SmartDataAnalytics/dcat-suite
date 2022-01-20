package org.aksw.dcat_suite.cli.cmd.file;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;

import org.aksw.jenax.arq.dataset.api.ResourceInDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

/** Remove a file from the current catalog */
@Command(name = "rm", separator = "=", description="Remove data about files from the local dcat repository", mixinStandardHelpOptions = true)
public class CmdDcatFileRm
    implements Callable<Integer>
{
    private static final Logger logger = LoggerFactory.getLogger(CmdDcatFileRm.class);

    @Parameters(arity="1..*", description="List of files")
    public List<String> files;


    public Integer call() {
        DcatRepoLocal repo = DcatRepoLocalUtils.findLocalRepo(Path.of(""));

        for (String file : files) {

            Path relPath = DcatRepoLocalUtils.normalizeRelPath(repo.getBasePath(), Path.of(file));

            ResourceInDataset status = DcatRepoLocalUtils.getFileStatus(repo, relPath);
            if (status != null) {
                logger.info("Removing status of " + status.getGraphName());
                status.getModel().removeAll();
            } else {
                logger.info("No status available for: " + relPath);
            }

        }

        return 0;
    }
}
