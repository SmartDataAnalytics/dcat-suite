package org.aksw.dcat_suite.cli.cmd;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.aksw.dcat_suite.cli.main.MainCliDcatSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.trentorise.opendata.jackan.CkanClient;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "ckan", separator = "=", description = "Retrieve DCAT descriptions from CKAN")
public class CmdImportCkan
    implements Callable<Integer>
{
    private static final Logger logger = LoggerFactory.getLogger(CmdImportCkan.class);

    @Option(names="--url", description="The URL of the CKAN instance", required=true)
    public String ckanUrl = "http://localhost/ckan";

    @Option(names="--apikey", description="Your API key for the CKAN instance")
    public String apikey;

    @Option(names = { "--ds" ,"--dataset"} , description = "Import a specific datasets (ckan id or name)")
    public List<String> datasets = new ArrayList<>();

    @Option(names="--all", description="Import everything")
    public boolean all = false;

    @Option(names = "--prefix", description = "Allocate URIs using this prefix")
    public String prefix = null;

    @Override
    public Integer call() throws Exception {
        CmdImportCkan cmImportCkan = this;

        CkanClient ckanClient = new CkanClient(cmImportCkan.ckanUrl, cmImportCkan.apikey);

        List<String> datasets;

        if(cmImportCkan.all) {
            if(!cmImportCkan.datasets.isEmpty()) {
                throw new RuntimeException("Options for import all and specific datasets mutually exclusive");
            }

            logger.info("Retrieving the list of all datasets in the catalog");
            datasets = ckanClient.getDatasetList();
        } else {
            if(cmImportCkan.datasets.isEmpty()) {
                throw new RuntimeException("No datasets to import");
            }

            datasets = cmImportCkan.datasets;
        }

        String effectivePrefix = prefix;
        if (effectivePrefix == null) {
            effectivePrefix = ckanUrl.trim();

            if (!(effectivePrefix.endsWith("/") || effectivePrefix.endsWith("#"))) {
                effectivePrefix = effectivePrefix + "/";
            }
        }

        MainCliDcatSuite.processCkanImport(ckanClient, effectivePrefix, datasets);

        return 0;
    }

    // TODO Add arguments to filter datastes

}