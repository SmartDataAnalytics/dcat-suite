package org.aksw.dcat.ap.playground.main;

import java.util.Iterator;
import java.util.Set;

import org.aksw.commons.accessors.SingleValuedAccessorDirect;

public class MainDemoSetView {
	public static void main(String[] args) {
		SingleValuedAccessorDirect<String> bean = new SingleValuedAccessorDirect<>();
		
		Set<Object> obj = new SetFromJsonListString(bean, true);
		obj.add(1);
		obj.add("test");
		obj.add("hello");
		
		System.out.println(bean.get());
		System.out.println(obj.iterator().next().getClass());
		
		Iterator<Object> it = obj.iterator();
		it.next();
		it.remove();
		System.out.println(bean.get());
		it.next();
		it.remove();
		System.out.println(bean.get());
		it.next();
		it.remove();
		System.out.println(bean.get());
	}
}
