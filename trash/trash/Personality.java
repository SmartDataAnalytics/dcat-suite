package org.aksw.dcat.ap.trash;

/**
 * 
 * @author raven Apr 9, 2018
 *
 * @param <E> Entity type backing the view
 * @param <V> View class
 */
public interface Personality<E, V> {
	<X extends V> Implementation<E, X> getImplementation(Class<X> clazz);
	
	
	<X extends V> void add(Class<X> clazz, Implementation<E, X> implementation);
	//<X extends T> X newInstance(Class<T> clazz);
}