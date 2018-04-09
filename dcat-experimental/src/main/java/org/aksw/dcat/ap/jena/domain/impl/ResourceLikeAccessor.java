package org.aksw.dcat.ap.jena.domain.impl;

import org.aksw.dcat.util.view.SingleValuedAccessor;

interface ResourceLikeAccessor {
	SingleValuedAccessor<String> entityUri();
	
	default String getEntiyUri() { return entityUri().get(); }
	default void setEntityUri(String uri) { entityUri().set(uri); }
}