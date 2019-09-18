package org.aksw.dcat_suite.metamodel;

import java.util.Set;

import org.apache.jena.rdf.model.Resource;

public interface SkosEntity
	extends Resource
{
	String getLabel();
	Set<String> getAltLabels();
}
