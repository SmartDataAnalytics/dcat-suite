package org.aksw.dcat_suite.cli.cmd;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.aksw.dcat_suite.cli.main.MainCliDcatSuite;
import org.aksw.dcat_suite.clients.DkanClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.trentorise.opendata.jackan.CkanClient;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "dkan", separator = "=", description = "Retrieve DCAT descriptions from DKAN")
public class CmdImportDkan
    implements Callable<Integer>
{
    private static final Logger logger = LoggerFactory.getLogger(CmdImportDkan.class);
    
    @Option(names="--url", description="The URL of the DKAN instance", required=true)
    public String dkanUrl = "http://localhost/dkan";

    @Option(names = { "--ds" ,"--dataset"} , description = "Import a specific datasets (ckan id or name)")
    public List<String> datasets = new ArrayList<>();

    @Option(names="--all", description="Import everything")
    public boolean all = false;
    
    @Option(names = "--prefix", description = "Allocate URIs using this prefix")
    public String prefix = null;

    @Override
    public Integer call() throws Exception {
        CmdImportDkan cmImportDkan = this;
        
        DkanClient dkanClient = new DkanClient(cmImportDkan.dkanUrl);

        List<String> datasets;

        if(cmImportDkan.all) {
            if(!cmImportDkan.datasets.isEmpty()) {
                throw new RuntimeException("Options for import all and specific datasets mutually exclusive");
            }

            logger.info("Retrieving the list of all datasets in the catalog");
            datasets = dkanClient.getDatasetList();
        } 
        else {
           if(cmImportDkan.datasets.isEmpty()) {
                throw new RuntimeException("No datasets to import");
            }

            datasets = cmImportDkan.datasets;
        }

        //CkanClient ckanClient = new CkanClient(cmImportDkan.dkanUrl, cmImportDkan.apikey);
        String effectivePrefix = prefix;
        if (effectivePrefix == null) {
            effectivePrefix = dkanUrl.trim();

            if (!(effectivePrefix.endsWith("/") || effectivePrefix.endsWith("#"))) {
                effectivePrefix = effectivePrefix + "/";
            }
        }

        MainCliDcatSuite.processDkanImport(dkanClient, effectivePrefix, datasets);

        return 0;
    }

}