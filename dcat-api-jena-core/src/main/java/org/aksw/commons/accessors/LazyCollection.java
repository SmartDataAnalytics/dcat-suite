package org.aksw.commons.accessors;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import org.aksw.commons.collections.SinglePrefetchIterator;

/**
 * Collection that forwards method calls to another one that is
 * only instantiated on addition of items if it did not yet exist
 * 
 * Delegates removals to it
 * 
 * @author raven Apr 9, 2018
 *
 * @param <T>
 */
public class LazyCollection<T, C extends Collection<T>>
	extends AbstractCollection<T>
{
	protected SingleValuedAccessor<C> accessor;
	protected Supplier<? extends C> ctor;
	protected boolean setNullOnEmpty;
	
	
	public LazyCollection(SingleValuedAccessor<C> accessor,
			Supplier<? extends C> ctor,
			boolean setNullOnEmpty) {
		super();
		this.accessor = accessor;
		this.ctor = ctor;
		this.setNullOnEmpty = setNullOnEmpty;
	}

	@Override
	public boolean add(T e) {
		C backend = accessor.get();
		
		if(backend == null) {
			backend = ctor.get();
			accessor.set(backend);
		}

		boolean result = backend.add(e);
		return result;
	}
	
	public void checkUnset(Collection<T> backend) {
		if(setNullOnEmpty && backend.isEmpty()) {
			accessor.set(null);
		}
	}
	
	@Override
	public boolean remove(Object o) {
		C backend = accessor.get();
		boolean result = backend == null ? false : backend.remove(o);
		
		checkUnset(backend);
		return result;
	}
	
	@Override
	public boolean contains(Object o) {
		C backend = accessor.get();

		boolean result = backend == null ? false : backend.contains(o);
		return result;
	}
	
	@Override
	public Iterator<T> iterator() {
		Collection<T> backend = accessor.get();
		
		Iterator<T> baseIt = Optional.ofNullable(backend).orElse(Collections.emptyList()).iterator();

		return new SinglePrefetchIterator<T>() {
			@Override
			protected T prefetch() throws Exception {
				while(baseIt.hasNext()) {
					T b = baseIt.next();
					return b;
				}
				return finish();
			}
			@Override
			public void doRemove() {
				baseIt.remove();

				checkUnset(backend);
			}
		};
	}

	@Override
	public int size() {
		Collection<T> backend = accessor.get();
		int result = backend == null ? 0 : backend.size();
		return result;
	}
	

	public static Set<String> test;
	
	public static void main(String[] args) {

		
		SingleValuedAccessor<Set<String>> accessor = new SingleValuedAccessorImpl<>(
				() -> LazyCollection.test,
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
