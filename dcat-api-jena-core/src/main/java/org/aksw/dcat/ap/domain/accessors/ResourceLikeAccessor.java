package org.aksw.dcat.ap.domain.accessors;

import org.aksw.commons.accessors.SingleValuedAccessor;
import org.aksw.dcat.ap.domain.api.ResourceLike;

interface ResourceLikeAccessor
	extends ResourceLike
{
	SingleValuedAccessor<String> entityUri();
	
	default String getEntityUri() { return entityUri().get(); }
	default void setEntityUri(String uri) { entityUri().set(uri); }
}