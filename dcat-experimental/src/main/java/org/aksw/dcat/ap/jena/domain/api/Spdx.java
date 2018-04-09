package org.aksw.dcat.ap.jena.domain.api;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

public class Spdx {
	public static final String NS = "http://spdx.org/rdf/terms#";
	
	public static final String _Checksum = NS + "checksum";
	public static final String _checksumValue = NS + "checksumValue";
	public static final String _algorithm = NS + "algorithm";
	
	public static Resource resource(String uri) {
		return ResourceFactory.createProperty(uri);
	}

	public static Property property(String uri) {
		return ResourceFactory.createProperty(uri);
	}
	
	public static Resource Checksum = resource(_Checksum);
	public static Property checksumValue = property(_checksumValue);
	public static Property algorithm = property(_algorithm);
}
