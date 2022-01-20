package org.aksw.dcat_suite.core.docker;

import org.aksw.jenax.annotation.reprogen.IriNs;
import org.aksw.jenax.annotation.reprogen.RdfType;

@RdfType("eg:ImageSpec")
public interface ImageSpec {
	@IriNs("eg")
	String getImage();
	
	ImageSpec setImage(String image);
}
