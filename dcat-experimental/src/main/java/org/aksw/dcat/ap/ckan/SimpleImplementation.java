package org.aksw.dcat.ap.ckan;

import java.util.function.Function;

public class SimpleImplementation<E, V>
	implements Implementation<E, V>
{
	protected Function<? super E, ? extends V> ctor;//BiFunction<>
	
	public SimpleImplementation(Function<? super E, ? extends V> ctor) {
		super();
		this.ctor = ctor;
	}

	@Override
	public boolean canWrap(E entity) {
		return true;
	}

	@Override
	public V wrap(E entity) {
		V result = ctor.apply(entity);
		return result;
	}	
}