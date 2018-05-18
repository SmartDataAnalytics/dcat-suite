package org.aksw.commons.accessors;

import java.util.function.Function;

/**
 * Function interface for for requesting accessor instances that enable
 * accessing the attribute 'name' of type 'clazz' on objects of type 'S'.
 * 
 * @author Claus Stadler, May 16, 2018
 *
 * @param <S> Entity type that acts as a source of properties
 */
public interface AccessorSupplierFactory<S> {
	<T> Function<S, ? extends SingleValuedAccessor<T>> createAccessor(String name, Class<T> clazz);
}