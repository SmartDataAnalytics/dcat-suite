package org.aksw.jena_sparql_api.pseudo_rdf;

import java.util.Set;

import org.aksw.jena_sparql_api.utils.model.SetFromPropertyValues;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

import com.google.common.collect.Range;

/**
 * Resource-centric update of a pseudo RDF graph based on a (real) RDF graph.
 * 
 * 
 * TODO In the case of the publisher relaton, we actually have a non-writable accessor
 * - the accessor returns the dataset itself - and not any of its attributes
 * 
 * 
 * @author raven Apr 19, 2018
 *
 */
public class GraphCopy {

	public static void copy(Resource src, PseudoNode tgt) {

		// Iterate all supported properties
		for(String pStr : tgt.getPropertyToAccessor().keySet()) {
			Property p = src.getModel().createProperty(pStr);

			PseudoRdfProperty tgtProperty = tgt.getProperty(pStr);
			
			//property.get
			
			Set<RDFNode> os = new SetFromPropertyValues<>(src, p, RDFNode.class);
			
			// Recursion depends on the property type
			// (a) field property
			// (b) collection property
			// (a) singleton property
			
			Range<Long> multiplicity = tgtProperty.getMultiplicity();
			
			System.out.println("Multiplicity of " + p + ": " + multiplicity);
		}
		
	}
}
