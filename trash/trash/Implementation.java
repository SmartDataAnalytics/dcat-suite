package org.aksw.dcat.ap.trash;

public interface Implementation<E, V> {
	boolean canWrap(E entity);
	V wrap(E entity);
}