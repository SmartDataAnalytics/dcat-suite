package org.aksw.dcat.ap.domain.accessors;

import org.aksw.dcat.ap.domain.api.ResourceLike;
import org.aksw.dcat.util.view.SingleValuedAccessor;

interface ResourceLikeAccessor
	extends ResourceLike
{
	SingleValuedAccessor<String> entityUri();
	
	default String getEntityUri() { return entityUri().get(); }
	default void setEntityUri(String uri) { entityUri().set(uri); }
}