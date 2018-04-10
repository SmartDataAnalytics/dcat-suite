package org.aksw.move_to_proper_place;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResourceFactory;

/**
 * Vocab for temporary attributes used in internal processing.
 * 
 * @author raven Apr 10, 2018
 *
 */
public class TMP {
	// Some arbitrary namespace - this should not appear in any output anyway (internal processing only)
	public static final String NS = "http://tmp.aksw.org/ontology/";
	public static final String _key = NS + "key";
	
	public static final Property key = ResourceFactory.createProperty(_key);
}
