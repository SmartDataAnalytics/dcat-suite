package org.aksw.dcat_suite.enrich;

import java.io.UnsupportedEncodingException;
import java.util.Collection;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.vocabulary.FOAF;
import org.apache.jena.vocabulary.RDF;
import org.onebusaway.gtfs.model.Stop;

public class GTFSStop {
	
	private Model stopModel;
	private Resource datasetResource;
	private Resource stopType; 
	private Property latitude; 
	private Property longitude; 
	private Property gtfsStop; 
	
	public GTFSStop (Resource datasetResource) {
		this.datasetResource = datasetResource; 
		stopType = stopModel.createResource(GTFSModel.GTFS_NS.concat("Stop")); 
		latitude = stopModel.createProperty(GTFSModel.GEO.concat("lat")); 
		longitude = stopModel.createProperty(GTFSModel.GEO.concat("long")); 
		gtfsStop = stopModel.createProperty(GTFSModel.GTFS_NS.concat("stop"));
	}
	
	public Model createModel (String prefix, Collection<Stop> stops) throws UnsupportedEncodingException {
		for (Stop stopInfo : stops) {
			String stopUri = GTFSUtils.createBaseUri(prefix,"stop",stopInfo.getName()); 
		
			Resource stop = stopModel.createResource(stopUri);
			datasetResource.addProperty(gtfsStop, stop);
			stop.addProperty(RDF.type,stopType);
			stop.addProperty(FOAF.name, stopInfo.getName());
			stop.addLiteral(latitude,stopInfo.getLat());
			stop.addLiteral(longitude,stopInfo.getLon());
		}
		return stopModel;
	}
}
