package org.aksw.dcat_suite.cli.cmd.file;

import java.util.concurrent.Callable;

import org.aksw.commons.io.util.StdIo;
import org.aksw.jenax.stmt.core.SparqlStmtMgr;
import org.apache.jena.query.Dataset;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.update.UpdateExecutionFactory;
import org.apache.jena.update.UpdateRequest;

import picocli.CommandLine.Command;

/**
 * Replace all downloadUrls pointing to files with corresponding <urn:mvn:> ids.
 *
 *
 *
 * @author raven
 *
 */
@Command(name = "urls", separator = "=", description="Convert local (download) URLs to maven URNs", mixinStandardHelpOptions = true)
public class CmdDcatMvnizeUrls
    implements Callable<Integer>
{
    @Override
    public Integer call() throws Exception {
        DcatRepoLocal repo = DcatRepoLocalUtils.findLocalRepo();
        Dataset mem = repo.getMemDataset();

        mem = CmdDcatFileFinalize.makeDatasetCentric(mem);

        UpdateRequest ur = SparqlStmtMgr.loadSparqlStmts("dcat-download-to-mvn.rq").get(0).getAsUpdateStmt().getUpdateRequest();

        UpdateExecutionFactory.create(ur, mem).execute();

        RDFDataMgr.write(StdIo.openStdOutWithCloseShield(), mem, RDFFormat.TRIG_PRETTY);

        return 0;
    }

}
