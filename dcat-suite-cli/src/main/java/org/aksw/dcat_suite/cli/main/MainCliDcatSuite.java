package org.aksw.dcat_suite.cli.main;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import org.aksw.ckan_deploy.core.DcatCkanDeployUtils;
import org.aksw.ckan_deploy.core.DcatCkanRdfUtils;
import org.aksw.ckan_deploy.core.DcatDeployVirtuosoUtils;
import org.aksw.ckan_deploy.core.DcatExpandUtils;
import org.aksw.ckan_deploy.core.DcatInstallUtils;
import org.aksw.ckan_deploy.core.DcatRepository;
import org.aksw.ckan_deploy.core.DcatRepositoryDefault;
import org.aksw.ckan_deploy.core.DcatUtils;
import org.aksw.dcat.jena.domain.api.DcatDataset;
import org.aksw.dcat.repo.api.CatalogResolver;
import org.aksw.dcat.repo.api.DatasetResolver;
import org.aksw.dcat.repo.impl.core.CatalogResolverUtils;
import org.aksw.jena_sparql_api.ext.virtuoso.VirtuosoBulkLoad;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.system.IRIResolver;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.shared.impl.PrefixMappingImpl;
import org.hobbit.core.service.docker.impl.docker_client.DockerServiceSystemDockerClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.google.common.collect.Streams;
import com.spotify.docker.client.DockerClient;

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

		@Parameter(names = { "--ds" ,"--dataset"} , description = "Import a specific datasets (ckan id or name)")
		protected List<String> datasets = new ArrayList<>();

		@Parameter(names="--all", description="Import everything")
		protected boolean all = false;
		
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

		@Parameter(names = { "--ds" ,"--dataset"} , description = "Datasets which to deploy (iri, identifier or title)")
		protected List<String> datasets = new ArrayList<>();

		@Parameter(names = "--port", description = "The URL of the CKAN instance")
		protected int port = 1111;

		@Parameter(names = "--host", description = "Hostname")
		protected String host = "localhost";

		@Parameter(names = "--user", description = "Username")
		protected String user = "dba";
		
		@Parameter(names = "--pass", description = "Password")
		protected String pass = "dba";

		@Parameter(names = "--allowed", description = "A writeable folder readable by virtuoso")
		protected String allowed = ".";

		@Parameter(names = "--docker", description = "Id of a docker container - files will be copied into the container to the folder specified by --allowed")
		protected String docker = null;
		
		@Parameter(names = "--nosymlinks", description = "Copy datsets to the allowed folder instead of linking them")
		protected boolean nosymlinks = false;

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
	
	public static void main(String[] args) throws Exception {

		
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
			Model dcatModel = DcatUtils.createModelWithNormalizedDcatFragment(cmShow.file);
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
				
								
				processCkanImport(ckanClient, cmImportCkan.prefix, datasets);
				
				break;
			}
			default: {
			}
			}

			break;
		}

		case "install": {
			String dcatSource = cmInstall.file;

			Model dcatModel = DcatUtils.createModelWithNormalizedDcatFragment(cmShow.file);
			Function<String, String> iriResolver = createIriResolver(dcatSource);
			DcatRepository dcatRepository = createDcatRepository();
			
			//for(dcatModel.listSubjects(null, DCAT.distribution, null)
			
			for(DcatDataset dcatDataset : DcatUtils.listDcatDatasets(dcatModel)) {
				DcatInstallUtils.install(dcatRepository, dcatDataset, iriResolver, false);
			}
			break;
		}
		
		
		default:
			throw new RuntimeException("Unknown command: " + cmd);

		}
	}
	public static Function<String, String> createIriResolver(String dcatSource) {
		Path dcatPath = Paths.get(dcatSource).toAbsolutePath();
		String baseIRI = dcatPath.getParent().toUri().toString();
		IRIResolver iriResolver = IRIResolver.create(baseIRI);

		return iriResolver::resolveToStringSilent;
	}
	
	public static DcatRepository createDcatRepository() throws IOException 
	{
		String userDirStr = System.getProperty("user.home");
		Path userFolder = Paths.get(userDirStr);
		if(!Files.exists(userFolder)) {
			throw new RuntimeException("Failed to find user directory");
		}
		
		Path repoFolder = userFolder.resolve(".dcat").resolve("repository");
		Files.createDirectories(repoFolder);
		
		DcatRepository result = new DcatRepositoryDefault(repoFolder);

		return result;
	}

	private static void processDeployVirtuoso(CommandDeployVirtuoso cmDeployVirtuoso) throws Exception {
		
		String dcatSource = cmDeployVirtuoso.file;
		
		Function<String, String> iriResolver = createIriResolver(dcatSource);
		CatalogResolver catalogResolver = CatalogResolverUtils.createCatalogResolverDefault();

		Model dcatModel = DcatUtils.createModelWithNormalizedDcatFragment(dcatSource);

		Collection<DcatDataset> dcatDatasets = DcatUtils.listDcatDatasets(dcatModel);
		
		logger.info("Detected datasets:");
		for(DcatDataset dcatDataset: dcatDatasets) {
			logger.info("  " + dcatDataset);
		}
		logger.info(dcatDatasets.size() + " datasets enqueued");
		
		Path allowedFolder = Paths.get(cmDeployVirtuoso.allowed);
		
		String dockerContainerId = cmDeployVirtuoso.docker;
		DockerClient dockerClient = null;
		if(dockerContainerId != null) {
			dockerClient = DockerServiceSystemDockerClient
					.create(true, Collections.emptyMap(), Collections.emptySet())
					.getDockerClient();
		}
		
		VirtuosoDataSource dataSource = new VirtuosoDataSource();
		dataSource.setPassword(cmDeployVirtuoso.pass);
		dataSource.setUser(cmDeployVirtuoso.user);
		dataSource.setPortNumber(cmDeployVirtuoso.port);
		dataSource.setServerName(cmDeployVirtuoso.host);

		try {
			try(Connection conn = dataSource.getConnection()) {
				
				VirtuosoBulkLoad.logEnable(conn, 2, 0);

				for(DcatDataset dcatDataset : dcatDatasets) {
					String datasetId = dcatDataset.getURI();
					DatasetResolver datasetResolver = catalogResolver.resolveDataset(datasetId).blockingGet();
					
					DcatDeployVirtuosoUtils.deploy(
							datasetResolver,
							iriResolver,
							dockerClient,
							dockerContainerId,
							null,
							allowedFolder,
							cmDeployVirtuoso.nosymlinks,
							conn);
				}
			
				conn.commit();
			}
			
			// TODO rollback on error
		} catch(Exception e) {
			throw new RuntimeException(e);
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
	
	
	public static void processCkanImport(CkanClient ckanClient, String prefix, List<String> datasets) {
		

		for (String s : datasets) {
			logger.info("Importing dataset " + s);
			
			CkanDataset ckanDataset = ckanClient.getDataset(s);
			
			PrefixMapping pm = DcatUtils.addPrefixes(new PrefixMappingImpl());

			DcatDataset dcatDataset = DcatCkanRdfUtils.convertToDcat(ckanDataset, pm);

			try {
				// Skolemize the resource first (so we have a reference to the resource)
				dcatDataset = DcatCkanRdfUtils.skolemizeClosureUsingCkanConventions(dcatDataset).as(DcatDataset.class);
				if(prefix != null) {
					dcatDataset = DcatCkanRdfUtils.assignFallbackIris(dcatDataset, prefix).as(DcatDataset.class);
				}
				
			} catch(Exception e) {
				logger.warn("Error processing dataset " + s, e);
			}
			
			RDFDataMgr.write(System.out, dcatDataset.getModel(), RDFFormat.NTRIPLES);
		}
	}
	
}

