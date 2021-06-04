package org.aksw.dcat_suite.cli.main;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.aksw.ckan_deploy.core.DcatCkanDeployUtils;
import org.aksw.ckan_deploy.core.DcatCkanRdfUtils;
import org.aksw.ckan_deploy.core.DcatDeployVirtuosoUtils;
import org.aksw.ckan_deploy.core.DcatExpandUtils;
import org.aksw.ckan_deploy.core.DcatRepository;
import org.aksw.ckan_deploy.core.DcatRepositoryDefault;
import org.aksw.commons.util.exception.ExceptionUtilsAksw;
import org.aksw.dcat.jena.domain.api.DcatDataset;
import org.aksw.dcat.jena.domain.api.DcatDistribution;
import org.aksw.dcat.repo.api.CatalogResolver;
import org.aksw.dcat.repo.api.DatasetResolver;
import org.aksw.dcat.repo.impl.cache.CatalogResolverCaching;
import org.aksw.dcat.repo.impl.core.CatalogResolverUtils;
import org.aksw.dcat.repo.impl.fs.CatalogResolverMulti;
import org.aksw.dcat.repo.impl.model.CatalogResolverModel;
import org.aksw.dcat.repo.impl.model.CatalogResolverSparql;
import org.aksw.dcat.repo.impl.model.DcatResolver;
import org.aksw.dcat.repo.impl.model.SearchResult;
//import org.aksw.dcat.server.controller.ControllerLookup;
import org.aksw.dcat.utils.DcatUtils;
import org.aksw.dcat_suite.cli.cmd.CmdDcatSuiteMain;
import org.aksw.dcat_suite.cli.cmd.CmdDeployVirtuoso;
import org.aksw.dcat_suite.cli.cmd.CmdEnrichGTFS;
import org.aksw.dcat_suite.clients.DkanClient;
import org.aksw.dcat_suite.clients.PostProcessor;
import org.aksw.dcat_suite.enrich.GTFSFile;
import org.aksw.dcat_suite.enrich.GTFSModel;
import org.aksw.dcat_suite.integrate.IntegrationFactory;
import org.aksw.jena_sparql_api.conjure.utils.ContentTypeUtils;
import org.aksw.jena_sparql_api.ext.virtuoso.VirtuosoBulkLoad;
import org.aksw.jena_sparql_api.http.domain.api.RdfEntityInfo;
import org.aksw.jena_sparql_api.http.repository.api.RdfHttpEntityFile;
import org.aksw.jena_sparql_api.http.repository.impl.HttpResourceRepositoryFromFileSystemImpl;
import org.aksw.jena_sparql_api.json.RdfJsonUtils;
import org.aksw.jena_sparql_api.mapper.proxy.JenaPluginUtils;
import org.aksw.jena_sparql_api.pseudo_rdf.MappingVocab;
import org.aksw.jena_sparql_api.rx.DatasetFactoryEx;
import org.aksw.jena_sparql_api.utils.io.StreamRDFDeferred;
import org.aksw.jena_sparql_api.utils.model.ResourceInDataset;
import org.aksw.jena_sparql_api.utils.model.ResourceInDatasetImpl;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.http.HttpRequest;
import org.apache.jena.arq.querybuilder.*;
import org.apache.jena.graph.FrontsNode;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.rdfconnection.SparqlQueryConnection;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.system.IRIResolver;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFOps;
import org.apache.jena.riot.system.StreamRDFWriter;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.shared.impl.PrefixMappingImpl;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.lang.arq.ParseException;
import org.apache.jena.sparql.vocabulary.FOAF;
import org.apache.jena.sys.JenaSystem;
import org.apache.jena.update.UpdateExecutionFactory;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateRequest;
import org.apache.jena.vocabulary.DCAT;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.hobbit.core.service.docker.impl.docker_client.DockerServiceSystemDockerClient;
import org.onebusaway.gtfs.model.FeedInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.drew.metadata.Schema;
import com.google.common.base.StandardSystemProperty;
import com.google.common.base.Strings;
import com.google.common.collect.Ordering;
import com.google.common.collect.Streams;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.messages.ContainerInfo;

import eu.trentorise.opendata.jackan.CkanClient;
import eu.trentorise.opendata.jackan.internal.org.apache.http.client.ClientProtocolException;
import eu.trentorise.opendata.jackan.model.CkanDataset;
import eu.trentorise.opendata.jackan.model.CkanResource;
import picocli.CommandLine;
import virtuoso.jdbc4.VirtuosoDataSource;

public class MainCliDcatSuite {

	private static final Logger logger = LoggerFactory.getLogger(MainCliDcatSuite.class);
	private static final String CKAN_UPDATE_QUERY = "PREFIX ckan: <http://ckan.aksw.org/ontology/> DELETE { ?s ?p ?o } WHERE { ?s ?p ?o FILTER (?p IN (ckan:id, ckan:name)) }";

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

	public static CatalogResolver createEffectiveCatalogResolver(List<String> catalogs)
			throws IOException, ParseException {

		List<CatalogResolver> catalogResolvers = new ArrayList<>();

		CatalogResolver defaultResolver = CatalogResolverUtils.createCatalogResolverDefault();
		catalogResolvers.add(defaultResolver);
		// catalogResolvers.addAll((defaultResolver));

		for (String catalog : catalogs) {
			Model model = RDFDataMgr.loadModel(catalog);

			logger.info("Loaded " + model.size() + " triples for catalog at " + catalog);

			SparqlQueryConnection conn = RDFConnectionFactory.connect(DatasetFactory.wrap(model));

			catalogResolvers.add(CatalogResolverUtils.createCatalogResolver(conn, Collections.emptyList()));
			// catalogResolvers.add(new CatalogResolverModel(model));
		}

		CatalogResolver result = CatalogResolverMulti.wrapIfNeeded(catalogResolvers);

		return result;
	}

	public static void createEffectiveConfigModel() {
		// TBD
	}

	public static void main(String[] args) throws Exception {
		JenaSystem.init();

		// TODO Move to a plugin
		JenaPluginUtils.registerResourceClasses(SearchResult.class);
		JenaPluginUtils.registerResourceClasses(DcatResolver.class);

		int exitCode = mainCore(args);
		if (exitCode != 0) {
			System.exit(exitCode);
		}
	}

	public static int mainCore(String[] args) throws Exception {

		int result = new CommandLine(new CmdDcatSuiteMain())
				.setExecutionExceptionHandler((ex, commandLine, parseResult) -> {
					boolean debugMode = true;
					if (debugMode) {
						ExceptionUtilsAksw.rethrowIfNotBrokenPipe(ex);
					} else {
						ExceptionUtilsAksw.forwardRootCauseMessageUnless(ex, logger::error,
								ExceptionUtilsAksw::isBrokenPipeException);
					}
					return 0;
				}).execute(args);
		return result;

	}

//
//        CmdMain cm = new CmdMain();
//        CmdSearch cmSearch = new CmdSearch();
//        CmdData cmData = new CmdData();
//        CmdShow cmShow = new CmdShow();
//        CmdExpand cmExpand = new CmdExpand();
//        CmdDeploy cmDeploy = new CmdDeploy();
//        CmdImport cmImport = new CmdImport();
//        CmdInstall cmInstall = new CmdInstall();
//        CmdTransform cmTransform = new CmdTransform();
//        CmdService cmdService = new CmdService();
//
//
//        // CommandCommit commit = new CommandCommit();
//        JCommander jc = JCommander.newBuilder()
//                .addObject(cm)
//                .addCommand("search", cmSearch)
//                .addCommand("data", cmData)
//                .addCommand("show", cmShow)
//                .addCommand("expand", cmExpand)
//                .addCommand("deploy", cmDeploy)
//                .addCommand("import", cmImport)
//                .addCommand("install", cmInstall)
//                .addCommand("transform", cmTransform)
//                .addCommand("service", cmdService)
//                .build();
//
//        JCommander serviceSubCmds = jc.getCommands().get("service");
//        CmdServiceCreate serviceCreateCmd = new CmdServiceCreate();
//        serviceSubCmds.addCommand("create", serviceCreateCmd);
//
//        JCommander deploySubCommands = jc.getCommands().get("deploy");
//
//        CmdDeployCkan cmDeployCkan = new CmdDeployCkan();
//        deploySubCommands.addCommand("ckan", cmDeployCkan);
//
//        CmdDeployVirtuoso cmDeployVirtuoso = new CmdDeployVirtuoso();
//        deploySubCommands.addCommand("virtuoso", cmDeployVirtuoso);
//
//
//        JCommander importSubCommands = jc.getCommands().get("import");
//
//        CmdImportCkan cmImportCkan = new CmdImportCkan();
//        importSubCommands.addCommand("ckan", cmImportCkan);
//
//        jc.parse(args);
//
//        if (cm.help) {
//            jc.usage();
//            return;
//        }
//
//        // TODO Change this to a plugin system - for now I hack this in statically
//        String cmd = jc.getParsedCommand();
//        switch (cmd) {
//        case "service": {
//            String serviceCmd = serviceSubCmds.getParsedCommand();
//            switch(serviceCmd) {
//            case "create":
//                //createService();
//            }
//            break;
//        }
//        case "search": {
////            List<String> noas = cmSearch.nonOptionArgs;
////            if(noas.size() != 1) {
////                throw new RuntimeException("Only one non-option argument expected for the artifact id");
////            }
////            String pattern = noas.get(0);
////
////            CatalogResolver effectiveCatalogResolver = createEffectiveCatalogResolver(cmSearch.catalogs);
////            searchDcat(effectiveCatalogResolver, pattern, cmSearch.jsonOutput);
////            break;
//        }
//        case "data": {
////            List<String> noas = cmData.nonOptionArgs;
////            if(noas.size() != 1) {
////                throw new RuntimeException("Only one non-option argument expected for the artifact id");
////            }
////            String artifactId = noas.get(0);
////
////            CatalogResolver effectiveCatalogResolver = createEffectiveCatalogResolver(cmSearch.catalogs);
////            showData(effectiveCatalogResolver, artifactId, cmData.contentType, cmData.encodings, cmData.link);
////            break;
//        }
//        case "show": {
////            Model dcatModel = DcatCkanRdfUtils.createModelWithNormalizedDcatFragment(cmShow.file);
////            RDFDataMgr.write(System.out, dcatModel, RDFFormat.TURTLE_PRETTY);
////            break;
//        }
//        case "expand": {
////            processExpand(cmExpand.file);
////            break;
//        }
//        case "deploy": {
//            String deployCmd = deploySubCommands.getParsedCommand();
//            switch(deployCmd) {
//            case "ckan": {
//                CkanClient ckanClient = new CkanClient(cmDeployCkan.ckanUrl, cmDeployCkan.apikey);
//                //showCkanDatasets(ckanClient);
//                //if(false) {
//                processDeploy(ckanClient, cmDeployCkan.file, cmDeployCkan.noupload, !cmDeployCkan.noMapByGroup, cmDeployCkan.organization);
//                //}
//                break;
//            }
//            case "virtuoso": {
//                processDeployVirtuoso(cmDeployVirtuoso);
//                break;
//            }
//            default: {
//                throw new RuntimeException("Unknow deploy command: " + deployCmd);
//            }
//            }
//            break;
//        }
//
//        case "import": {
//            String importCmd = importSubCommands.getParsedCommand();
//            switch(importCmd) {
//            case "ckan": {
//                CkanClient ckanClient = new CkanClient(cmImportCkan.ckanUrl, cmImportCkan.apikey);
//
//                List<String> datasets;
//
//                if(cmImportCkan.all) {
//                    if(!cmImportCkan.datasets.isEmpty()) {
//                        throw new RuntimeException("Options for import all and specific datasets mutually exclusive");
//                    }
//
//                    logger.info("Retrieving the list of all datasets in the catalog");
//                    datasets = ckanClient.getDatasetList();
//                } else {
//                    if(cmImportCkan.datasets.isEmpty()) {
//                        throw new RuntimeException("No datasets to import");
//                    }
//
//                    datasets = cmImportCkan.datasets;
//                }
//
//
//                processCkanImport(ckanClient, cmImportCkan.prefix, datasets);
//
//                break;
//            }
//            default: {
//
//            }
//            }
//
//            break;
//        }
//
////        case "transform": {
////            // TODO Find some expectOne() args
////            List<String> transforms = cmTransform.transforms;
////            String dcatFile = Iterables.getOnlyElement(cmTransform.nonOptionArgs);
////            Model dcatModel = RDFDataMgr.loadModel(dcatFile);
////
////            boolean materalize = cmTransform.materialize;
////
////            //Map<String, String> env = Collections.emptyMap();
////            Map<String, Node> env = cmTransform.envVars.stream()
////                .map(DcatOps::parseEntry)
////                .map(e -> Maps.immutableEntry(e.getKey(), NodeFactory.createLiteral(e.getValue())))
////                .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
////
////
//////			Properties p = new Properties();
//////			for(String env : cmTransform.envVars) {
//////				p.load(new ByteArrayInputStream(env.getBytes()));
//////			}
////            //SparqlStmtUtils.processFile(pm, filenameOrURI)
////            // cmTransform.nonOptionArgs
////            Consumer<Resource> distTransform = DcatOps.createDistTransformer(
////                    transforms, env, Paths.get("target"));
////
////            DcatOps.transformAllDists(dcatModel, distTransform);
////
////            if(materalize) {
////                Path path = Paths.get("target");
////                Consumer<Resource> materializer = DcatOps.createDistMaterializer(path);
////                DcatOps.transformAllDists(dcatModel, materializer);
////            }
////
////            RDFDataMgr.write(System.out, dcatModel, RDFFormat.TURTLE_PRETTY);
////            break;
////        }
//
//
//        case "install": {
////            String dcatSource = cmInstall.file;
////
////            Model dcatModel = DcatCkanRdfUtils.createModelWithNormalizedDcatFragment(cmShow.file);
////            Function<String, String> iriResolver = createIriResolver(dcatSource);
////            DcatRepository dcatRepository = createDcatRepository();
////
////            //for(dcatModel.listSubjects(null, DCAT.distribution, null)
////
////            for(DcatDataset dcatDataset : DcatUtils.listDcatDatasets(dcatModel)) {
////                DcatInstallUtils.install(dcatRepository, dcatDataset, iriResolver, false);
////            }
////            break;
//        }
//
//
//        default:
//            throw new RuntimeException("Unknown command: " + cmd);
//
//        }
//    }

	public static Function<String, String> createIriResolver(String dcatSource) {
		Path dcatPath = Paths.get(dcatSource).toAbsolutePath();
		String baseIRI = dcatPath.getParent().toUri().toString();
		IRIResolver iriResolver = IRIResolver.create(baseIRI);

		return iriResolver::resolveToStringSilent;
	}

	public static DcatRepository createDcatRepository() throws IOException {
		String userDirStr = StandardSystemProperty.USER_HOME.value();
		Path userFolder = Paths.get(userDirStr);
		if (!Files.exists(userFolder)) {
			throw new RuntimeException("Failed to find user directory");
		}

		Path repoFolder = userFolder.resolve(".dcat").resolve("repository");
		Files.createDirectories(repoFolder);

		DcatRepository result = new DcatRepositoryDefault(repoFolder);

		return result;
	}

	public static void processDeployVirtuoso(CmdDeployVirtuoso cmDeployVirtuoso) throws Exception {

		String dcatSource = cmDeployVirtuoso.file;

		Collection<String> datasetIds;
		CatalogResolver catalogResolver;

		Path tempDir = Paths.get(cmDeployVirtuoso.tmpFolder);

		Function<String, String> iriResolver = null;
		if (dcatSource != null) {
			iriResolver = createIriResolver(dcatSource);
			Model dcatModel = DcatCkanRdfUtils.createModelWithNormalizedDcatFragment(dcatSource);
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
		for (String dcatDataset : datasetIds) {
			logger.info("  " + dcatDataset);
		}
		logger.info(datasetIds.size() + " datasets enqueued");

		Path allowedFolder = Paths.get(cmDeployVirtuoso.allowed);

		String dockerContainerId = cmDeployVirtuoso.docker;
		DockerClient dockerClient = null;

		String hostName = cmDeployVirtuoso.host;
		if (dockerContainerId != null) {
			dockerClient = DockerServiceSystemDockerClient.create(true, Collections.emptyMap(), Collections.emptySet())
					.getDockerClient();

			if (hostName == null) {
				ContainerInfo containerInfo = dockerClient.inspectContainer(dockerContainerId);
				hostName = containerInfo.networkSettings().ipAddress();
			}
		}

		if (Strings.isNullOrEmpty(hostName)) {// || hostName.equals("127.0.0.1")) {
			hostName = "localhost";
		}

		VirtuosoDataSource dataSource = new VirtuosoDataSource();
		dataSource.setPassword(cmDeployVirtuoso.pass);
		dataSource.setUser(cmDeployVirtuoso.user);
		dataSource.setPortNumber(cmDeployVirtuoso.port);
		dataSource.setServerName(hostName);

		try {
			try (Connection conn = dataSource.getConnection()) {

				VirtuosoBulkLoad.logEnable(conn, 2, 0);

				// for(DcatDataset dcatDataset : dcatDatasets) {
				for (String datasetId : datasetIds) {
					// String datasetId = dcatDataset.getURI();
					DatasetResolver datasetResolver = catalogResolver.resolveDataset(datasetId).blockingGet();

					DcatDeployVirtuosoUtils.deploy(datasetResolver, iriResolver, dockerClient, dockerContainerId,
							tempDir, allowedFolder, cmDeployVirtuoso.nosymlinks, conn);
				}

				conn.commit();
			}

			// TODO rollback on error
		} catch (Exception e) {
			throw new RuntimeException(e);
			// conn.rollback();
		}
	}

	/**
	 * Search catalogs for a given keyword pattern Applies a generic yet powerful
	 * search strategy to SPARQL-able catalogs, whereas uses the search() method
	 * otherwise. Note, that in the future we may want to add a flag for whether to
	 * override a provided search() method
	 *
	 * @param acc
	 * @param catalogResolver
	 * @param pattern
	 * @throws IOException
	 */
	public static void searchDcat(Collection<SearchResult> acc, CatalogResolver catalogResolver, String pattern)
			throws IOException {
		if (catalogResolver instanceof CatalogResolverCaching) {

		} else if (catalogResolver instanceof CatalogResolverMulti) {
			Collection<CatalogResolver> subResolvers = ((CatalogResolverMulti) catalogResolver).getResolvers();
			for (CatalogResolver subResolver : subResolvers) {
				searchDcat(acc, subResolver, pattern);
			}
		} else if (catalogResolver instanceof CatalogResolverSparql) {
			CatalogResolverSparql crs = (CatalogResolverSparql) catalogResolver;
			SparqlQueryConnection conn = crs.getConnection();
			Query shapeQuery = crs.getDcatShape();

			Query patternQuery = crs.getPatternToQuery().apply(pattern);
			List<SearchResult> matches = CatalogResolverSparql.searchDcat(conn, patternQuery, shapeQuery);
			logger.info(matches.size() + " matches from sparql-based resolver " + catalogResolver);
			acc.addAll(matches);

		} else {
			List<Resource> matches = catalogResolver.search(pattern).toList().blockingGet();
			logger.info(matches.size() + " matches from generic resolver " + catalogResolver);
			acc.addAll(matches.stream().map(x -> x.as(SearchResult.class)).collect(Collectors.toList()));
		}
	}

	public static List<CatalogResolver> unnestResolvers(List<CatalogResolver> acc, CatalogResolver resolver) {
		if (resolver instanceof CatalogResolverCaching) {
			CatalogResolver subResolver = ((CatalogResolverCaching) resolver).getBackend();
			unnestResolvers(acc, subResolver);
		} else if (resolver instanceof CatalogResolverMulti) {
			Collection<CatalogResolver> subResolvers = ((CatalogResolverMulti) resolver).getResolvers();
			for (CatalogResolver subResolver : subResolvers) {
				unnestResolvers(acc, subResolver);
			}
		} else {
			acc.add(resolver);
		}
		return acc;
	}

	public static void searchDcat(CatalogResolver catalogResolver, String pattern, boolean json) throws IOException {
		List<CatalogResolver> catalogs = unnestResolvers(new ArrayList<>(), catalogResolver);
		logger.info("Searching " + catalogs.size() + " catalogs for '" + pattern + "'");
		List<SearchResult> items = new ArrayList<SearchResult>();
		for (int i = 0; i < catalogs.size(); ++i) {
			CatalogResolver catalog = catalogs.get(i);
			try {
				searchDcat(items, catalog, pattern);
			} catch (Exception e) {
				logger.info("Lookup failed for resolver " + catalog);
			}
		}

		Collections.sort(items, Ordering.from(SearchResult::defaultCompare).reversed());

		if (json) {
			JsonArray j = RdfJsonUtils.toJson(items, 3, false);
			Gson gson = new GsonBuilder().setLenient().setPrettyPrinting().create();
			String str = gson.toJson(j);
			System.out.println(str);
		} else {
			MainDeleteme.print(items);
		}
	}

//	public static void showData(CatalogResolver catalogResolver, String artifactId, String formatOrContentType,
//			List<String> encodings, boolean link) throws IOException {
//
//		// Try to parse the format
//		RdfEntityInfo info = ContentTypeUtils.deriveHeadersFromFileExtension("." + formatOrContentType);
//		if (info != null) {
//			if (info.getContentType() != null) {
//				formatOrContentType = info.getContentType();
//			}
//
//			if (!CollectionUtils.isEmpty(info.getContentEncodings())) {
//				encodings = info.getContentEncodings();
//			}
//		}
//
//		catalogResolver.resolveDataset(artifactId).blockingGet();
//		catalogResolver.resolveDistribution(artifactId).toList().blockingGet();
//		// catalogResolver.resolveDataset(artifactId).blockingGet();
//
//		HttpRequest request = HttpResourceRepositoryFromFileSystemImpl.createRequest(artifactId, formatOrContentType,
//				encodings);
//		HttpResourceRepositoryFromFileSystemImpl datasetRepository = HttpResourceRepositoryFromFileSystemImpl
//				.createDefault();
//		RdfHttpEntityFile entity = ControllerLookup.resolveEntity(catalogResolver, datasetRepository, request);
//
//		if (entity == null) {
//			throw new RuntimeException("Could not obtain an HTTP entity from given arguments " + artifactId + " "
//					+ formatOrContentType + " " + encodings);
//		}
//
//		// RdfHttpEntityFile entity = HttpResourceRepositoryFromFileSystemImpl.get(repo,
//		// artifactId, contentType, encodings);
//		Path path = entity.getAbsolutePath();
//
//		if (link) {
//			System.out.println(path);
//		} else {
//			Files.copy(path, System.out);
//		}
//	}
	
	public static void processEnrichGTFS(String gtfsFile, String dsTitle, String prefix, String [] gtfsTypes) throws IOException {

		GTFSModel gtfsModel = new GTFSModel(gtfsFile, dsTitle, prefix); 
		gtfsModel.enrichFromFeedInfo();
		if (gtfsTypes != null) {
			gtfsModel.enrichFromType(gtfsTypes);
		}
			
		//OutputStream out = new FileOutputStream("src/stops.ttl");
		//RDFDataMgr.write(out, gtfsModel.getModel(), Lang.TURTLE);
		RDFDataMgr.write(System.out, gtfsModel.getModel(), RDFFormat.NTRIPLES);
	} 
	
	public static GTFSModel processEnrichGTFSWeb (String gtfsFile, String dsTitle, String prefix) throws IOException {
		GTFSModel gtfsModel = new GTFSModel(gtfsFile, dsTitle, prefix); 
		gtfsModel.enrichFromFeedInfo();
		return gtfsModel;
	}
	
	public static void integrate(Model dcatModel, Model linkModel, Model mapModel, RDFConnection conn) {
		IntegrationFactory.integrate(dcatModel, linkModel, mapModel, conn); 
		RDFDataMgr.write(System.out, dcatModel, RDFFormat.NTRIPLES);
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

	public static void processDeploy(CkanClient ckanClient, String dcatSource, boolean noFileUpload, boolean mapByGroup,
			String organization) throws IOException {
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

		Model deployDcatModel = DcatCkanDeployUtils.deploy(ckanClient, exportDcatModel, iriResolver, noFileUpload,
				mapByGroup, organization);
		DcatExpandUtils.writeSortedNtriples(deployDcatModel, targetFolder.resolve("deploy-dcat.nt"));
	}

	public static void processCkanImport(CkanClient ckanClient, String prefix, List<String> datasets, boolean quads) {

		// TODO Move this update request to a separate file and/or trigger it using a
		// flag
		UpdateRequest ur = UpdateFactory.create(
				"PREFIX ckan: <http://ckan.aksw.org/ontology/> DELETE { ?s ?p ?o } WHERE { ?s ?p ?o FILTER (?p IN (ckan:id, ckan:name)) }");

		Model pm2 = RDFDataMgr.loadModel("rdf-prefixes/prefix.cc.2019-12-17.ttl");
		StreamRDF streamRdf = StreamRDFWriter.getWriterStream(System.out, RDFFormat.TRIG_BLOCKS, null);
		streamRdf = new StreamRDFDeferred(streamRdf, true, pm2, 10, 1000, null);
		streamRdf.start();

		for (String s : datasets) {
			logger.info("Importing dataset " + s);

			CkanDataset ckanDataset = PostProcessor.process(ckanClient.getDataset(s));
			PrefixMapping pm = DcatUtils.addPrefixes(new PrefixMappingImpl());

			DcatDataset dcatDataset = DcatCkanRdfUtils.convertToDcat(ckanDataset, pm);

			try {
				// Skolemize the resource first (so we have a reference to the resource)
				dcatDataset = DcatCkanRdfUtils.skolemizeClosureUsingCkanConventions(dcatDataset).as(DcatDataset.class);
				if (prefix != null) {
					dcatDataset = DcatCkanRdfUtils.assignFallbackIris(dcatDataset, prefix).as(DcatDataset.class);
				}

				// Remove temporary ckan specific attributes
				if (false) {
					UpdateExecutionFactory.create(ur, DatasetFactory.wrap(dcatDataset.getModel())).execute();
				}

			} catch (Exception e) {
				logger.warn("Error processing dataset " + s, e);
			}

//            RDFDataMgr.write(System.out, dcatDataset.getModel(), RDFFormat.NTRIPLES);

			if (quads) {				
				ResourceInDataset resourceInNamedGraph = ResourceInDatasetImpl.createFromCopy(
						DatasetFactoryEx.createInsertOrderPreservingDataset(), dcatDataset.getURI(), dcatDataset);
				StreamRDFOps.sendDatasetToStream(resourceInNamedGraph.getDataset().asDatasetGraph(), streamRdf);
			} else {
				StreamRDFOps.sendGraphToStream(dcatDataset.getModel().getGraph(), streamRdf);
			}			
		}
		streamRdf.finish();
	}

	public static void processDkanImport(DkanClient dkanClient, String prefix, List<String> datasetNameOrIds,
			Boolean altJSON)
			throws ClientProtocolException, URISyntaxException, IOException, org.json.simple.parser.ParseException {
		UpdateRequest ur = UpdateFactory.create(CKAN_UPDATE_QUERY);
		for (String s : datasetNameOrIds) {
			logger.info("Importing dataset " + s);
			List<CkanDataset> datasets = dkanClient.getDataset(s, altJSON);

			for (CkanDataset dataset : datasets) {
				dataset = PostProcessor.process(dataset);
				DcatDataset dcatDataset = getDcatDataset(dataset, prefix, ur);
				RDFDataMgr.write(System.out, dcatDataset.getModel(), RDFFormat.NTRIPLES);
			}
		}
	}

	private static DcatDataset getDcatDataset(CkanDataset ckanDataset, String prefix, UpdateRequest ur) {
		// TODO Move this update request to a separate file and/or trigger it using a
		// flag
		PrefixMapping pm = DcatUtils.addPrefixes(new PrefixMappingImpl());
		DcatDataset dcatDataset = DcatCkanRdfUtils.convertToDcat(ckanDataset, pm);

		try {
			// Skolemize the resource first (so we have a reference to the resource)
			dcatDataset = DcatCkanRdfUtils.skolemizeClosureUsingCkanConventions(dcatDataset).as(DcatDataset.class);

			if (prefix != null) {
				dcatDataset = DcatCkanRdfUtils.assignFallbackIris(dcatDataset, prefix).as(DcatDataset.class);
			}

			// Remove temporary ckan specific attributes
			if (false) {
				UpdateExecutionFactory.create(ur, DatasetFactory.wrap(dcatDataset.getModel())).execute();
			}

		} catch (Exception e) {
			logger.warn("Error processing dataset " + ckanDataset.getId(), e);
		}

		return dcatDataset;
	}
}
