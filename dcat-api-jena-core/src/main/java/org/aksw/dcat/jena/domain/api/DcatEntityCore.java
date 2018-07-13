package org.aksw.dcat.jena.domain.api;

import org.aksw.dcat.ap.domain.api.DcatApAgent;

public interface DcatEntityCore {
//	String getCkanId();
//	void setCkanId(String id);
	
	// Name is a public identifier; id is a ckan internal identifier
	String getIdentifier();
	void setIdentifier(String name);

	String getTitle();
	void setTitle(String title);

	String getDescription();
	void setDescription(String description);

	DcatApAgent getPublisher() ;
	void setPublisher(DcatApAgent agent) ;
}
