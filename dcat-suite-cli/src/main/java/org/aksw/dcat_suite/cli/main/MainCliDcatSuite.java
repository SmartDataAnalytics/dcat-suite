package org.aksw.dcat_suite.cli.main;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;

import org.aksw.ckan_deploy.core.DcatCkanDeployUtils;
import org.aksw.ckan_deploy.core.DcatInstallUtils;
import org.aksw.ckan_deploy.core.DcatUtils;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.system.IRIResolver;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.google.common.collect.Streams;

import eu.trentorise.opendata.jackan.CkanClient;

public class MainCliDcatSuite {

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

		String baseIRI = targetFolder.toUri().toString();
		IRIResolver iriResolver = IRIResolver.create(baseIRI);
		
		// If the dataset has named graphs, we perform export
		boolean hasNamedGraphs = Streams.stream(dataset.listNames()).findFirst().isPresent();

		Model exportDcatModel;
		if (hasNamedGraphs) {
			exportDcatModel = DcatInstallUtils.export(dataset, targetFolder);
			DcatInstallUtils.writeSortedNtriples(exportDcatModel, targetFolder.resolve("dcat.nt"));
		} else {
			exportDcatModel = dataset.getDefaultModel();
		}
		

		Model deployDcatModel = DcatCkanDeployUtils.deploy(ckanClient, exportDcatModel, iriResolver);
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
