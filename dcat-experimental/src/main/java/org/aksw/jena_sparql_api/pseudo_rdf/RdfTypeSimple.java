package org.aksw.jena_sparql_api.pseudo_rdf;

import java.util.function.Supplier;

import org.apache.jena.rdf.model.RDFNode;

public class RdfTypeSimple<T>
	implements RdfType<T>
{
	protected Supplier<T> instanceSupplier;

	public RdfTypeSimple(Class<T> clazz) {
		this(() -> newInstance(clazz));
	}
	
	public static <T> T newInstance(Class<T> clazz) {
		T result;
		try {
			result = clazz.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
		return result;
	}

	
	public RdfTypeSimple(Supplier<T> instanceSupplier) {
		super();
		this.instanceSupplier = instanceSupplier;
	}


	/**
	 * A type might not be instantiable at all
	 */
	public boolean isInstantiable() {
		return true;
	}
	
	
	/**
	 * Check for whether a new instance can be created from the provided arguments
	 * 
	 */
	@Override
	public boolean canNewInstance(RDFNode args) {
		return true;
	}

	@Override
	public T newInstance(RDFNode args) {
		T result = instanceSupplier.get();
		return result;
	}

}
