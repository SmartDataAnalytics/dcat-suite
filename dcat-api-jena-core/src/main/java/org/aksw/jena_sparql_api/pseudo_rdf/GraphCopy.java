package org.aksw.jena_sparql_api.pseudo_rdf;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

import org.aksw.jena_sparql_api.utils.model.NodeMapper;
import org.aksw.jena_sparql_api.utils.model.SetFromPropertyValues;
import org.apache.jena.graph.Node;
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

		System.out.println("Copying " + src + " to " + tgt);
		
		// Iterate all supported properties
		for(String pStr : tgt.getPropertyToAccessor().keySet()) {
			Property p = src.getModel().createProperty(pStr);

			PseudoRdfProperty tgtProperty = tgt.getProperty(pStr);
			
			//property.get
						
			// Recursion depends on the property type
			// (a) field property
			// (b) collection property
			// (a) singleton property
			
			Range<Long> multiplicity = tgtProperty.getMultiplicity();
			
			System.out.println("Multiplicity of " + p + ": " + multiplicity);
			
			// Singleton attribute
			if(multiplicity.equals(Range.singleton(1l))) {
				Collection<PseudoNode> tgtOs = tgtProperty.getValues().stream()
						.filter(x -> x instanceof PseudoNode)
						.map(x -> (PseudoNode)x)
						.collect(Collectors.toList());
				
				PseudoNode tgtO = tgtOs.iterator().next();
				
				Collection<Resource> os = new SetFromPropertyValues<>(src, p, Resource.class);
				Collection<Resource> srcOs = os.isEmpty() ? Collections.emptySet() : Collections.singleton(os.iterator().next());
				
				for(Resource srcO : srcOs) {
					System.out.println("Singleton: " + srcO);
					Resource r = srcO.asResource();
					
					// If there is more than one src object, only 
					
					copy(r, tgtO);
				}
			// Basic getter/setter
			} else if(multiplicity.equals(Range.closed(0l, 1l))) {
				Collection<RDFNode> srcOs = new SetFromPropertyValues<>(src, p, RDFNode.class);
				Collection<Node> tgtOs = tgtProperty.getValues();
				for(RDFNode srcO : srcOs) {
					Node n = srcO.asNode();
					tgtOs.clear();
					tgtOs.add(n);					
				}
			// Collection attribute
			} else if(multiplicity.equals(Range.atLeast(0l))) {
				// TODO We need to discriminate between literals and objects...
				Collection<RDFNode> srcOs = new SetFromPropertyValues<>(src, p, RDFNode.class);
				Collection<Node> tgtOs = tgtProperty.getValues();
				for(RDFNode srcO : srcOs) {
					// Try to create instances of the tgt property's type
					RdfType<?> rdfType = tgtProperty.getType();
					
					Object o = rdfType.newInstance(srcO);
					Node tgtNode = ((NodeMapper<Object>)tgtProperty.getNodeMapper()).toNode(o);
					
					//Node tgtO = rdfType.newInstance(srcO);
					tgtOs.add(tgtNode);
					
					if(srcO.isResource() && tgtNode instanceof PseudoNode) {
						PseudoNode tgtO = (PseudoNode)tgtNode;
						
						Resource r = srcO.asResource();
						copy(r, tgtO);
					}
					
				}
				
			} else {
				System.out.println("Unsupported multiplicity: " + multiplicity);
			}
			
		}
		
	}
}
