package org.aksw.dcat.util.view;

public interface SingleValuedAccessor<T> {
	T get();
	void set(T value);
}
