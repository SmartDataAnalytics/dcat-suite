package org.aksw.dcat_suite.cli.main;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.aksw.ckan_deploy.core.DcatCkanDeployUtils;
import org.aksw.ckan_deploy.core.DcatInstallUtils;
import org.aksw.ckan_deploy.core.DcatUtils;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.Banner;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.google.common.collect.Streams;

import eu.trentorise.opendata.jackan.CkanClient;

public class MainCliDcatSuite {
	@Configuration
	public static class ConfigSparqlIntegrate {

		@Bean
		public ApplicationRunner applicationRunner() {
			return args -> {
				String apiKey = Optional.ofNullable(args.getOptionValues("apikey")).orElse(Collections.emptyList())
						.stream().findFirst().orElseThrow(() -> new RuntimeException("API key must be given"));

				String host = Optional.ofNullable(args.getOptionValues("host")).orElse(Collections.emptyList()).stream()
						.findFirst().orElseThrow(() -> new RuntimeException("Host must be given"));

				CkanClient ckanClient = new CkanClient(host, apiKey);

				Collection<String> dcatSources = Optional.ofNullable(args.getNonOptionArgs())
						.orElseThrow(() -> new RuntimeException("No dataset sources specified"));

				for (String dcatSource : dcatSources) {

					Path dcatPath = Paths.get(dcatSource).toAbsolutePath();
					if (!Files.exists(dcatPath)) {
						throw new FileNotFoundException(dcatPath + " does not exist");
					}

					Path targetFolder = dcatPath.getParent().resolve("target").resolve("dcat");
					Files.createDirectories(targetFolder);

					Dataset dataset = RDFDataMgr.loadDataset(dcatSource);

					// CkanRdfDatasetProcessor processor = new CkanRdfDatasetProcessor(ckanClient);

					// processor.process(dataset);
					Model exportDcatModel = DcatInstallUtils.export(dataset, targetFolder);
					DcatInstallUtils.writeSortedNtriples(exportDcatModel, targetFolder.resolve("dcat.nt"));

					Model deployDcatModel = DcatCkanDeployUtils.deploy(ckanClient, exportDcatModel);
					DcatInstallUtils.writeSortedNtriples(deployDcatModel, targetFolder.resolve("deploy-dcat.nt"));
				}
			};
		}
	}

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

	@Parameters(separators = "=", commandDescription = "Expand quad datasets")
	public static class CommandExpand {

		@Parameter(description = "Quad-based RDF dataset")
		protected String file;
	}

	@Parameters(separators = "=", commandDescription = "Deploy DCAT datasets")
	public static class CommandDeploy {
	}

	@Parameters(separators = "=", commandDescription = "Deploy DCAT datasets")
	public static class CommandDeployCkan {

		@Parameter(description = "The DCAT file which to deploy", required = true)
		protected String file;

		@Parameter(names = "--host", description = "The URL of the CKAN instance")
		protected String host;

		@Parameter(names = "--apikey", description = "Your API key for the CKAN instance")
		protected String apikey;
	}

	public static void main(String[] args) throws IOException {
		CommandMain cm = new CommandMain();
		CommandShow cmShow = new CommandShow();
		CommandExpand cmExpand = new CommandExpand();
		CommandDeploy cmDeploy = new CommandDeploy();
		CommandDeployCkan cmDeployCkan = new CommandDeployCkan();

		// CommandCommit commit = new CommandCommit();
		JCommander jc = JCommander.newBuilder()
				.addObject(cm)
				.addCommand("show", cmShow)
				.addCommand("expand", cmExpand)
				.addCommand("deploy", cmDeploy)
				.build();

		JCommander deploySubCommands = jc.getCommands().get("deploy");
		deploySubCommands.addCommand("ckan", cmDeployCkan);

		jc.parse(args);

        if (cm.help) {
            jc.usage();
            return;
        }

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
			CkanClient ckanClient = new CkanClient(cmDeployCkan.host, cmDeployCkan.apikey);
			processDeploy(ckanClient, cmDeployCkan.file);
			break;
		}
		default:
			throw new RuntimeException("Unknown command: " + cmd);

		}
	}

	public static void processDeploy(CkanClient ckanClient, Collection<String> dcatSources) throws IOException {
	}

	public static Path processExpand(String dcatSource) throws IOException {
		Dataset dataset = RDFDataMgr.loadDataset(dcatSource);
		Path dcatPath = Paths.get(dcatSource).toAbsolutePath();
		Path targetFolder = dcatPath.getParent().resolve("target").resolve("dcat");
		Files.createDirectories(targetFolder);

		Model model = DcatInstallUtils.export(dataset, targetFolder);
		Path result = targetFolder.resolve("dcat.nt");
		DcatInstallUtils.writeSortedNtriples(model, result);
		return result;
	}

	public static void processDeploy(CkanClient ckanClient, String dcatSource) throws IOException {
		Dataset dataset = RDFDataMgr.loadDataset(dcatSource);
		Path dcatPath = Paths.get(dcatSource).toAbsolutePath();
		Path targetFolder = dcatPath.getParent().resolve("target").resolve("dcat");
		Files.createDirectories(targetFolder);

		// If the dataset has named graphs, we perform export
		boolean hasNamedGraphs = Streams.stream(dataset.listNames()).findFirst().isPresent();

		Model exportDcatModel;
		if (hasNamedGraphs) {
			exportDcatModel = DcatInstallUtils.export(dataset, targetFolder);
			DcatInstallUtils.writeSortedNtriples(exportDcatModel, targetFolder.resolve("dcat.nt"));
		} else {
			exportDcatModel = dataset.getDefaultModel();
		}

		Model deployDcatModel = DcatCkanDeployUtils.deploy(ckanClient, exportDcatModel);
		DcatInstallUtils.writeSortedNtriples(deployDcatModel, targetFolder.resolve("deploy-dcat.nt"));
	}
}

// List<String> ds = ckanClient.getDatasetList(10, 0);
//
// for (String s : ds) {
// System.out.println();
// System.out.println("DATASET: " + s);
// CkanDataset d = ckanClient.getDataset(s);
// System.out.println(" RESOURCES:");
// for (CkanResource r : d.getResources()) {
// System.out.println(" " + r.getName());
// System.out.println(" FORMAT: " + r.getFormat());
// System.out.println(" URL: " + r.getUrl());
// }
// }
