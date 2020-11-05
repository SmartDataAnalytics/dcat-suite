package org.aksw.dcat_suite.cli.cmd;

import java.util.concurrent.Callable;

import org.aksw.dcat_suite.cli.main.MainCliDcatSuite;

import eu.trentorise.opendata.jackan.CkanClient;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "ckan", separator = "=", description = "Deploy DCAT datasets to CKAN")
public class CmdDeployCkan
    implements Callable<Integer>
{

    @Parameters(description = "The DCAT file which to deploy", arity="1")
    public String file;

    @Option(names = "--url", description = "The URL of the CKAN instance")
    public String ckanUrl = "http://localhost/ckan";

    @Option(names = {"-o", "--org", "--orga", "--organization"}, description = "The ID or name of the organization into which to upload (matched in this order).")
    public String organization = null;

    @Option(names = {"--no-group-map"}, description = "Disable mapping organization by group attribute.")
    public boolean noMapByGroup = false;

    @Option(names = "--apikey", description = "Your API key for the CKAN instance")
    public String apikey;

    @Option(names = "--noupload", description = "Disable file upload")
    public boolean noupload = false;

    @Override
    public Integer call() throws Exception {
        CmdDeployCkan cmDeployCkan = this;

        CkanClient ckanClient = new CkanClient(cmDeployCkan.ckanUrl, cmDeployCkan.apikey);
        MainCliDcatSuite.processDeploy(ckanClient, cmDeployCkan.file, cmDeployCkan.noupload, !cmDeployCkan.noMapByGroup, cmDeployCkan.organization);

        return 0;
    }

}