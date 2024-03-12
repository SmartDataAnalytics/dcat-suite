package org.aksw.dcat_suite.cli.cmd.file;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import org.aksw.commons.io.util.StdIo;
import org.aksw.jena_sparql_api.http.domain.api.RdfEntityInfo;
import org.aksw.jenax.arq.dataset.api.ResourceInDataset;
import org.aksw.jenax.arq.dataset.impl.ResourceInDatasetImpl;
import org.aksw.jenax.sparql.query.rx.RDFDataMgrEx;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.util.ResourceUtils;

import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = "probe", separator = "=", description="Attempt to detect a file's encodings and content type and emit it as an rdf model", mixinStandardHelpOptions = true)
public class CmdDcatFileProbe
    implements Callable<Integer>
{
    @Parameters(arity="1..*", description="List of files")
    public List<String> files;


    public Integer call() throws IOException {
        Path basePath = Path.of("").toAbsolutePath();

        for (String file : files) {
            Path relPath = Path.of(file);

            RdfEntityInfo tmp = DcatRepoLocalUtils.probeFile(basePath, relPath);
            ResourceInDataset r = ResourceInDatasetImpl.createFromCopyIntoResourceGraph(tmp);
            RDFDataMgr.write(StdIo.openStdOutWithCloseShield(), r.getDataset(), RDFFormat.TRIG_BLOCKS);
        }

        return 0;
    }
}
