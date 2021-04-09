package org.aksw.dcat_suite.enrich;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.vocabulary.FOAF;
import org.apache.jena.vocabulary.DCAT;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;
import org.onebusaway.gtfs.model.FeedInfo;

public class GTFSModel {

	private String prefix, title;
	private Path fileName; 
	private GTFSFile gtfs; 
	private Model model; 
	
	 public static final String EUROVOC_TRANSPORT_THEME = "http://publications.europa.eu/resource/authority/eurovoc/2015";
	 public static final String EXAMPLE_NS = "http://www.example.org/";
	 public static final String SCHEMA_NS = "https://schema.org/";

	public GTFSModel (String gtfsFile, String title, String prefix) throws IOException {
		Path path = Paths.get(gtfsFile);
		this.prefix = prefix;
		this.title = title;
        this.fileName = path.getFileName();
		this.gtfs = new GTFSFile(gtfsFile);
		model = ModelFactory.createDefaultModel();
	}
	
	public void enrichFromFeedInfo() throws UnsupportedEncodingException {
		
		String resourceUri = createBaseUri("dataset");
		Resource dsResource = model.createResource(resourceUri);
		dsResource.addProperty(DCTerms.identifier,DigestUtils
			      .md5Hex(resourceUri));
	
        dsResource.addProperty(RDF.type, DCAT.Dataset);
        dsResource.addProperty(DCTerms.title, this.title);
        dsResource.addProperty(DCAT.theme, model.createResource(EUROVOC_TRANSPORT_THEME));
        
        String distUri = createBaseUri("distribution");
        Resource distribution = model.createResource(distUri);
        distribution.addProperty(model.createProperty(EXAMPLE_NS.concat("localId")),fileName.toString());
        dsResource.addProperty(DCAT.distribution,distribution); 
        
		processFeedInfo(dsResource);
	}
	
	public void processFeedInfo (Resource dsResource) throws UnsupportedEncodingException {
		
		for (FeedInfo feedinfo : gtfs.getStore().getAllFeedInfos()) {
			
			String temporal = "";
			if (feedinfo.getStartDate() != null) {
				temporal = feedinfo.getStartDate().getAsString();
			}
			if (feedinfo.getEndDate() != null) {
				temporal = temporal.concat(feedinfo.getStartDate().getAsString());
			} 
			if (!temporal.equals("")) {
				Resource periodOfTime = model.createResource(prefix.concat(temporal));
				periodOfTime.addProperty(RDF.type, DCTerms.PeriodOfTime);
				dsResource.addProperty(DCTerms.temporal, periodOfTime);
				if (feedinfo.getStartDate() != null) {
					Property startDateProperty = model.createProperty(SCHEMA_NS.concat("startDate"));
					String startDate = concatDate(feedinfo.getStartDate().getYear(),feedinfo.getStartDate().getMonth(), feedinfo.getStartDate().getDay());
					periodOfTime.addProperty(startDateProperty, startDate);
				}
				if (feedinfo.getEndDate() != null) {
					Property endDateProperty = model.createProperty(SCHEMA_NS.concat("endDate"));
					String endDate = concatDate(feedinfo.getEndDate().getYear(), feedinfo.getEndDate().getMonth(), feedinfo.getEndDate().getDay());
					periodOfTime.addProperty(endDateProperty, endDate);
				}
			}
			if (feedinfo.getLang() != null) {
				dsResource.addProperty(DCTerms.language,feedinfo.getLang());
			}
			
			Resource publisherResource = null;
			if (feedinfo.getPublisherUrl() != null) {
				publisherResource = model.createResource(feedinfo.getPublisherUrl());
			} 
			else if (feedinfo.getPublisherName() != null) {
				publisherResource = model.createResource(createBaseUri(""));
			} 
			if (publisherResource != null) {
				publisherResource.addProperty(RDF.type, FOAF.Agent);
				dsResource.addProperty(DCTerms.publisher,publisherResource);
				if (feedinfo.getPublisherName() != null) {
					publisherResource.addProperty(FOAF.name, feedinfo.getPublisherName());
				}
			
			}
			
			if (feedinfo.getVersion() != null) {
				dsResource.addProperty(model.createProperty(SCHEMA_NS.concat("version")),feedinfo.getVersion());
			}
		}
	}
	
	public String concatDate(int year, int month, int day) {
		String date = String.valueOf(year)
				.concat("-")
				.concat(String.valueOf(month)
				.concat("-")
				.concat(String.valueOf(day)));
		return date;
	}
	
	public String createBaseUri(String distOrDataset) throws UnsupportedEncodingException {
		String resourceUri = this.prefix
				.concat(distOrDataset)
				.concat("/")
				.concat(URLEncoder
						.encode(this.title, StandardCharsets.UTF_8.toString()
								.toString())); 
		return resourceUri;
	}
	
	public Model getModel() {
		return this.model;
	}
}
