package org.aksw.dcat_suite.cli.cmd;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.aksw.dcat_suite.cli.main.MainCliDcatSuite;

import com.google.common.base.StandardSystemProperty;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "virtuoso", description = "Deploy datasets to a local Virtuoso via OBDC")
public class CmdDeployVirtuoso
    implements Callable<Integer>
{
    @Parameters(description = "The DCAT file which to deploy") //, required = true)
    public String file;

    @Option(names = { "--ds" ,"--dataset"} , description = "Datasets which to deploy (iri, identifier or title)")
    public List<String> datasets = new ArrayList<>();

    @Option(names = "--port", description = "Virtuoso's ODBC port")
    public int port = 1111;

    @Option(names = "--host", description = "Hostname")
    public String host = null; //"localhost";

    @Option(names = "--user", description = "Username")
    public String user = "dba";

    @Option(names = "--pass", description = "Password")
    public String pass = "dba";

    @Option(names = "--allowed", description = "A writeable folder readable by virtuoso")
    public String allowed = ".";

    @Option(names = "--docker", description = "Id of a docker container - files will be copied into the container to the folder specified by --allowed")
    public String docker = null;

    @Option(names = "--nosymlinks", description = "Copy datsets to the allowed folder instead of linking them")
    public boolean nosymlinks = false;

    @Option(names = "--tmp", description = "Temporary directory for e.g. unzipping large files")
    public String tmpFolder = StandardSystemProperty.JAVA_IO_TMPDIR.value() + "/dcat/";

    @Override
    public Integer call() throws Exception {
        CmdDeployVirtuoso cmDeployVirtuoso = this;
        MainCliDcatSuite.processDeployVirtuoso(cmDeployVirtuoso);
        return 0;
    }

}