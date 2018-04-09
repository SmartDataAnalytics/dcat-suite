package org.aksw.dcat.ap.jena.domain.impl;

import org.aksw.dcat.util.view.SingleValuedAccessor;

public interface AccessorSupplier<P> {
	<T> SingleValuedAccessor<T> getSingleValuedAccessor(P property, Class<T> valueType);	
}