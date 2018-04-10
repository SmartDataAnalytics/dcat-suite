package org.aksw.dcat.ap.binding.ckan.domain.impl;

/**
 * Personality for wrapper types, such as Property source.
 * Unwraps the backing object and forwards the request to a personality for its type.
 * 
 * 
 * @author raven Apr 10, 2018
 *
 * @param <W> The wrapper type
 * @param <E> The entity to be forwarded to a different personality
 * @param <V> The requested view type
 */
//public class PersonalityDelegating<E, F, V>
//	implements Personality<E, V>
//{
//	protected Map<Class<E>, Personality<F, V>> wrapperTo
//
//	@Override
//	public <X extends V> Implementation<E, X> getImplementation(Class<X> clazz) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public <X extends V> void add(Class<X> clazz, Implementation<E, X> implementation) {
//		// TODO Auto-generated method stub
//		
//	}
//
//}
