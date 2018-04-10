package org.aksw.dcat.ap.binding.ckan.domain.impl;

public interface Implementation<E, V> {
	boolean canWrap(E entity);
	V wrap(E entity);
}