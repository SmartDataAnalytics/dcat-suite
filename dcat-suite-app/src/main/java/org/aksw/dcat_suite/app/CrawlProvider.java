package org.aksw.dcat_suite.app;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import org.aksw.ckan_deploy.core.DcatCkanRdfUtils;
import org.aksw.dcat.jena.domain.api.DcatDataset;
import org.aksw.dcat.utils.DcatUtils;
import org.aksw.dcat_suite.cli.main.MainCliDcatSuite;
import org.aksw.dcat_suite.clients.DkanClient;
import org.aksw.dcat_suite.clients.PostProcessor;
import org.aksw.jena_sparql_api.rx.DatasetFactoryEx;
import org.aksw.jena_sparql_api.utils.model.ResourceInDataset;
import org.aksw.jena_sparql_api.utils.model.ResourceInDatasetImpl;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.system.StreamRDFOps;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.shared.impl.PrefixMappingImpl;
import org.apache.jena.update.UpdateExecutionFactory;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateRequest;
import org.json.simple.parser.ParseException;

import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;

import eu.trentorise.opendata.jackan.CkanClient;
import eu.trentorise.opendata.jackan.internal.org.apache.http.client.ClientProtocolException;
import eu.trentorise.opendata.jackan.model.CkanDataset;

public class CrawlProvider {
	private static final String CKAN_UPDATE_QUERY = "PREFIX ckan: <http://ckan.aksw.org/ontology/> DELETE { ?s ?p ?o } WHERE { ?s ?p ?o FILTER (?p IN (ckan:id, ckan:name)) }";
	
	public Model crawl(String url, String prefix, Select<String> selectField) throws ClientProtocolException, IOException, ParseException, URISyntaxException {
		 
	        
	     String effectivePrefix = prefix;
	     if (effectivePrefix == null) {
	        effectivePrefix = url.trim();

	     if (!(effectivePrefix.endsWith("/") || effectivePrefix.endsWith("#"))) {
	                effectivePrefix = effectivePrefix + "/";
	       }
	      }
	     if (selectField.getValue().equals("dkan")) {
	    	 DkanClient dkanClient = new DkanClient(url);
		     List<String> datasets = dkanClient.getDatasetList();
	    	 return processDkanImport(dkanClient, effectivePrefix, datasets, false);
	     }
	     else {
	    	 CkanClient ckanClient = new CkanClient(url); 
	    	 List<String> datasets = ckanClient.getDatasetList();
	    	 return processCkanImport(ckanClient,effectivePrefix,datasets);
	     }
	}
	
	public static Model processCkanImport(CkanClient ckanClient, String prefix, List<String> datasets) {
		UpdateRequest ur = UpdateFactory.create(
		"PREFIX ckan: <http://ckan.aksw.org/ontology/> DELETE { ?s ?p ?o } WHERE { ?s ?p ?o FILTER (?p IN (ckan:id, ckan:name)) }");
		Model model = ModelFactory.createDefaultModel();; 
		for (String s : datasets) {

			CkanDataset ckanDataset = PostProcessor.process(ckanClient.getDataset(s));
			PrefixMapping pm = DcatUtils.addPrefixes(new PrefixMappingImpl());

			DcatDataset dcatDataset = DcatCkanRdfUtils.convertToDcat(ckanDataset, pm);

				// Skolemize the resource first (so we have a reference to the resource)
				dcatDataset = DcatCkanRdfUtils.skolemizeClosureUsingCkanConventions(dcatDataset).as(DcatDataset.class);
				if (prefix != null) {
					dcatDataset = DcatCkanRdfUtils.assignFallbackIris(dcatDataset, prefix).as(DcatDataset.class);
				}

				// Remove temporary ckan specific attributes
				if (false) {
					UpdateExecutionFactory.create(ur, DatasetFactory.wrap(dcatDataset.getModel())).execute();
				}	
			model.add(dcatDataset.getModel()); 
		}
		return model;
	}
	
	public static Model processDkanImport(DkanClient dkanClient, String prefix, List<String> datasetNameOrIds,
			Boolean altJSON)
			throws ClientProtocolException, URISyntaxException, IOException, org.json.simple.parser.ParseException {
		
			UpdateRequest ur = UpdateFactory.create(CKAN_UPDATE_QUERY);
			Model model = ModelFactory.createDefaultModel();
			for (String s : datasetNameOrIds) {
				List<CkanDataset> datasets = dkanClient.getDataset(s, altJSON);

				for (CkanDataset dataset : datasets) {
					dataset = PostProcessor.process(dataset);
					DcatDataset dcatDataset = MainCliDcatSuite.getDcatDataset(dataset, prefix, ur);
					model.add(dcatDataset.getModel());
			}
		}
			return model;
	}
	
	

}
