package org.aksw.dcat.ap.trash;

import java.util.HashMap;
import java.util.Map;

public class PersonalityImpl<E, V>
	implements Personality<E, V>
{
	protected Map<Class<? extends V>, Implementation<E, ? extends V>> map = new HashMap<>();
	
	@Override
	public <X extends V> Implementation<E, X> getImplementation(Class<X> clazz) {
		Implementation<E, X> result = (Implementation<E, X>) map.get(clazz);
		return result;
	}

	@Override
	public <X extends V> void add(Class<X> clazz, Implementation<E, X> implementation) {
		map.put(clazz, implementation);
	}	
}