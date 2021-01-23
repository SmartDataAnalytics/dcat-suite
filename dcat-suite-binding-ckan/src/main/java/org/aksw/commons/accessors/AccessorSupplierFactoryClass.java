package org.aksw.commons.accessors;

import java.util.function.Function;

import org.aksw.commons.beans.model.ConversionService;
import org.aksw.commons.beans.model.EntityModel;
import org.aksw.commons.beans.model.EntityOps;
import org.aksw.commons.beans.model.PropertyOps;
import org.aksw.jena_sparql_api.mapper.impl.type.ConversionServiceSpringAdapter;
import org.springframework.context.support.ConversionServiceFactoryBean;

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
				throw new RuntimeException("Found accessor for " + name + " but argument type" + clazz + " not compatible with its type: " + propertyOps.getType());
			} else {
				result = obj -> new SingleValuedAccessorFromPropertyOps<T>(propertyOps, obj);
			}
		}
		
		return result;
	}
	
	public static <S> AccessorSupplierFactoryClass<S> create(Class<S> entityClass) {

		ConversionServiceFactoryBean bean = new ConversionServiceFactoryBean();
        bean.afterPropertiesSet();

        ConversionService conversionService = new ConversionServiceSpringAdapter(bean.getObject());

        // TODO Add a converter between java.sql.Date and Calendar
//		Data date;
//		Calendar cal = Calendar.getInstance();
//		cal.setTime(date);
//		
//		new XSDDateTime(cal)


        
		EntityOps entityOps = EntityModel.createDefaultModel(entityClass, conversionService);

		AccessorSupplierFactoryClass<S> result = new AccessorSupplierFactoryClass<>(entityOps);

		return result;
	}
}