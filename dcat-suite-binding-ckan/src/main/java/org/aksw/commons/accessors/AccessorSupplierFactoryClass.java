package org.aksw.commons.accessors;

import java.util.function.Function;

import org.aksw.commons.beans.model.ConversionService;
import org.aksw.commons.beans.model.EntityModel;
import org.aksw.commons.beans.model.EntityOps;
import org.aksw.commons.beans.model.PropertyOps;


public class AccessorSupplierFactoryClass<S>
	implements AccessorSupplierFactory<S>
{
	protected EntityOps entityOps;
	
	public AccessorSupplierFactoryClass(EntityOps entityOps) {
		super();
		this.entityOps = entityOps;
	}

	@Override
	public <T> Function<S, ? extends SingleValuedAccessor<T>> createAccessor(String name, Class<T> clazz) {
		Function<S, ? extends SingleValuedAccessor<T>> result;

		PropertyOps propertyOps = entityOps.getProperty(name);
		if (propertyOps == null) {
			throw new RuntimeException("No accessor found for " + name);
		} else {
			if(!propertyOps.acceptsType(clazz)) {
				throw new RuntimeException("Found accessor for " + name + " but the requested argument type" + clazz + " not compatible with its type: " + propertyOps.getType());
			} else {
				result = obj -> new SingleValuedAccessorFromPropertyOps<T>(propertyOps, obj);
			}
		}
		
		return result;
	}
	

	/**
	 * Wrap an entity model such that it can act as an accessor supplier factory
	 * 
	 * @param <S>
	 * @param entityClass
	 * @param conversionService
	 * @return
	 */
	public static <S> AccessorSupplierFactoryClass<S> create(Class<S> entityClass, ConversionService conversionService) {
		EntityOps entityOps = EntityModel.createDefaultModel(entityClass, conversionService);
		AccessorSupplierFactoryClass<S> result = new AccessorSupplierFactoryClass<>(entityOps);

		return result;
	}
}