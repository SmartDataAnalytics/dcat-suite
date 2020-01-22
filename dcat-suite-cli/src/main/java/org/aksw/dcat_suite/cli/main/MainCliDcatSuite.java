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
import java.util.stream.Collectors;

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
import org.aksw.dcat.repo.impl.cache.CatalogResolverCaching;
import org.aksw.dcat.repo.impl.core.CatalogResolverUtils;
import org.aksw.dcat.repo.impl.fs.CatalogResolverMulti;
import org.aksw.dcat.repo.impl.model.CatalogResolverModel;
import org.aksw.dcat.repo.impl.model.CatalogResolverSparql;
import org.aksw.dcat.repo.impl.model.SearchResult;
import org.aksw.dcat.server.controller.ControllerLookup;
import org.aksw.jena_sparql_api.conjure.utils.ContentTypeUtils;
import org.aksw.jena_sparql_api.ext.virtuoso.VirtuosoBulkLoad;
import org.aksw.jena_sparql_api.http.domain.api.RdfEntityInfo;
import org.aksw.jena_sparql_api.http.repository.api.RdfHttpEntityFile;
import org.aksw.jena_sparql_api.http.repository.impl.HttpResourceRepositoryFromFileSystemImpl;
import org.apache.commons.collections.CollectionUtils;
import org.apache.http.HttpRequest;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.Query;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.rdfconnection.SparqlQueryConnection;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.system.IRIResolver;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.shared.impl.PrefixMappingImpl;
import org.apache.jena.sparql.lang.arq.ParseException;
import org.hobbit.core.service.docker.impl.docker_client.DockerServiceSystemDockerClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.google.common.base.StandardSystemProperty;
import com.google.common.base.Strings;
import com.google.common.collect.Streams;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.messages.ContainerInfo;

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
	
	@Parameters(separators = "=", commandDescription = "Search DCAT catalogs")
	public static class CommandSearch {
		@Parameter(description = "Search pattern (regex)")
		protected List<String> nonOptionArgs;

//		// ArtifactID - can refer to any dataset, distribution, download
//		protected String artifactId;
		@Parameter(names={"-c", "--catalog"}, description = "Catalog reference")
		protected List<String> catalogs = Collections.emptyList();

		@Parameter(names = "--help", help = true)
		protected boolean help = false;
	}


	@Parameters(separators = "=", commandDescription = "Show data")
	public static class CommandData {
		@Parameter(description = "Non option args")
		protected List<String> nonOptionArgs;

//		// ArtifactID - can refer to any dataset, distribution, download
//		protected String artifactId;
		@Parameter(names={"-c", "--catalog"}, description = "Catalog reference")
		protected List<String> catalogs = Collections.emptyList();
		
		// Note: format is more generic than content-type as csv or rdf.gzip are valid formats
		// So a format is any string from which content type and encoding can be inferred
		@Parameter(names={"-f", "--format"}, description = "Preferred format / content type")
		protected String contentType = "text/turtle";
		
		@Parameter(names={"-e", "--encoding"}, description = "Preferred encoding(s)")
		protected List<String> encodings = Collections.emptyList();

		@Parameter(names={"-l", "--link"}, description = "Instead of returning the content directly, return a file url in the cache")
		protected boolean link = false;

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

		@Parameter(names="--url", description="The URL of the CKAN instance", required=true)
		protected String ckanUrl = "http://localhost/ckan";

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

		@Parameter(names = "--url", description = "The URL of the CKAN instance")
		protected String ckanUrl = "http://localhost/ckan";

		@Parameter(names = "--apikey", description = "Your API key for the CKAN instance")
		protected String apikey;
		
		@Parameter(names = "--noupload", description = "Disable file upload")
		protected boolean noupload = false;
		
	}

	@Parameters(separators = "=", commandDescription = "Deploy datasets to a local Virtuoso via OBDC")
	public static class CommandDeployVirtuoso {

		@Parameter(description = "The DCAT file which to deploy") //, required = true)
		protected String file;

		@Parameter(names = { "--ds" ,"--dataset"} , description = "Datasets which to deploy (iri, identifier or title)")
		protected List<String> datasets = new ArrayList<>();

		@Parameter(names = "--port", description = "Virtuoso's ODBC port")
		protected int port = 1111;

		@Parameter(names = "--host", description = "Hostname")
		protected String host = null; //"localhost";

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

		@Parameter(names = "--tmp", description = "Temporary directory for e.g. unzipping large files")
		protected String tmpFolder = StandardSystemProperty.JAVA_IO_TMPDIR.value() + "/dcat/";

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
	
	public static CatalogResolver createEffectiveCatalogResolver(List<String> catalogs) throws IOException, ParseException {

		List<CatalogResolver> catalogResolvers = new ArrayList<>();

		CatalogResolver defaultResolver = CatalogResolverUtils.createCatalogResolverDefault();
		catalogResolvers.add(defaultResolver);
		//catalogResolvers.addAll((defaultResolver));
		
		for(String catalog : catalogs) {
			Model model = RDFDataMgr.loadModel(catalog);
			
			logger.info("Loaded " + model.size() + " triples for catalog at " + catalog);
			
			RDFConnection conn = RDFConnectionFactory.connect(DatasetFactory.wrap(model));
			catalogResolvers.add(CatalogResolverUtils.createCatalogResolver(conn));
			//catalogResolvers.add(new CatalogResolverModel(model));
		}
		
		CatalogResolver result = CatalogResolverMulti.wrapIfNeeded(catalogResolvers);

		return result;
	}
	
	
	public static void main(String[] args) throws Exception {

		
		CommandMain cm = new CommandMain();
		CommandSearch cmSearch = new CommandSearch();	
		CommandData cmData = new CommandData();		
		CommandShow cmShow = new CommandShow();
		CommandExpand cmExpand = new CommandExpand();
		CommandDeploy cmDeploy = new CommandDeploy();		
		CommandImport cmImport = new CommandImport();
		CommandInstall cmInstall = new CommandInstall();

		
		// CommandCommit commit = new CommandCommit();
		JCommander jc = JCommander.newBuilder()
				.addObject(cm)
				.addCommand("search", cmSearch)
				.addCommand("data", cmData)
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
		case "search": {
			List<String> noas = cmSearch.nonOptionArgs;
			if(noas.size() != 1) {
				throw new RuntimeException("Only one non-option argument expected for the artifact id");
			}
			String pattern = noas.get(0);

			CatalogResolver effectiveCatalogResolver = createEffectiveCatalogResolver(cmSearch.catalogs);			
			searchDcat(effectiveCatalogResolver, pattern);
			break;
		}
		case "data": {
			List<String> noas = cmData.nonOptionArgs;
			if(noas.size() != 1) {
				throw new RuntimeException("Only one non-option argument expected for the artifact id");
			}
			String artifactId = noas.get(0);

			CatalogResolver effectiveCatalogResolver = createEffectiveCatalogResolver(cmSearch.catalogs);						
			showData(effectiveCatalogResolver, artifactId, cmData.contentType, cmData.encodings, cmData.link);
			break;
		}
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
				CkanClient ckanClient = new CkanClient(cmDeployCkan.ckanUrl, cmDeployCkan.apikey);
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
		String userDirStr = StandardSystemProperty.USER_HOME.value();
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
		
		Collection<String> datasetIds;
		CatalogResolver catalogResolver;

		Path tempDir = Paths.get(cmDeployVirtuoso.tmpFolder);
		
		Function<String, String> iriResolver = null;
		if(dcatSource != null) {		
			iriResolver = createIriResolver(dcatSource);
			Model dcatModel = DcatUtils.createModelWithNormalizedDcatFragment(dcatSource);
			Collection<DcatDataset> dcatDatasets = DcatUtils.listDcatDatasets(dcatModel);
			datasetIds = dcatDatasets.stream().map(Resource::getURI).collect(Collectors.toList());
			
			catalogResolver = CatalogResolverUtils.wrapWithDiskCache(new CatalogResolverModel(dcatModel));
		} else {
			catalogResolver = CatalogResolverUtils.createCatalogResolverDefault();

			datasetIds = cmDeployVirtuoso.datasets;
//
//			catalogResolver.resolveDataset(datasetId)
//			
//			
//			DcatDataset dcatDataset = DcatCkanRdfUtils.convertToDcat(ckanDataset, pm);
		}
		



		
		logger.info("Detected datasets:");
		for(String dcatDataset: datasetIds) {
			logger.info("  " + dcatDataset);
		}
		logger.info(datasetIds.size() + " datasets enqueued");
		
		Path allowedFolder = Paths.get(cmDeployVirtuoso.allowed);
		
		String dockerContainerId = cmDeployVirtuoso.docker;
		DockerClient dockerClient = null;
		
		String hostName = cmDeployVirtuoso.host;
		if(dockerContainerId != null) {
			dockerClient = DockerServiceSystemDockerClient
					.create(true, Collections.emptyMap(), Collections.emptySet())
					.getDockerClient();
			
			if(hostName == null) {
				ContainerInfo containerInfo = dockerClient.inspectContainer(dockerContainerId);
				hostName = containerInfo.networkSettings().ipAddress();				
			}
		}

		if(Strings.isNullOrEmpty(hostName)) {// || hostName.equals("127.0.0.1")) {
			hostName = "localhost";
		}

		
		VirtuosoDataSource dataSource = new VirtuosoDataSource();
		dataSource.setPassword(cmDeployVirtuoso.pass);
		dataSource.setUser(cmDeployVirtuoso.user);
		dataSource.setPortNumber(cmDeployVirtuoso.port);
		dataSource.setServerName(hostName);

		try {
			try(Connection conn = dataSource.getConnection()) {
				
				VirtuosoBulkLoad.logEnable(conn, 2, 0);

				//for(DcatDataset dcatDataset : dcatDatasets) {
				for(String datasetId : datasetIds) {
					//String datasetId = dcatDataset.getURI();
					DatasetResolver datasetResolver = catalogResolver.resolveDataset(datasetId).blockingGet();
					
					DcatDeployVirtuosoUtils.deploy(
							datasetResolver,
							iriResolver,
							dockerClient,
							dockerContainerId,
							tempDir,
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

	public static void searchDcat(Collection<SearchResult> acc, CatalogResolver catalogResolver, String pattern) throws IOException {
		if(catalogResolver instanceof CatalogResolverCaching) {
			
		} else if(catalogResolver instanceof CatalogResolverMulti) {
			Collection<CatalogResolver> subResolvers = ((CatalogResolverMulti)catalogResolver).getResolvers();
			for(CatalogResolver subResolver : subResolvers) {
				searchDcat(acc, subResolver, pattern);
			}
		} else if(catalogResolver instanceof CatalogResolverSparql) {
			CatalogResolverSparql crs = (CatalogResolverSparql)catalogResolver;
			SparqlQueryConnection conn = crs.getConnection();
			
			Query patternQuery = crs.getPatternToQuery().apply(pattern);
			List<SearchResult> matches = CatalogResolverSparql.searchDcat(conn, patternQuery);
			logger.info(matches.size() + " matches from sparql-based resolver " + catalogResolver);
			acc.addAll(matches);
			
		} else {
			List<Resource> matches = catalogResolver.search(pattern).toList().blockingGet();
			logger.info(matches.size() + " matches from generic resolver " + catalogResolver);
			acc.addAll(matches.stream().map(x -> x.as(SearchResult.class)).collect(Collectors.toList()));
		}
	}
	
	public static List<CatalogResolver> unnestResolvers(List<CatalogResolver> acc, CatalogResolver resolver) {
		if(resolver instanceof CatalogResolverCaching) {
			CatalogResolver subResolver = ((CatalogResolverCaching)resolver).getBackend();
			unnestResolvers(acc, subResolver);
		} else if(resolver instanceof CatalogResolverMulti) {
			Collection<CatalogResolver> subResolvers = ((CatalogResolverMulti)resolver).getResolvers();
			for(CatalogResolver subResolver : subResolvers) {
				unnestResolvers(acc, subResolver);
			}
		} else {
			acc.add(resolver);
		}
		return acc;
	}
	
	public static void searchDcat(CatalogResolver catalogResolver, String pattern) throws IOException {
		List<CatalogResolver> catalogs = unnestResolvers(new ArrayList<>(), catalogResolver);
		logger.info("Searching " + catalogs.size() + " catalogs for '" + pattern + "'");
		List<SearchResult> items = new ArrayList<SearchResult>();
		for(int i = 0; i < catalogs.size(); ++i) {
			CatalogResolver catalog = catalogs.get(i);
			try {
				searchDcat(items, catalog, pattern);
			} catch(Exception e) {
				logger.info("Lookup failed for resolver " + catalog);
			}
		}

		Collections.sort(items, SearchResult::defaultCompare);
		MainDeleteme.print(items);
	}
	
	
	public static void showData(CatalogResolver catalogResolver, String artifactId, String formatOrContentType, List<String> encodings, boolean link) throws IOException {

		// Try to parse the format
		RdfEntityInfo info = ContentTypeUtils.deriveHeadersFromFileExtension("." + formatOrContentType);
		if(info != null) {
			if(info.getContentType() != null) {
				formatOrContentType = info.getContentType();
			}
			
			if(!CollectionUtils.isEmpty(info.getContentEncodings())) {
				encodings = info.getContentEncodings();
			}
		}
		
		catalogResolver.resolveDataset(artifactId).blockingGet();
		catalogResolver.resolveDistribution(artifactId).toList().blockingGet();
		//catalogResolver.resolveDataset(artifactId).blockingGet();
		
		HttpRequest request = HttpResourceRepositoryFromFileSystemImpl.createRequest(artifactId, formatOrContentType, encodings);
		HttpResourceRepositoryFromFileSystemImpl datasetRepository = HttpResourceRepositoryFromFileSystemImpl.createDefault();
		RdfHttpEntityFile entity = ControllerLookup.resolveEntity(catalogResolver, datasetRepository, request);

		if(entity == null) {
			throw new RuntimeException("Could not obtain an HTTP entity from given arguments " + artifactId + " " + formatOrContentType + " " + encodings);
		}

		//RdfHttpEntityFile entity = HttpResourceRepositoryFromFileSystemImpl.get(repo, artifactId, contentType, encodings);
		Path path = entity.getAbsolutePath();
		
		if(link) {
			System.out.println(path);
		} else {
			Files.copy(path, System.out);
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

