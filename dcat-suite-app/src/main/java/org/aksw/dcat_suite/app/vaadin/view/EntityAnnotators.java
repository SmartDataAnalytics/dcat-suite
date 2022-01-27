package org.aksw.dcat_suite.app.vaadin.view;

import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResourceFactory;

public class EntityAnnotators {
	public static final RDFNode Provenance = ResourceFactory.createStringLiteral("prov");
	public static final RDFNode Basic = ResourceFactory.createStringLiteral("basic");
	// public static final RDFNode Basic = ResourceFactory.createStringLiteral("basic");
}
