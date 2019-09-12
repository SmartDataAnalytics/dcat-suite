package org.aksw.dcat_suite.plugin.virtuoso;

import org.aksw.dcat_suite.core.docker.ImageSpec;
import org.aksw.jena_sparql_api.mapper.annotation.IriNs;
import org.aksw.jena_sparql_api.mapper.annotation.ResourceView;
import org.apache.jena.rdf.model.Resource;

@ResourceView
public interface ImageSpecVirtuoso
	extends Resource
{
	@IriNs("eg")
	String getAllowedDir();
	
	ImageSpec setAllowedDir(String allowedDir);
}
