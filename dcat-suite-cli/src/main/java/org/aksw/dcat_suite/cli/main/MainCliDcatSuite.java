package org.aksw.dcat_suite.cli.main;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.List;

import org.aksw.ckan_deploy.core.DcatCkanDeployUtils;
import org.aksw.ckan_deploy.core.DcatCkanRdfUtils;
import org.aksw.ckan_deploy.core.DcatDeployVirtuosoUtils;
import org.aksw.ckan_deploy.core.DcatExpandUtils;
import org.aksw.ckan_deploy.core.DcatInstallUtils;
import org.aksw.ckan_deploy.core.DcatUtils;
import org.aksw.dcat.jena.domain.api.DcatDataset;
import org.aksw.jena_sparql_api.ext.virtuoso.VirtuosoBulkLoad;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.system.IRIResolver;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.shared.impl.PrefixMappingImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.google.common.collect.Streams;

import eu.trentorise.opendata.jackan.CkanClient;
import eu.trentorise.opendata.jackan.model.CkanDataset;
import eu.trentorise.opendata.jackan.model.CkanResource;
import virtuoso.jdbc4.VirtuosoDataSource;

public class MainCliDcatSuite {

	
	private static final Logger logger = LoggerFactory.getLogger(MainCliDcatSuite.class);

	
	@Parameters(separators = "=", commandDescription = "Show DCAT information")
	public static class CommandMain {
		@Parameter(description = "Non option args")
		protected List<String> nonOptionArgs;

		@Parameter(names = "--help", help = true)
		protected boolean help = false;
	}

	@Parameters(separators = "=", commandDescription = "Show DCAT information")
	public static class CommandShow {

		@Parameter(description = "Any RDF file")
		protected String file;
	}

	@Parameters(separators = "=", commandDescription = "Download datasets to local repository based on DCAT information")
	public static class CommandInstall {
		@Parameter(description = "A DCAT file")
		protected String file;
	}

	
	@Parameters(separators = "=", commandDescription = "Expand quad datasets")
	public static class CommandExpand {

		@Parameter(description = "Quad-based RDF dataset")
		protected String file;
	}

	@Parameters(separators = "=", commandDescription = "Deploy DCAT datasets")
	public static class CommandDeploy {
	}

	@Parameters(separators = "=", commandDescription = "Retrieve DCAT descriptions")
	public static class CommandImport {
	}

	@Parameters(separators = "=", commandDescription = "Retrieve DCAT descriptions from CKAN")
	public static class CommandImportCkan {

		@Parameter(names="--host", description="The URL of the CKAN instance", required=true)
		protected String host;

		@Parameter(names="--apikey", description="Your API key for the CKAN instance")
		protected String apikey;
		
		
		@Parameter(names = "--prefix", description = "Allocate URIs using this prefix")
		protected String prefix;

		// TODO Add arguments to filter datastes
	
	}

	@Parameters(separators = "=", commandDescription = "Deploy DCAT datasets")
	public static class CommandDeployCkan {

		@Parameter(description = "The DCAT file which to deploy", required = true)
		protected String file;

		@Parameter(names = "--host", description = "The URL of the CKAN instance")
		protected String host;

		@Parameter(names = "--apikey", description = "Your API key for the CKAN instance")
		protected String apikey;
		
		@Parameter(names = "--noupload", description = "Disable file upload")
		protected boolean noupload = false;
		
	}

	@Parameters(separators = "=", commandDescription = "Deploy datasets to a local Virtuoso via OBDC")
	public static class CommandDeployVirtuoso {

		@Parameter(description = "The DCAT file which to deploy", required = true)
		protected String file;

		@Parameter(names = "--port", description = "The URL of the CKAN instance")
		protected int port = 1111;

		@Parameter(names = "--user", description = "Username")
		protected String user = "dba";
		
		@Parameter(names = "--pass", description = "Password")
		protected String pass = "dba";

		@Parameter(names = "--allowed", description = "A folder which virtuoso is allowed to access")
		protected String allowed = ".";

	}

	public static void showCkanDatasets(CkanClient ckanClient) {
		List<String> ds = ckanClient.getDatasetList(10, 0);

		for (String s : ds) {
			System.out.println();
			System.out.println("DATASET: " + s);
			CkanDataset d = ckanClient.getDataset(s);
			System.out.println(" RESOURCES:");
			for (CkanResource r : d.getResources()) {
				System.out.println(" " + r.getName());
				System.out.println(" FORMAT: " + r.getMimetype());
				System.out.println(" FORMAT: " + r.getMimetypeInner());
				System.out.println(" FORMAT: " + r.getFormat());
				System.out.println(" URL: " + r.getUrl());
			}
		}

	}
	
	public static void main(String[] args) throws IOException {

		
		CommandMain cm = new CommandMain();
		CommandShow cmShow = new CommandShow();
		CommandExpand cmExpand = new CommandExpand();
		CommandDeploy cmDeploy = new CommandDeploy();		
		CommandImport cmImport = new CommandImport();
		CommandInstall cmInstall = new CommandInstall();

		
		// CommandCommit commit = new CommandCommit();
		JCommander jc = JCommander.newBuilder()
				.addObject(cm)
				.addCommand("show", cmShow)
				.addCommand("expand", cmExpand)
				.addCommand("deploy", cmDeploy)
				.addCommand("import", cmImport)
				.addCommand("install", cmInstall)
				.build();

		JCommander deploySubCommands = jc.getCommands().get("deploy");

		CommandDeployCkan cmDeployCkan = new CommandDeployCkan();
		deploySubCommands.addCommand("ckan", cmDeployCkan);
		
		CommandDeployVirtuoso cmDeployVirtuoso = new CommandDeployVirtuoso();
		deploySubCommands.addCommand("virtuoso", cmDeployVirtuoso);

		
		JCommander importSubCommands = jc.getCommands().get("import");

		CommandImportCkan cmImportCkan = new CommandImportCkan();
		importSubCommands.addCommand("ckan", cmImportCkan);

		
		jc.parse(args);

        if (cm.help) {
            jc.usage();
            return;
        }

        // TODO Change this to a plugin system - for now I hack this in statically
		String cmd = jc.getParsedCommand();
		switch (cmd) {
		case "show": {
			Dataset dataset = RDFDataMgr.loadDataset(cmShow.file);
			Model dcatModel = DcatUtils.addPrefixes(DcatUtils.createModelWithDcatFragment(dataset));
			RDFDataMgr.write(System.out, dcatModel, RDFFormat.TURTLE_PRETTY);
			break;
		}
		case "expand": {
			processExpand(cmExpand.file);
			break;
		}
		case "deploy": {
			String deployCmd = deploySubCommands.getParsedCommand();
			switch(deployCmd) {
			case "ckan": {
				CkanClient ckanClient = new CkanClient(cmDeployCkan.host, cmDeployCkan.apikey);
				//showCkanDatasets(ckanClient);
				//if(false) {
				processDeploy(ckanClient, cmDeployCkan.file, cmDeployCkan.noupload);
				//}
				break;
			}
			case "virtuoso": {				
				processDeployVirtuoso(cmDeployVirtuoso);
				break;
			}
			default: {
				throw new RuntimeException("Unknow deploy command: " + deployCmd);
			}
			}
			break;
		}
		
		case "import": {
			String importCmd = importSubCommands.getParsedCommand();
			switch(importCmd) {
			case "ckan": {
				CkanClient ckanClient = new CkanClient(cmImportCkan.host, cmImportCkan.apikey);

				processCkanImport(ckanClient, cmImportCkan.prefix);
				
				break;
			}
			default: {
			}
			}

			break;
		}

		case "install": {
			String dcatSource = cmInstall.file;
			Model dcatModel = RDFDataMgr.loadModel(dcatSource);
			//for(dcatModel.listSubjects(null, DCAT.distribution, null)
			String userDirStr = System.getProperty("user.home");
			Path userFolder = Paths.get(userDirStr);
			if(!Files.exists(userFolder)) {
				throw new RuntimeException("Failed to find user directory");
			}
			
			Path repoFolder = userFolder.resolve(".dcat").resolve("repository");
			Files.createDirectories(repoFolder);
			
			for(DcatDataset dcatDataset : DcatUtils.listDcatDatasets(dcatModel)) {
				DcatInstallUtils.install(repoFolder, dcatDataset, false);
			}
			break;
		}
		
		
		default:
			throw new RuntimeException("Unknown command: " + cmd);

		}
	}

	private static void processDeployVirtuoso(CommandDeployVirtuoso cmDeployVirtuoso) {
		
		String dcatSource = cmDeployVirtuoso.file;
		
		Dataset dataset = RDFDataMgr.loadDataset(dcatSource);
		Path dcatPath = Paths.get(dcatSource).toAbsolutePath();

		String baseIRI = dcatPath.getParent().toUri().toString();
		IRIResolver iriResolver = IRIResolver.create(baseIRI);

		
		Model dcatModel = RDFDataMgr.loadModel(dcatSource);

		Path allowedFolder = Paths.get(cmDeployVirtuoso.allowed);
		
		VirtuosoDataSource dataSource = new VirtuosoDataSource();
		dataSource.setPassword(cmDeployVirtuoso.pass);
		dataSource.setUser(cmDeployVirtuoso.user);
		dataSource.setPortNumber(cmDeployVirtuoso.port);
		dataSource.setServerName("localhost");

		try {
			try(Connection conn = dataSource.getConnection()) {
				
				VirtuosoBulkLoad.logEnable(conn, 2, 0);

				for(DcatDataset dcatDataset : DcatUtils.listDcatDatasets(dcatModel)) {
				
					DcatDeployVirtuosoUtils.deploy(dcatDataset, iriResolver, allowedFolder, conn);
				}
			
				conn.commit();
			}
			
			// TODO rollback on error
		} catch(Exception e ) {
			//conn.rollback();
		}
	}

	public static Path processExpand(String dcatSource) throws IOException {
		Dataset dataset = RDFDataMgr.loadDataset(dcatSource);
		Path dcatPath = Paths.get(dcatSource).toAbsolutePath();
		Path targetFolder = dcatPath.getParent().resolve("target").resolve("dcat");
		Files.createDirectories(targetFolder);

		Model model = DcatExpandUtils.export(dataset, targetFolder);
		Path result = targetFolder.resolve("dcat.nt");
		DcatExpandUtils.writeSortedNtriples(model, result);
		return result;
	}

	public static void processDeploy(CkanClient ckanClient, String dcatSource, boolean noFileUpload) throws IOException {
		Dataset dataset = RDFDataMgr.loadDataset(dcatSource);
		Path dcatPath = Paths.get(dcatSource).toAbsolutePath();
		Path targetFolder = dcatPath.getParent().resolve("target").resolve("dcat");
		Files.createDirectories(targetFolder);

		String baseIRI = targetFolder.toUri().toString();
		IRIResolver iriResolver = IRIResolver.create(baseIRI);
		
		// If the dataset has named graphs, we perform export
		boolean hasNamedGraphs = Streams.stream(dataset.listNames()).findFirst().isPresent();

		Model exportDcatModel;
		if (hasNamedGraphs) {
			exportDcatModel = DcatExpandUtils.export(dataset, targetFolder);
			DcatExpandUtils.writeSortedNtriples(exportDcatModel, targetFolder.resolve("dcat.nt"));
		} else {
			exportDcatModel = dataset.getDefaultModel();
		}
		

		Model deployDcatModel = DcatCkanDeployUtils.deploy(ckanClient, exportDcatModel, iriResolver, noFileUpload);
		DcatExpandUtils.writeSortedNtriples(deployDcatModel, targetFolder.resolve("deploy-dcat.nt"));
	}
	
	
	public static void processCkanImport(CkanClient ckanClient, String prefix) {
		
		//String host = ckanClient.getCatalogUrl();
		//String host = "http://localhost/dataset/";

		List<String> ds = ckanClient.getDatasetList();

		for (String s : ds) {
			logger.info("Importing dataset " + s);
			
			CkanDataset ckanDataset = ckanClient.getDataset(s);
			
			PrefixMapping pm = DcatUtils.addPrefixes(new PrefixMappingImpl());

			DcatDataset dcatDataset = DcatCkanRdfUtils.convertToDcat(ckanDataset, pm);

			try {
				DcatCkanRdfUtils.skolemize(dcatDataset.getModel());
				//DcatCkanRdfUtils.assignDefaultIris(dcatDataset);
				if(prefix != null) {
					DcatCkanRdfUtils.assignFallbackIris(dcatDataset, prefix);
				}
			} catch(Exception e) {
				logger.warn("Error processing dataset " + s, e);
			}
			
			RDFDataMgr.write(System.out, dcatDataset.getModel(), RDFFormat.NTRIPLES);
		}
	}
	
}

