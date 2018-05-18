package org.aksw.jena_sparql_api.pseudo_rdf;

import org.apache.jena.rdf.model.RDFNode;

public class RdfTypeUri
	implements RdfType<String>
{
	@Override
	public boolean canNewInstance(RDFNode rdfNode) {
		boolean result = rdfNode.isURIResource();
		return result;
	}

	@Override
	public String newInstance(RDFNode rdfNode) {
		String result = rdfNode.asResource().getURI();
		return result;
	}

}
