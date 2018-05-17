package org.aksw.dcat.ap.binding.ckan.rdf_view;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.aksw.commons.accessors.AccessorSupplierFactory;
import org.aksw.commons.accessors.PropertySource;
import org.aksw.jena_sparql_api.pseudo_rdf.PseudoRdfProperty;

//class PropertySourceFactory<T> {
//	PropertySource wrap(T entity);
//}
public class ModelMappingRegistry {
	
	public <S> PseudoNodeSchema<S> getPseudoNodeSchema(Class<S> clazz) {
		return null;
	}
	
	//protected Map<String, Class<?>> type;
	protected Map<Class<?>, AccessorSupplierFactory<?>> javaClassToRegistry = new HashMap<>();

	
	protected Map<String, Function<PropertySource, PseudoRdfProperty>> ckanDatasetAccessors = new HashMap<>();

	
	public void put() {
		
	}
	
	public <S> AccessorSupplierFactory<S> getAccessorSupplierFactory(Class<S> clazz) {
		AccessorSupplierFactory<S> result = (AccessorSupplierFactory<S>)javaClassToRegistry.get(clazz);
		return result;
	}
	

	public PredicateMappingRegistry getPredicateMappingRegistry(Object id) {
		return null;
	}
	
}