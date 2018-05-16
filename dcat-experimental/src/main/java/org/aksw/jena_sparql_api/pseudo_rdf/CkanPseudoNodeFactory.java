package org.aksw.jena_sparql_api.pseudo_rdf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import org.aksw.dcat.ap.binding.ckan.domain.impl.PropertySource;
import org.aksw.dcat.ap.binding.ckan.domain.impl.PropertySourceCkanDataset;
import org.aksw.dcat.ap.binding.ckan.domain.impl.PropertySourceCkanResource;
import org.aksw.dcat.ap.binding.ckan.domain.impl.PropertySourcePrefix;
import org.aksw.dcat.ap.binding.ckan.domain.impl.SingleValuedAccessorFromPropertyOps;
import org.aksw.dcat.ap.domain.api.Spdx;
import org.aksw.dcat.ap.playground.main.SetFromJsonListString;
import org.aksw.dcat.util.view.CastConverter;
import org.aksw.dcat.util.view.CollectionAccessorFromCollection;
import org.aksw.dcat.util.view.CollectionAccessorFromCollectionValue;
import org.aksw.dcat.util.view.CollectionAccessorSingleton;
import org.aksw.dcat.util.view.CollectionFromConverter;
import org.aksw.dcat.util.view.LazyCollection;
import org.aksw.dcat.util.view.LazyMap;
import org.aksw.dcat.util.view.SetFromCkanExtras;
import org.aksw.dcat.util.view.SingleValuedAccessor;
import org.aksw.dcat.util.view.SingleValuedAccessorFromCollection;
import org.aksw.dcat.util.view.SingleValuedAccessorFromMap;
import org.aksw.dcat.util.view.SingleValuedAccessorImpl;
import org.aksw.jena_sparql_api.beans.model.EntityModel;
import org.aksw.jena_sparql_api.beans.model.EntityOps;
import org.aksw.jena_sparql_api.beans.model.PropertyOps;
import org.aksw.jena_sparql_api.utils.model.NodeMapper;
import org.aksw.jena_sparql_api.utils.model.NodeMapperFactory;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.vocabulary.FOAF;
import org.apache.jena.vocabulary.DCAT;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.VCARD;
import org.apache.jena.vocabulary.VCARD4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ConversionServiceFactoryBean;
import org.springframework.core.convert.ConversionService;

import eu.trentorise.opendata.jackan.model.CkanDataset;
import eu.trentorise.opendata.jackan.model.CkanResource;

//class ResourceAttributeComparator
//	implements Comparator<RDFNode>
//{
//	protected Property p;
//
//	@Override
//	public int compare(RDFNode a, RDFNode b) {
//		boolean result = a.isResource() && b.isResource();
//		
//		result = result && Objects.equals(a.asResource().getProperty(p), b.asResource().getProperty(p));
//		return result;
//	}
//	
//}

//class PropertySourceFactory<T> {
//	PropertySource wrap(T entity);
//}
class ModelMappingRegistry {
	
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

class PredicateMappingRegistry {
	protected Map<String, Function<PropertySource, PseudoRdfProperty>> predicateMappings;
	
	
	void put(String predicate, Function<PropertySource, PseudoRdfProperty> fn) {
		
	}
	
	Function<PropertySource, PseudoRdfProperty> get(String predicate) {
		return predicateMappings.get(predicate);
	}
}

//class TypeMappingRegistry<S> {
//	protected <T> BiFunction<String, Class<T>, Function<S, SingleValuedAccessor<T>>> accessorSupplierFactory;
//
//	//protected Map<String, Function<PropertySource, PseudoRdfProperty>> ckanDatasetAccessors = new HashMap<>();
//}

interface AccessorSupplierFactory<S> {
	<T> Function<S, ? extends SingleValuedAccessor<T>> createAccessor(String name, Class<T> clazz);
}


class AccessorSupplierFactoryDelegate<S>
	implements AccessorSupplierFactory<S>
{
	protected AccessorSupplierFactory<S> delegate;

	public AccessorSupplierFactoryDelegate(AccessorSupplierFactory<S> delegate) {
		super();
		this.delegate = delegate;
	}

	@Override
	public <T> Function<S, ? extends SingleValuedAccessor<T>> createAccessor(String name, Class<T> clazz) {
		Function<S, ? extends SingleValuedAccessor<T>> result = delegate.createAccessor(name, clazz);
		return result;
	}
}

class AccessorSupplierCkanDataset
	extends AccessorSupplierFactoryDelegate<CkanDataset>
{
	public AccessorSupplierCkanDataset(AccessorSupplierFactory<CkanDataset> delegate) {
		super(delegate);
	}

	@Override
	public <T> Function<CkanDataset, ? extends SingleValuedAccessor<T>> createAccessor(String name, Class<T> clazz) {
		Function<CkanDataset, ? extends SingleValuedAccessor<T>> result;

		String[] parts = name.split("\\:", 2);

		String namespace = parts.length == 2 ? parts[0] : "";
		String localName = parts.length == 2 ? parts[1] : parts[0];

		if(namespace.equals("extra")) {
			// FIXME hack ... need a converter in general
			result = ckanDataset -> (SingleValuedAccessor<T>)new SingleValuedAccessorFromCollection<>(new SetFromCkanExtras(ckanDataset, localName));
		} else {
			result = delegate.createAccessor(localName, clazz);
		}
		
		return result;
	}
}


class AccessorSupplierCkanResource
	extends AccessorSupplierFactoryDelegate<CkanResource>
{
	public AccessorSupplierCkanResource(AccessorSupplierFactory<CkanResource> delegate) {
		super(delegate);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> Function<CkanResource, ? extends SingleValuedAccessor<T>> createAccessor(String name, Class<T> clazz) {
		Function<CkanResource, ? extends SingleValuedAccessor<T>> result;
	
		String[] parts = name.split("\\:", 2);
	
		String namespace = parts.length == 2 ? parts[0] : "";
		String localName = parts.length == 2 ? parts[1] : parts[0];
	
		if(namespace.equals("others")) {
			// FIXME hack ... need a converter in general
//			result = null; //(SingleValuedAccessor<T>)new SingleValuedAccessorFromSet<>(new SetFromCkanExtras(ckanResource, localName));
			result = ckanResource -> (SingleValuedAccessor<T>)new SingleValuedAccessorFromMap<>(
					new LazyMap<>(
							new SingleValuedAccessorImpl<>(ckanResource::getOthers, ckanResource::setOthers), HashMap::new),
							localName);

//			CkanResource x = new CkanResource();
//			result.apply(x).set((T)"http://foobar");
//			System.out.println(x.getOthers());
			
			
		} else {
			result = delegate.createAccessor(localName, clazz);
		}
		
		return result;
	}
}


class AccessorSupplierFactoryClass<S>
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

class PropertySourceFromAccessorSupplier<S>
	implements PropertySource
{
	protected S source;
	protected AccessorSupplierFactory<S> accessorSupplierFactory;
	//protected Table<String, Class<?>, >
	
	public PropertySourceFromAccessorSupplier(S source, AccessorSupplierFactory<S> accessorSupplierFactory) {
		super();
		Objects.requireNonNull(source);
		Objects.requireNonNull(accessorSupplierFactory);
		
		this.source = source;
		this.accessorSupplierFactory = accessorSupplierFactory;
	}

	@Override
	public S getSource() {
		return source;
	}

	@Override
	public <T> SingleValuedAccessor<T> getProperty(String name, Class<T> valueType) {
		Function<S, ? extends SingleValuedAccessor<T>> accessorSupplier = accessorSupplierFactory.createAccessor(name, valueType);
		Objects.requireNonNull(accessorSupplier, "Could not obtain an access supplier for attribute '" + name + "' of type " + valueType + " in " + accessorSupplierFactory);
		SingleValuedAccessor<T> result = accessorSupplier.apply(source);

		return result;
	}
	
}

interface PseudoNodeSchema<S> {
	Class<S> getEntityClass();
	AccessorSupplierFactory<S> getAccessorSupplierFactory(); 		
	PredicateMappingRegistry getPredicateMappings();

}


public class CkanPseudoNodeFactory {	
	private static final Logger logger = LoggerFactory.getLogger(CkanPseudoNodeFactory.class);
	
	//public Map<String, Function<>> targetToAccessorSupplier;
	
	public static ModelMappingRegistry modelMappingRegistry;

	
	public Map<String, Function<PropertySource, PseudoRdfProperty>> ckanDatasetAccessors = new HashMap<>();
	public Map<String, Function<PropertySource, PseudoRdfProperty>> ckanResourceAccessors = new HashMap<>();

	public Map<String, Function<PropertySource, PseudoRdfProperty>> ckanDatasetPublisherAccessors = new HashMap<>();
	
	public Map<String, Function<PropertySource, PseudoRdfProperty>> ckanDatasetContactPointAccessors = new HashMap<>();
	public Map<String, Function<PropertySource, PseudoRdfProperty>> ckanResourceHashAccessors = new HashMap<>();

	// Function to obtain a key from datasets/distributions beyond their URIs; used to identify equivalences when updating resources
	protected Function<RDFNode, Node> getDatasetKey = r -> !r.isResource() ? null : Optional.ofNullable(r.asResource().getProperty(DCTerms.title)).map(p -> p.getObject().asNode()).orElse(null);
	protected Function<RDFNode, Node> getDistributionKey = r -> !r.isResource() ? null : Optional.ofNullable(r.asResource().getProperty(DCTerms.title)).map(p -> p.getObject().asNode()).orElse(null);
	
	
	
	private static class InstanceHolder {
		public static final CkanPseudoNodeFactory instance = new CkanPseudoNodeFactory(); 
	}
	
	protected static CkanPseudoNodeFactory instanceHolder;
	
	public static CkanPseudoNodeFactory get() {
		return InstanceHolder.instance;
	}
	
	public CkanPseudoNodeFactory() {
		initDefaults();
		Model mappingModel = RDFDataMgr.loadModel("dcat-ap-ckan-mapping.ttl");
		loadMappings(mappingModel);
	}
	

	/**
	 * Mapping for single valued properties
	 * 
	 * @param registry
	 * @param p
	 * @param attr
	 * @param attrClass
	 * @param nodeMapper
	 */
	public static <T> void addSimpleMapping(
			Map<String, Function<PropertySource, PseudoRdfProperty>> registry,
			String p, String attr, Class<T> attrClass, RdfType<T> rdfType, NodeMapper<T> nodeMapper) {		

		registry.put(p,
				s -> new PseudoRdfObjectPropertyImpl<>(
							s.getPropertyAsSet(attr, attrClass),
							rdfType,
							nodeMapper));
	}


	public static void addStringMapping(Map<String, Function<PropertySource, PseudoRdfProperty>> registry, String p, String attr) {
		addSimpleMapping(registry, p, attr, String.class, new RdfTypeRDFDatatype<>(String.class), NodeMapperFactory.string);
	}
	

	public static <T> void addCollectionMapping(
			AccessorSupplierFactory<?> accessorSupplierFactory,
			Map<String, Function<PropertySource, PseudoRdfProperty>> registry, String p, String attr, TypeMapper typeMapper, String dtypeUri) {
		Class<T> clazz;
		NodeMapper<T> nodeMapper;
		RdfType<T> rdfType;
		if(dtypeUri.equals(MappingVocab.r2rmlIRI.getURI())) {
			clazz = (Class<T>)String.class;
			nodeMapper = (NodeMapper<T>)NodeMapperFactory.uriString;
			rdfType = (RdfType<T>)new RdfTypeUri();
//			addUriStringMapping(registry, dtypeUri, attr);
		} else {
			RDFDatatype rdfDatatype = typeMapper.getTypeByName(dtypeUri);
			if(rdfDatatype == null) {
				throw new RuntimeException("Provided TypeMapper did not contain a RDFDatatype for " + dtypeUri);
			}
			clazz = (Class<T>)rdfDatatype.getJavaClass();
			rdfType = new RdfTypeRDFDatatype<>(clazz);
			nodeMapper = NodeMapperFactory.from(clazz);
		}
		
		validateAccessor(accessorSupplierFactory, attr, Collection.class);
		
		addCollectionMapping(registry, p, attr, clazz, rdfType, nodeMapper);
	
	}
	
	public static <T> void addExtraJsonArrayMapping(
			AccessorSupplierFactory<?> accessorSupplierFactory,
			Map<String, Function<PropertySource, PseudoRdfProperty>> registry, String p, String attr, TypeMapper typeMapper, String dtypeUri) {
		Class<T> clazz;
		NodeMapper<T> nodeMapper;
		RdfType<T> rdfType;
		if(dtypeUri.equals(MappingVocab.r2rmlIRI.getURI())) {
			clazz = (Class<T>)String.class;
			nodeMapper = (NodeMapper<T>)NodeMapperFactory.uriString;
			rdfType = (RdfType<T>)new RdfTypeUri();
//			addUriStringMapping(registry, dtypeUri, attr);
		} else {
			RDFDatatype rdfDatatype = typeMapper.getTypeByName(dtypeUri);
			if(rdfDatatype == null) {
				throw new RuntimeException("Provided TypeMapper did not contain a RDFDatatype for " + dtypeUri);
			}
			clazz = (Class<T>)rdfDatatype.getJavaClass();
			rdfType = new RdfTypeRDFDatatype<>(clazz);
			nodeMapper = NodeMapperFactory.from(clazz);
		}
		
		validateAccessor(accessorSupplierFactory, attr, String.class);
		
		addExtraJsonArrayMapping(registry, p, attr, clazz, rdfType, nodeMapper);
	}

	
//	public void foo() {
//		PseudoNodeSchema<CkanDataset> pseudoNodeSchema = modelMappingRegistry.getPseudoNodeSchema(CkanDataset.class);
//		
////		Map<String, Class<?>> javaBinding = new HashMap<>();
////		javaBinding.put(DCAT.Dataset.getURI(), CkanDataset.class);
////		javaBinding.put(DCAT.Distribution.getURI(), CkanResource.class);
////		
////		Map<Class<?>, BiFunction<String, Class, Function<Object, SingleValuedAccessor>>> map = new HashMap<>();
////		map.put(CkanDataset.class, CkanUtils::getSingleValuedAccessorSupplierDataset);
////		map.put(CkanResource.class, CkanUtils::getSingleValuedAccessorSupplierResource);
//
//		
//		// Check whether the entity associated with the target type supports the attribute with the requested sname and type
//		AccessorSupplierFactory<?>
//		Function<?, SingleValuedAccessor<?>> accessorFactory = accessorSupplierFactory.apply(attr, attrClass);
//
//		new PropertySourceFromAccessorSupplier<>(entity, accessorFactory);
//		
//
//		//AccessorSupplierFactory<CkanDataset> accessorSupplierFactory = modelMappingRegistry.getAccessorSupplierFactory(CkanDataset.class);		
//		//PredicateMappingRegistry predicateMappings = modelMappingRegistry.getPredicateMappingRegistry(DCAT.Dataset);
//		
//		//TODO register at modelMappingRegistry new AccessorSupplierCkanDataset(AccessorSupplierFactoryClass.create(CkanDataset.class));
//		
//		//new PropertySourceFromAccessorSupplier<>(ckanDataset, accessorSupplierFactory);
//		
//	}
	
	public static void validateAccessor(
			AccessorSupplierFactory<?> accessorSupplierFactory,
			String attr, Class<?> attrClass) {
			
		Function<?, ? extends SingleValuedAccessor<?>> accessorSupplier = accessorSupplierFactory.createAccessor(attr, attrClass);
		if(accessorSupplier == null) {
			throw new RuntimeException("Accessor for attribute " + attr + " of type " + attrClass + " not possible with " + accessorSupplierFactory);
		}
	}

	public static <T> void addLiteralMapping(
			AccessorSupplierFactory<?> accessorSupplierFactory,
			Map<String, Function<PropertySource, PseudoRdfProperty>> registry,
			String p, String attr, TypeMapper typeMapper, String dtypeUri) {

		
		if(dtypeUri.equals(MappingVocab.r2rmlIRI.getURI())) {
			validateAccessor(accessorSupplierFactory, attr, String.class);			
			addUriStringMapping(registry, p, attr);
		} else {
			RDFDatatype rdfDatatype = typeMapper.getTypeByName(dtypeUri);
			if(rdfDatatype == null) {
				throw new RuntimeException("Provided TypeMapper did not contain a RDFDatatype for " + dtypeUri);
			}
			Class<T> clazz = (Class<T>) rdfDatatype.getJavaClass();
			NodeMapper<T> nodeMapper = NodeMapperFactory.from(clazz);
			
			validateAccessor(accessorSupplierFactory, attr, clazz);
			addSimpleMapping(registry, p, attr, clazz, new RdfTypeRDFDatatype<>(clazz), nodeMapper);
		}
	}

	public static void addUriStringMapping(Map<String, Function<PropertySource, PseudoRdfProperty>> registry, String p, String attr) {
		addSimpleMapping(registry, p, attr, String.class, new RdfTypeUri(), NodeMapperFactory.uriString);
	}

	
	/**
	 * Mapping for propertries with collections of literal values
	 * 
	 * @param registry
	 * @param p
	 * @param attr
	 * @param attrClass
	 * @param nodeMapper
	 */
	public static <T> void addCollectionMappingDirect(Map<String, Function<PropertySource, PseudoRdfProperty>> registry, Property p, String attr, Class<T> attrClass) {
		registry.put(p.getURI(),
				s -> new PseudoRdfObjectPropertyImpl<>(
							new CollectionAccessorFromCollectionValue<>(s.getCollectionProperty(attr, attrClass)),
							new RdfTypeRDFDatatype<>(attrClass),
							NodeMapperFactory.from(attrClass)));	
	}

	public static <T> void addCollectionMapping(
			Map<String, Function<PropertySource, PseudoRdfProperty>> registry, String p, String attr, Class<T> attrClass, RdfType<T> rdfType, NodeMapper<T> nodeMapper) {
		
		registry.put(p,
				s -> new PseudoRdfObjectPropertyImpl<>(
						new CollectionAccessorFromCollection<>(
								new LazyCollection<>(
								s.getCollectionProperty(attr, attrClass),
								ArrayList::new, true)),
							rdfType,
							nodeMapper));	
	}


	public static <T> void addExtraJsonArrayMapping(
			Map<String, Function<PropertySource, PseudoRdfProperty>> registry, String p, String attr, Class<T> clazz, RdfType<T> rdfType, NodeMapper<T> nodeMapper) {
		
		registry.put(p,
				s -> new PseudoRdfObjectPropertyImpl<>(
						new CollectionAccessorFromCollection<>(
								new CollectionFromConverter<>(
										new SetFromJsonListString(s.getProperty(attr, String.class), true),
										new CastConverter<>())),
							//new RdfTypeUri(),
							//new RdfTypeRDFDatatype<>(String.class),
						    //new RdfTypeRDFDatatype<>(clazz),
							rdfType,
							nodeMapper));	
	}


	public static <T> void addRelation(
			Map<String, Function<PropertySource, PseudoRdfProperty>> registry,
			Property p,
			String attr,
			Class<T> attrClass,
			Function<T, PropertySource> objToPropertySource,
			Map<String, Function<PropertySource, PseudoRdfProperty>> targetRegistry) {

		registry.put(p.getURI(),
				s -> new PseudoRdfObjectPropertyImpl<>(
						new CollectionAccessorFromCollection<>(
								new LazyCollection<>(
								s.getCollectionProperty(attr, attrClass),
								ArrayList::new, true)),
						new RdfTypeSimple<>(attrClass),
						new PseudoNodeMapper<>(attrClass,
								ckanResource ->  objToPropertySource.apply(ckanResource),
								targetRegistry)));		
	}
	
	
	public static <T> Function<PropertySource, PseudoRdfProperty> createLiteralAccessor(String attrName, Class<T> clazz) {
		Function<PropertySource, PseudoRdfProperty> result = 
			s -> new PseudoRdfObjectPropertyImpl<>(
					s.getPropertyAsSet(attrName, clazz),
					new RdfTypeRDFDatatype<>(clazz),
					NodeMapperFactory.from(clazz));

			return result;
	}

	Map<Class<?>, AccessorSupplierFactory<?>> targetToAccessorFactory = new HashMap<>();

	public void loadMappings(Model mappingModel) {
		
		
		List<Resource> mappings = mappingModel.listObjectsOfProperty(MappingVocab.mapping)
				.filterKeep(RDFNode::isResource).mapWith(RDFNode::asResource).toList();

		//Map<String, MappingProcessor> mappingProcessorRegistry = new HashMap<>();
				
		
		Map<RDFNode, Map<String, Function<PropertySource, PseudoRdfProperty>>> targetToAccessors = new HashMap<>();
		targetToAccessors.put(DCAT.Dataset, ckanDatasetAccessors);
		targetToAccessors.put(DCAT.Distribution, ckanResourceAccessors);
		targetToAccessors.put(FOAF.Agent, ckanDatasetPublisherAccessors);
		targetToAccessors.put(VCARD4.Kind, ckanDatasetContactPointAccessors);
		targetToAccessors.put(Spdx.Checksum, ckanResourceHashAccessors);

		
		Map<RDFNode, Class<?>> targetToEntityClass = new HashMap<>();
		targetToEntityClass.put(DCAT.Dataset, CkanDataset.class);
		targetToEntityClass.put(DCAT.Distribution, CkanResource.class);
		targetToEntityClass.put(FOAF.Agent, CkanDataset.class);
		targetToEntityClass.put(VCARD4.Kind, CkanDataset.class);
		targetToEntityClass.put(Spdx.Checksum, CkanResource.class);
		
		for(Resource mapping : mappings) {
			MappingUtils.applyMappingDefaults(mapping);

			try { 
				processMapping(targetToAccessors, targetToEntityClass, mapping);
			} catch(Exception e) {
				logger.warn("Skipping mapping due to error", e);
			}
		}
		
	}

	private void processMapping(
			Map<RDFNode, Map<String, Function<PropertySource, PseudoRdfProperty>>> targetToAccessors,
			Map<RDFNode, Class<?>> targetToEntityClass, Resource mapping) {
		//RDFDataMgr.write(System.out, mapping.getModel(), RDFFormat.TURTLE_PRETTY);

		String mappingType = mapping.getPropertyResourceValue(RDF.type).getURI();
		//type.getURI();
		
		// Get the mapping processor for the type
		//MappingProcessor mappingProcessor = mappingProcessorRegistry.get(type);

		RDFNode target = mapping.getProperty(MappingVocab.target).getObject();
		
		Class<?> targetEntityClass = targetToEntityClass.get(target);
		AccessorSupplierFactory<?> accessorSupplierFactory = targetToAccessorFactory.get(targetEntityClass);
		
		
		// Resolve the target to the mapping registry
		Map<String, Function<PropertySource, PseudoRdfProperty>> mappingRegistry = targetToAccessors.computeIfAbsent(target, (k) -> new HashMap<>());
		
		
		TypeMapper typeMapper = TypeMapper.getInstance();
		if(mappingType.equals(MappingVocab.LiteralMapping.getURI())) {

			Resource dtype = mapping.getProperty(MappingVocab.type).getObject().asResource();				
			String predicate = mapping.getProperty(MappingVocab.predicate).getObject().asResource().getURI();
			String key = mapping.getProperty(MappingVocab.key).getString();
			
			
			//typeMapper.getTypeByName(dtype.getURI());
			
			System.out.println("Adding " + predicate + " -> " + key);
			CkanPseudoNodeFactory.addLiteralMapping(accessorSupplierFactory, mappingRegistry, predicate, key, typeMapper, dtype.getURI());
			//if(type.get)
			
		} else if(mappingType.equals(MappingVocab.CollectionMapping.getURI())) {
			Resource dtype = mapping.getProperty(MappingVocab.type).getObject().asResource();				
			String predicate = mapping.getProperty(MappingVocab.predicate).getObject().asResource().getURI();
			String key = mapping.getProperty(MappingVocab.key).getString();

			CkanPseudoNodeFactory.addCollectionMapping(accessorSupplierFactory, mappingRegistry, predicate, key, typeMapper, dtype.getURI());
			
		} else if(mappingType.equals(MappingVocab.JsonArrayMapping.getURI())) {
			Resource dtype = mapping.getProperty(MappingVocab.type).getObject().asResource();				
			String predicate = mapping.getProperty(MappingVocab.predicate).getObject().asResource().getURI();
			String key = mapping.getProperty(MappingVocab.key).getString();
			
			CkanPseudoNodeFactory.addExtraJsonArrayMapping(accessorSupplierFactory, mappingRegistry, predicate, key, typeMapper, dtype.getURI());
		} else {
			logger.warn("Unknown mapping type: " + mappingType);
		}
		
		//mappingProcessor.apply(mapping, mappingRegistry);
	}
	
	public void initDefaults() {

		AccessorSupplierCkanDataset ckanDatasetAccessor = new AccessorSupplierCkanDataset(AccessorSupplierFactoryClass.create(CkanDataset.class));
		AccessorSupplierCkanResource ckanResourceAccessor = new AccessorSupplierCkanResource(AccessorSupplierFactoryClass.create(CkanResource.class));
		
		targetToAccessorFactory.put(CkanDataset.class, ckanDatasetAccessor);
		targetToAccessorFactory.put(CkanResource.class, ckanResourceAccessor);

		/* datasaset mappings */
		addStringMapping(ckanDatasetAccessors, DCTerms.title.getURI(), "title");
		addStringMapping(ckanDatasetAccessors, DCTerms.description.getURI(), "notes");
		addCollectionMapping(ckanDatasetAccessors, DCAT.keyword.getURI(), "tags", String.class, new RdfTypeRDFDatatype<>(String.class), NodeMapperFactory.string);		
		addExtraJsonArrayMapping(ckanDatasetAccessors, DCAT.theme.getURI(), "extra:theme", String.class, new RdfTypeUri(), NodeMapperFactory.uriString);
		addStringMapping(ckanDatasetAccessors, DCTerms.identifier.getURI(), "extra:identifier");
		
//		DcatDataset x;
//		x.keyw
		
		/**
		 * requesting properties by the value 
		 * 
		 */
		AccessorSupplierFactory<CkanDataset> ckanDatasetAccessorFactory = getAccessorFactory(CkanDataset.class);
		AccessorSupplierFactory<CkanResource> ckanResourceAccessorFactory = getAccessorFactory(CkanResource.class);

		addRelation(ckanDatasetAccessors, DCAT.distribution, "resources", CkanResource.class, ckanResource -> new PropertySourceFromAccessorSupplier<>(ckanResource, ckanResourceAccessorFactory), ckanResourceAccessors);
		
		/* dataset -> publisher */
		// Given the property source s, return an accessor to itself
		// For each source s (itself), wrap it as a new PseudoRdfResource
		ckanDatasetAccessors.put(DCTerms.publisher.getURI(),
				s -> new PseudoRdfObjectPropertyImpl<>(
						new CollectionAccessorSingleton<>((CkanDataset)s.getSource()),
//						new SingleValuedAccessorDirect<>(new CollectionFromSingleValuedAccessor<>(new SingleValuedAccessorDirect<>()),
						new RdfTypeSimple<>(CkanDataset::new),
						new PseudoNodeMapper<>(CkanDataset.class,
								ckanDataset ->  new PropertySourcePrefix("extra:publisher_",  new PropertySourceFromAccessorSupplier<>(ckanDataset, ckanDatasetAccessorFactory)),
								ckanDatasetPublisherAccessors)));


		ckanDatasetAccessors.put(DCAT.contactPoint.getURI(),
				s -> new PseudoRdfObjectPropertyImpl<>(
						new CollectionAccessorSingleton<>((CkanDataset)s.getSource()),
						new RdfTypeSimple<>(CkanDataset::new),
						new PseudoNodeMapper<>(CkanDataset.class,
								ckanDataset -> new PropertySourceFromAccessorSupplier<>(ckanDataset, ckanDatasetAccessorFactory),
								ckanDatasetContactPointAccessors)));

		//new PropertySourcePrefix("extra:contact_",
		
		ckanResourceAccessors.put(Spdx.checksum.getURI(),
				s -> new PseudoRdfObjectPropertyImpl<>(
						new CollectionAccessorSingleton<>((CkanResource)s.getSource()),
						new RdfTypeSimple<>(CkanResource::new),
						new PseudoNodeMapper<>(CkanResource.class,
								ckanResource -> new PropertySourceFromAccessorSupplier<>(ckanResource, ckanResourceAccessorFactory),
								ckanResourceHashAccessors)));

//		ckanDatasetAccessors.put(DCTerms.publisher.getURI(),
//				s -> new PseudoRdfObjectPropertyImpl<>(
//						new SingleValuedAccessorDirect<>(new CollectionFromSingleValuedAccessor<>(new SingleValuedAccessorDirect<>(s))),
//						ss -> new PseudoNode(
//								new PropertySourcePrefix("extra:publisher_", ss), ckanDatasetPublisherAccessors)
//						));
		
//		ckanDatasetAccessors.put(DCTerms.publisher.getURI(),
//				s -> new PseudoRdfObjectPropertyImpl<>(
//						// TODO simplify this: get the "publisher" field, treat it as a set, and wrap it as an accessor
//						new SingleValuedAccessorFromSet<>(new SetFromSingleValuedAccessor<>(s.getProperty("publisher", String.class))),
//						// Map the backing ckanResource to a resource view
//						ckanDataset -> new PseudoRdfResourceImpl(
//								new PropertySourcePrefix("publisher_", new PropertySourceCkanDataset(ckanDataset)), ckanDatasetPublisherAccessors)
//						));

		
		/*
		 * Publisher
		 * 
		 */
		//schema.registerLiteral(FOAF.name, String.class)
		
//		ckanDatasetPublisherAccessors.put(FOAF.name.getURI(),
//				s -> Optional.ofNullable(s.getProperty("name", String.class)).map(PseudoRdfLiteralPropertyImpl::new).orElse(null));

		ckanDatasetPublisherAccessors.put(FOAF.name.getURI(), createLiteralAccessor("name", String.class));

//		ckanDatasetPublisherAccessors.put(FOAF.name.getURI(),
//				s -> new PseudoRdfObjectPropertyImpl<>(
//							s.getPropertyAsSet("name", String.class),
//							new RdfTypeRDFDatatype<>(String.class),
//							NodeMapperFactory.string));
	
		
		/*
		 * distribution mappings 
		 */
		
//		ckanResourceAccessors.put(DCTerms.description.getURI(),
//				s -> Optional.ofNullable(s.getProperty("description", String.class)).map(PseudoRdfLiteralPropertyImpl::new).orElse(null));
//		ckanResourceAccessors.put(DCTerms.description.getURI(),
//				s -> new PseudoRdfObjectPropertyImpl<>(
//							new SingleValuedAccessorDirect<>(
//									new CollectionFromSingleValuedAccessor<>(
//											s.getProperty("description", String.class))),
//							NodeMapperFactory.string));
		ckanResourceAccessors.put(DCTerms.description.getURI(), createLiteralAccessor("description", String.class));

//		ckanResourceAccessors.put(DCTerms.description.getURI(),
//				s -> new PseudoRdfObjectPropertyImpl<>(
//							s.getPropertyAsSet("description", String.class),
//							NodeMapperFactory.string));
	}

	
//	AccessorSupplierFactoryClass<CkanDataset> accessorSupplierFactory = AccessorSupplierFactoryClass.create(CkanDataset.class);

	//ModelMappingRegistry modelMappingRegistry = new ModelMappingRegistry();

	public <S> AccessorSupplierFactory<S> getAccessorFactory(Class<S> clazz) {
		AccessorSupplierFactory<S> result = (AccessorSupplierFactory<S>)targetToAccessorFactory.get(clazz);
		return result;
	}
	
	public PseudoNode createDataset() {
		CkanDataset ckanDataset = new CkanDataset();

		AccessorSupplierFactory<CkanDataset> accessorFactory = getAccessorFactory(CkanDataset.class);
		PropertySource propertySource = new PropertySourceFromAccessorSupplier<CkanDataset>(ckanDataset, accessorFactory);
		
		PseudoNode result = new PseudoNode(propertySource, ckanDatasetAccessors);
		return result;
	}
	
	public PseudoNode createDistribution() {
		CkanResource ckanResource = new CkanResource();

		AccessorSupplierFactory<CkanResource> accessorFactory = getAccessorFactory(CkanResource.class);
		PropertySource propertySource = new PropertySourceFromAccessorSupplier<CkanResource>(ckanResource, accessorFactory);
		
		PseudoNode result = new PseudoNode(propertySource, ckanResourceAccessors);
		return result;
	}
	
	
}