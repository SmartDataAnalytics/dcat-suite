package org.aksw.dcat_suite.core.docker;

import org.aksw.jena_sparql_api.mapper.annotation.IriNs;
import org.aksw.jena_sparql_api.mapper.annotation.RdfType;

@RdfType("eg:ImageSpec")
public interface ImageSpec {
	@IriNs("eg")
	String getImage();
	
	ImageSpec setImage(String image);
}
