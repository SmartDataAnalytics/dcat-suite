package org.aksw.dcat.util.view;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

public class LazyMap<K, V, C extends Map<K, V>> extends AbstractMap<K, V> {
	protected SingleValuedAccessor<C> accessor;
	protected Supplier<? extends C> ctor;
	protected boolean setNullOnEmpty;

	public LazyMap(SingleValuedAccessor<C> accessor, Supplier<? extends C> ctor) {
		this(accessor, ctor, true);
	}

	public LazyMap(SingleValuedAccessor<C> accessor, Supplier<? extends C> ctor, boolean setNullOnEmpty) {
		super();
		this.accessor = accessor;
		this.ctor = ctor;
		this.setNullOnEmpty = setNullOnEmpty;
	}

	@Override
	public V put(K k, V v) {
		C backend = accessor.get();

		if (backend == null) {
			backend = ctor.get();
			accessor.set(backend);
		}

		backend.put(k, v);
		
		// If v == null, we may unset the key's value causing the map to become empty
		checkUnset(backend);

		return v;
	}

	public void checkUnset(C backend) {
		if (setNullOnEmpty && backend.isEmpty()) {
			accessor.set(null);
		}
	}

	@Override
	public boolean remove(Object k, Object v) {
		C backend = accessor.get();
		boolean result = backend == null ? false : backend.remove(k, v);

		checkUnset(backend);
		return result;
	}

	@Override
	public boolean containsKey(Object k) {
		C backend = accessor.get();

		boolean result = backend == null ? false : backend.containsKey(k);
		return result;
	}

	@Override
	public Set<Entry<K, V>> entrySet() {
		Map<K, V> backend = accessor.get();

		Set<Entry<K, V>> set = Optional.ofNullable(backend).orElse(Collections.emptyMap()).entrySet();

		return set;
//		return new SinglePrefetchIterator<T>() {
//			@Override
//			protected T prefetch() throws Exception {
//				while (baseIt.hasNext()) {
//					T b = baseIt.next();
//					return b;
//				}
//				return finish();
//			}
//
//			@Override
//			public void doRemove() {
//				baseIt.remove();
//
//				checkUnset(backend);
//			}
//		};
	}

	@Override
	public int size() {
		C backend = accessor.get();
		int result = backend == null ? 0 : backend.size();
		return result;
	}

	public static Set<String> test;

	public static void main(String[] args) {

		SingleValuedAccessor<Set<String>> accessor = new SingleValuedAccessorImpl<>(() -> LazyCollection.test,
				val -> LazyCollection.test = val);

		Collection<String> tmp = new LazyCollection<>(accessor, HashSet::new, true);

		System.out.println("Content: " + tmp);
		System.out.println("Test: " + test);

		System.out.println("Adding item");
		tmp.add("Hello");

		System.out.println("Adding item");
		tmp.add("World");

		System.out.println("Content: " + tmp);
		System.out.println("Test: " + test);

		System.out.println("Removing item");
		tmp.remove("World");

		System.out.println("Content: " + tmp);
		System.out.println("Test: " + test);

		System.out.println("Removing item");
		tmp.remove("Hello");

		System.out.println("Content: " + tmp);
		System.out.println("Test: " + test);

		System.out.println("Adding item");
		tmp.add("World");

		System.out.println("Content: " + tmp);
		System.out.println("Test: " + test);

		System.out.println("Clearing");
		tmp.clear();

		System.out.println("Content: " + tmp);
		System.out.println("Test: " + test);

	}
}