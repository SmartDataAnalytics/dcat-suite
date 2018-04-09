package org.aksw.dcat.ap.jena.domain.api;

/**
 * DCAT AP defines these fields for publishers which are considered to be of type foaf:Agent.
 * 
 * 
 * 
 * @author raven Apr 9, 2018
 *
 */
public interface DcatApAgent
	extends ResourceLike
{
	String getName();
	void setName(String name);
	
	String getMbox();
	void setMbox(String mbox);
	
	String getHomepage();
	void setHomepage(String homepage);
	
	String getType();
	void setType(String resource);
	
	// TODO add other foaf agent fields
}
