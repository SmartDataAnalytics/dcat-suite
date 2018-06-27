package org.aksw.dcat.ap.binding.ckan.rdf_view;

import java.util.Arrays;
import java.util.Collection;

interface A {
	Collection<? extends Number> get();
	
	void set();
	
	//void add(Number x);
}

interface B
	extends A
{
	Collection<Long> get();
	//void add(Long x);


	default void set() {
		System.out.println("set");
	}
	
}

class C
	implements B, A {
	
	public Collection<Long> get() { return Arrays.asList(1l); }

}

//interface C
//	extends B {
//	Collection<Double> get();	
//}

public class MainDeleteme {
	public static void main(String[] args) {
		B b = new C();
		A a = b;
		//a.get().add(Double.MIN_VALUE);
		//a.get().add(1l);
		//b.add
	}
}
