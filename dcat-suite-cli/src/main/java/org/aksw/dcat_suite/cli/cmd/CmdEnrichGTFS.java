package org.aksw.dcat_suite.cli.cmd;

import java.io.File;
import java.io.FileInputStream;
import java.util.concurrent.Callable;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.aksw.dcat_suite.cli.main.MainCliDcatSuite;
import org.aksw.dcat_suite.enrich.GTFSFile;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "gtfs", separator = "=", description = "Generate and enrich DCAT descriptions from GTFS files")
public class CmdEnrichGTFS 
implements Callable<Integer>
{

	@Option(names="--folder", description="Path to the GTFS folder that is to be enriched with DCAT metadata", required=true)
	public String gtfsFile;

	@Option(names = { "--ns" ,"--namespace"} , description = "The namespace of the DCAT dataset description", required=true)
	public String prefix;
	
    @Option(names="--title", description = "The title of the dataset", required=true)
    public String dsTitle;
    
    // TODO: add prefix
    public Integer call() throws Exception {
       // GTFSFile gtfsFile = new GTFSFile(this.gtfsFile);
        
        
        String effectivePrefix = this.prefix;
        if (!(effectivePrefix.endsWith("/") || effectivePrefix.endsWith("#"))) {
        	effectivePrefix = effectivePrefix + "/";
        }
        
        MainCliDcatSuite.processEnrichGTFS(this.gtfsFile, this.dsTitle, effectivePrefix);
        return 0;
    }

}
