package org.aksw.dcat_suite.cli.cmd;

import java.util.concurrent.Callable;

import org.aksw.dcat_suite.cli.main.MainCliDcatSuite;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "integrate", separator = "=", description = "Integrate DCAT descriptions with the Semantic Web")
public class CmdIntegrate
implements Callable<Integer>
{

	@Parameters(description="Path to the DCAT file")
	public String dcatFile;

	@Option(names = { "--ep", "--endpoint"} , description = "The target endpoint for data integration", required=true)
	public String endpoint;
    
    @Option(names= { "--lf", "--linkfile" }, description="The file with the generated links", required=true)
    public String linkFile;
    
    @Option(names= { "--pm", "--propertyMapping" }, description="The path property mapping file", required=true)
    public String propMap;
  
    
    @Override
    public Integer call() throws Exception {
    	CmdIntegrate cmdIntegrate = this;
    	Model dcatModel = ModelFactory.createDefaultModel().read(cmdIntegrate.dcatFile) ;
    	Model linkModel = ModelFactory.createDefaultModel().read(cmdIntegrate.linkFile) ;
    	Model mapModel = ModelFactory.createDefaultModel().read(cmdIntegrate.propMap) ; 
    	RDFConnection conn = RDFConnectionFactory.connect(endpoint);
        
        MainCliDcatSuite.integrate(dcatModel, linkModel, mapModel, conn);
    	
        return 0;
    }

}
