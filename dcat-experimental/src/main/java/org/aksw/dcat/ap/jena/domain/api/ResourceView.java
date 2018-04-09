package org.aksw.dcat.ap.jena.domain.api;

import java.util.Set;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;

public interface ResourceView {
	// Return a set view for the requested property
	Set<RDFNode> getProperties(Property p);
}
