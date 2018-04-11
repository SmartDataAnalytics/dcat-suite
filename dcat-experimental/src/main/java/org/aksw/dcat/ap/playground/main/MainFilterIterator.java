package org.aksw.dcat.ap.playground.main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.aksw.commons.collections.SinglePrefetchIterator;
import org.apache.jena.ext.com.google.common.collect.Iterables;

public class MainFilterIterator {
	public static void main(String[] args) {
		//Collection<Integer> c = new ArrayList<>(Arrays.asList(7, 9, 6, 3, 8, 4, 5, 1, 0, 2));
		Collection<Integer> c = new ArrayList<>(Arrays.asList(2, 4, 6));
		Supplier<Iterable<Integer>> f = () -> filter(new ArrayList<>(c), i -> i.intValue() % 2 == 0);

		// should fail
		if(false) {
			Iterable<Integer> even = f.get();
			System.out.println(Iterables.toString(even));
			
			Iterator<Integer> it = even.iterator();
			it.remove();
			System.out.println(Iterables.toString(even));
		}		

		// should fail
		if(false) {
			Iterable<Integer> even = f.get();
			System.out.println(Iterables.toString(even));
			
			Iterator<Integer> it = even.iterator();
			it.next();
			it.next();
			it.hasNext();
			it.remove();
			System.out.println(Iterables.toString(even));
		}		

		// should work
		if(true) {
			Iterable<Integer> even = f.get();
			System.out.println(Iterables.toString(even));
			
			Iterator<Integer> it = even.iterator();
			it.next();
			it.next();
			it.remove();
			System.out.println(Iterables.toString(even));
		}		
	}
	
	public static <T> Iterable<T> filter(Iterable<T> i, Predicate<? super T> pred) {
		return () -> {
			return new SinglePrefetchIterator<T>() {
				final Iterator<T> baseIt = i.iterator();
				@Override
				protected T prefetch() throws Exception {
					T e = null;
					while(baseIt.hasNext()) {
						e = baseIt.next();
						if(pred.test(e)) {
							return e;
						}
					}
					return finish();
				}
				@Override
				public void doRemove() { baseIt.remove(); }
			};
		};
	}
}
