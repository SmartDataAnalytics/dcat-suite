package org.aksw.dcat_suite.cli.cmd.file;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.aksw.jenax.arq.dataset.impl.DatasetOneNgImpl;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;

import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = "status", separator = "=", description="Show local/remote DCAT status", mixinStandardHelpOptions = true)
public class CmdDcatFileStatus
    implements Callable<Integer>
{
    @Parameters(description = "Files for which to show status information")
    public List<String> files = new ArrayList<>();

    public Integer call() {
        DcatRepoLocal repo = DcatRepoLocalUtils.findLocalRepo();
        Dataset repoDs = repo.getDataset();
        repoDs.begin(ReadWrite.READ);


        Path basePath = repo.getBasePath();
        for (String file : files) {
            Path relPath = DcatRepoLocalUtils.normalizeRelPath(basePath, file);

            Dataset ds = DatasetOneNgImpl.create(repoDs, relPath.toString());
            RDFDataMgr.write(System.out, ds, RDFFormat.TRIG_PRETTY);
        }

        repoDs.commit();

        return 0;
    }

}
