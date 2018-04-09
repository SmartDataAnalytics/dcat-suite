package org.aksw.dcat.ap.ckan;

public interface Implementation<E, V> {
	boolean canWrap(E entity);
	V wrap(E entity);
}