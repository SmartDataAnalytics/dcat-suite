package org.aksw.commons.accessors;

import java.util.function.Function;

import org.aksw.jena_sparql_api.beans.model.EntityModel;
import org.aksw.jena_sparql_api.beans.model.EntityOps;
import org.aksw.jena_sparql_api.beans.model.PropertyOps;
import org.springframework.context.support.ConversionServiceFactoryBean;
import org.springframework.core.convert.ConversionService;

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
		if(propertyOps != null && propertyOps.acceptsType(clazz)) {
			result = obj -> new SingleValuedAccessorFromPropertyOps<T>(propertyOps, obj);
		} else {
			result = null;
		}
		
		return result;
	}
	
	public static <S> AccessorSupplierFactoryClass<S> create(Class<S> entityClass) {

		ConversionServiceFactoryBean bean = new ConversionServiceFactoryBean();
        bean.afterPropertiesSet();

        ConversionService conversionService = bean.getObject();
		EntityOps entityOps = EntityModel.createDefaultModel(entityClass, conversionService);

		AccessorSupplierFactoryClass<S> result = new AccessorSupplierFactoryClass<>(entityOps);

		return result;
	}
}