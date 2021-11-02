package org.aksw.dcat_suite.cli.cmd;

import java.util.concurrent.Callable;

import org.aksw.dcat_suite.cli.main.MainCliDcatSuite;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "gtfs", separator = "=", description = "Generate and enrich DCAT descriptions from GTFS files")
public class CmdEnrichGTFS 
implements Callable<Integer>
{

	@Option(names="--folder", description="Path to the GTFS folder that is to be enriched with DCAT metadata", required=true)
	public String gtfsFile;

	@Option(names = { "--ns", "--namespace"} , description = "The namespace of the DCAT dataset description", required=true)
	public String prefix;
	
    @Option(names="--title", description = "The title of the dataset", required=true)
    public String dsTitle;
    
    @Option(names= { "--gtfs-type", "--gt" }, split = ",", description="The gtfs type that should enrich a dataset", required=false)
    public String [] gtfsType = null;
    
    public Integer call() throws Exception {
    CmdEnrichGTFS cmdEnrichGTFS = this;
        
        String effectivePrefix = this.prefix;
        if (!(effectivePrefix.endsWith("/") || effectivePrefix.endsWith("#"))) {
        	effectivePrefix = effectivePrefix + "/";
        }
        
        MainCliDcatSuite.processEnrichGTFS(cmdEnrichGTFS.gtfsFile, cmdEnrichGTFS.dsTitle, effectivePrefix, 
        		cmdEnrichGTFS.gtfsType);
        return 0;
    }

}
