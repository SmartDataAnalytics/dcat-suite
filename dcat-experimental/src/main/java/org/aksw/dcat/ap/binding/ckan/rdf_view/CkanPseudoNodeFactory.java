package org.aksw.dcat.ap.binding.ckan.rdf_view;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.aksw.commons.accessors.AccessorSupplierFactory;
import org.aksw.commons.accessors.AccessorSupplierFactoryClass;
import org.aksw.commons.accessors.CollectionAccessorFromCollection;
import org.aksw.commons.accessors.CollectionAccessorFromCollectionValue;
import org.aksw.commons.accessors.CollectionAccessorSingleton;
import org.aksw.commons.accessors.CollectionFromConverter;
import org.aksw.commons.accessors.LazyCollection;
import org.aksw.commons.accessors.PropertySource;
import org.aksw.commons.accessors.PropertySourcePrefix;
import org.aksw.commons.accessors.SingleValuedAccessor;
import org.aksw.commons.converters.CastConverter;
import org.aksw.dcat.ap.domain.api.Spdx;
import org.aksw.dcat.ap.playground.main.SetFromJsonListString;
import org.aksw.jena_sparql_api.pseudo_rdf.MappingUtils;
import org.aksw.jena_sparql_api.pseudo_rdf.MappingVocab;
import org.aksw.jena_sparql_api.pseudo_rdf.NodeView;
import org.aksw.jena_sparql_api.pseudo_rdf.PseudoNodeMapper;
import org.aksw.jena_sparql_api.pseudo_rdf.PseudoRdfProperty;
import org.aksw.jena_sparql_api.pseudo_rdf.PseudoRdfPropertyImpl;
import org.aksw.jena_sparql_api.pseudo_rdf.RdfType;
import org.aksw.jena_sparql_api.pseudo_rdf.RdfTypeRDFDatatype;
import org.aksw.jena_sparql_api.pseudo_rdf.RdfTypeSimple;
import org.aksw.jena_sparql_api.pseudo_rdf.RdfTypeUri;
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
import org.apache.jena.vocabulary.VCARD4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Converter;

import eu.trentorise.opendata.jackan.model.CkanDataset;
import eu.trentorise.opendata.jackan.model.CkanResource;
import eu.trentorise.opendata.jackan.model.CkanTag;

/**
 * Entry point for creating Jena {@link Node} views over CKAN domain objects.
 * These Node objects can then be wrapped as {@link Resource}s using Jena's polymophism system:
 * 
 * Usage example:
 * <code>
 * Model model = ModelFactory.createModelForGraph(new PseudoGraph());
 * RdfDcatApDataset dcatDataset = model.asRDFNode(CkanPseudoNodeFactory.get().createDataset()).as(RdfDcatApDataset.class);
 * // Note: RdfDcatApDataset implements Resource
 * </code>
 * 
 * 
 * @author Claus Stadler, May 17, 2018
 *
 */
public class CkanPseudoNodeFactory {	
	private static final Logger logger = LoggerFactory.getLogger(CkanPseudoNodeFactory.class);
	
	//public Map<String, Function<>> targetToAccessorSupplier;
	
	public static ModelMappingRegistry modelMappingRegistry;

	public static Map<String, Converter<?, ?>> viaMap = new HashMap<>();

	static {
		viaMap.put("ckanTag", Converter.from(CkanTag::new, CkanTag::getName));		
	}

	
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
		this(RDFDataMgr.loadModel("dcat-ap-ckan-mapping.ttl"));
	}

    public CkanPseudoNodeFactory(Model mappingModel) {
        initDefaults();
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
				s -> new PseudoRdfPropertyImpl<>(
							s.getPropertyAsSet(attr, attrClass),
							rdfType,
							nodeMapper));
	}


	public static void addStringMapping(Map<String, Function<PropertySource, PseudoRdfProperty>> registry, String p, String attr) {
		addSimpleMapping(registry, p, attr, String.class, new RdfTypeRDFDatatype<>(String.class), NodeMapperFactory.string);
	}
	

	public static <T> void addCollectionMapping(
			AccessorSupplierFactory<?> accessorSupplierFactory,
			Map<String, Function<PropertySource, PseudoRdfProperty>> registry, String p, String attr, TypeMapper typeMapper, String dtypeUri, Converter<?, ?> converter) {
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
		
		addCollectionMapping(registry, p, attr, clazz, rdfType, nodeMapper, (Converter)converter);
	
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
				s -> new PseudoRdfPropertyImpl<>(
							new CollectionAccessorFromCollectionValue<>(s.getCollectionProperty(attr, attrClass)),
							new RdfTypeRDFDatatype<>(attrClass),
							NodeMapperFactory.from(attrClass)));	
	}

	public static <T, V> void addCollectionMapping(
			Map<String, Function<PropertySource, PseudoRdfProperty>> registry, String p, String attr, Class<V> attrClass, RdfType<T> rdfType, NodeMapper<T> nodeMapper, Converter<T, V> converter) {
		
		Converter<T, V> c = converter == null
				? (Converter<T, V>)Converter.identity()
				: converter;
		
		registry.put(p,
				s -> new PseudoRdfPropertyImpl<>(
						new CollectionAccessorFromCollection<>(
							new CollectionFromConverter<>(
								new LazyCollection<>(
									s.getCollectionProperty(attr, attrClass),
									ArrayList::new, true),
								c)),
							rdfType,
							nodeMapper));	
	}


	public static <T> void addExtraJsonArrayMapping(
			Map<String, Function<PropertySource, PseudoRdfProperty>> registry, String p, String attr, Class<T> clazz, RdfType<T> rdfType, NodeMapper<T> nodeMapper) {
		
		registry.put(p,
				s -> new PseudoRdfPropertyImpl<>(
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
				s -> new PseudoRdfPropertyImpl<>(
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
			s -> new PseudoRdfPropertyImpl<>(
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
			String via = mapping.getProperty(MappingVocab.via).getString();
			
			Converter<?, ?> converter = viaMap.get(via);
			if(via != null && converter == null) {
				throw new RuntimeException("No converter registered for: " + via);
			} else {
				System.out.println("Converter found for" + via);
			}
			
			CkanPseudoNodeFactory.addCollectionMapping(accessorSupplierFactory, mappingRegistry, predicate, key, typeMapper, dtype.getURI(), converter);
			
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
		addCollectionMapping(ckanDatasetAccessors, DCAT.keyword.getURI(), "tags", String.class, new RdfTypeRDFDatatype<>(String.class), NodeMapperFactory.string, null);		
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
				s -> new PseudoRdfPropertyImpl<>(
						new CollectionAccessorSingleton<>((CkanDataset)s.getSource()),
//						new SingleValuedAccessorDirect<>(new CollectionFromSingleValuedAccessor<>(new SingleValuedAccessorDirect<>()),
						new RdfTypeSimple<>(CkanDataset::new),
						new PseudoNodeMapper<>(CkanDataset.class,
								ckanDataset ->  new PropertySourcePrefix("extra:publisher_",  new PropertySourceFromAccessorSupplier<>(ckanDataset, ckanDatasetAccessorFactory)),
								ckanDatasetPublisherAccessors)));


		ckanDatasetAccessors.put(DCAT.contactPoint.getURI(),
				s -> new PseudoRdfPropertyImpl<>(
						new CollectionAccessorSingleton<>((CkanDataset)s.getSource()),
						new RdfTypeSimple<>(CkanDataset::new),
						new PseudoNodeMapper<>(CkanDataset.class,
								ckanDataset -> new PropertySourceFromAccessorSupplier<>(ckanDataset, ckanDatasetAccessorFactory),
								ckanDatasetContactPointAccessors)));

		//new PropertySourcePrefix("extra:contact_",
		
		ckanResourceAccessors.put(Spdx.checksum.getURI(),
				s -> new PseudoRdfPropertyImpl<>(
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
	
	public NodeView createDataset() {
		CkanDataset ckanDataset = new CkanDataset();

		AccessorSupplierFactory<CkanDataset> accessorFactory = getAccessorFactory(CkanDataset.class);
		PropertySource propertySource = new PropertySourceFromAccessorSupplier<CkanDataset>(ckanDataset, accessorFactory);
		
		NodeView result = new NodeView(propertySource, ckanDatasetAccessors);
		return result;
	}
	
	public NodeView createDistribution() {
		CkanResource ckanResource = new CkanResource();

		AccessorSupplierFactory<CkanResource> accessorFactory = getAccessorFactory(CkanResource.class);
		PropertySource propertySource = new PropertySourceFromAccessorSupplier<CkanResource>(ckanResource, accessorFactory);
		
		NodeView result = new NodeView(propertySource, ckanResourceAccessors);
		return result;
	}
	
	
}