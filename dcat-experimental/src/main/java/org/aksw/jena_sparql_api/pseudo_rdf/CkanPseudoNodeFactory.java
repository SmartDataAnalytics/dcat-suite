package org.aksw.jena_sparql_api.pseudo_rdf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.aksw.dcat.ap.binding.ckan.domain.impl.PropertySource;
import org.aksw.dcat.ap.binding.ckan.domain.impl.PropertySourceCkanDataset;
import org.aksw.dcat.ap.binding.ckan.domain.impl.PropertySourceCkanResource;
import org.aksw.dcat.ap.binding.ckan.domain.impl.PropertySourcePrefix;
import org.aksw.dcat.ap.playground.main.SetFromJsonListString;
import org.aksw.dcat.util.view.CastConverter;
import org.aksw.dcat.util.view.CollectionAccessorFromCollection;
import org.aksw.dcat.util.view.CollectionAccessorFromCollectionValue;
import org.aksw.dcat.util.view.CollectionAccessorSingleton;
import org.aksw.dcat.util.view.CollectionFromConverter;
import org.aksw.dcat.util.view.LazyCollection;
import org.aksw.jena_sparql_api.utils.model.NodeMapper;
import org.aksw.jena_sparql_api.utils.model.NodeMapperFactory;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sparql.vocabulary.FOAF;
import org.apache.jena.vocabulary.DCAT;
import org.apache.jena.vocabulary.DCTerms;

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

public class CkanPseudoNodeFactory {

	protected Map<String, Function<PropertySource, PseudoRdfProperty>> ckanDatasetAccessors = new HashMap<>();
	protected Map<String, Function<PropertySource, PseudoRdfProperty>> ckanResourceAccessors = new HashMap<>();

	protected Map<String, Function<PropertySource, PseudoRdfProperty>> ckanDatasetPublisherAccessors = new HashMap<>();
	
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
	public static <T> void addSimpleMapping(Map<String, Function<PropertySource, PseudoRdfProperty>> registry, String p, String attr, Class<T> attrClass, NodeMapper<T> nodeMapper) {
		registry.put(p,
				s -> new PseudoRdfObjectPropertyImpl<>(
							s.getPropertyAsSet(attr, attrClass),
							new RdfTypeRDFDatatype<>(attrClass),
							nodeMapper));
	}


	public static void addStringMapping(Map<String, Function<PropertySource, PseudoRdfProperty>> registry, String p, String attr) {
		addSimpleMapping(registry, p, attr, String.class, NodeMapperFactory.string);
	}
	

	public static <T> void addCollectionMapping(Map<String, Function<PropertySource, PseudoRdfProperty>> registry, String p, String attr, TypeMapper typeMapper, String dtypeUri) {
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
		addCollectionMapping(registry, p, attr, clazz, rdfType, nodeMapper);
	
	}
	
	public static <T> void addExtraJsonArrayMapping(Map<String, Function<PropertySource, PseudoRdfProperty>> registry, String p, String attr, TypeMapper typeMapper, String dtypeUri) {
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
		addExtraJsonArrayMapping(registry, p, attr, clazz, rdfType, nodeMapper);
	}

	public static <T> void addLiteralMapping(Map<String, Function<PropertySource, PseudoRdfProperty>> registry, String p, String attr, TypeMapper typeMapper, String dtypeUri) {
		if(dtypeUri.equals(MappingVocab.r2rmlIRI.getURI())) {
			addUriStringMapping(registry, dtypeUri, attr);
		} else {
			RDFDatatype rdfDatatype = typeMapper.getTypeByName(dtypeUri);
			if(rdfDatatype == null) {
				throw new RuntimeException("Provided TypeMapper did not contain a RDFDatatype for " + dtypeUri);
			}
			Class<T> clazz = (Class<T>) rdfDatatype.getJavaClass();
			NodeMapper<T> nodeMapper = NodeMapperFactory.from(clazz);
			
			addSimpleMapping(registry, p, attr, clazz, nodeMapper);
		}
	}

	public static void addUriStringMapping(Map<String, Function<PropertySource, PseudoRdfProperty>> registry, String p, String attr) {
		addSimpleMapping(registry, p, attr, String.class, NodeMapperFactory.uriString);
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

	public static <T> void addCollectionMapping(Map<String, Function<PropertySource, PseudoRdfProperty>> registry, String p, String attr, Class<T> attrClass, RdfType<T> rdfType, NodeMapper<T> nodeMapper) {
		registry.put(p,
				s -> new PseudoRdfObjectPropertyImpl<>(
						new CollectionAccessorFromCollection<>(
								new LazyCollection<>(
								s.getCollectionProperty(attr, attrClass),
								ArrayList::new, true)),
							rdfType,
							nodeMapper));	
	}


	public static <T> void addExtraJsonArrayMapping(Map<String, Function<PropertySource, PseudoRdfProperty>> registry, String p, String attr, Class<T> clazz, RdfType<T> rdfType, NodeMapper<T> nodeMapper) {
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
	
	public void initDefaults() {

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
		addRelation(ckanDatasetAccessors, DCAT.distribution, "resources", CkanResource.class, PropertySourceCkanResource::new, ckanResourceAccessors);
		
		/* dataset -> publisher */
		// Given the property source s, return an accessor to itself
		// For each source s (itself), wrap it as a new PseudoRdfResource
		ckanDatasetAccessors.put(DCTerms.publisher.getURI(),
				s -> new PseudoRdfObjectPropertyImpl<>(
						new CollectionAccessorSingleton<>((CkanDataset)s.getSource()),
//						new SingleValuedAccessorDirect<>(new CollectionFromSingleValuedAccessor<>(new SingleValuedAccessorDirect<>()),
						new RdfTypeSimple<>(CkanDataset::new),
						new PseudoNodeMapper<>(CkanDataset.class,
								ckanDataset ->  new PropertySourcePrefix("extra:publisher_", new PropertySourceCkanDataset(ckanDataset)),
								ckanDatasetPublisherAccessors)));

		
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
	
	public PseudoNode createDataset() {
		CkanDataset ckanDataset = new CkanDataset();
		PseudoNode result = new PseudoNode(new PropertySourceCkanDataset(ckanDataset), ckanDatasetAccessors);
		return result;
	}
	
	public PseudoNode createDistribution() {
		CkanResource ckanResource = new CkanResource();
		PseudoNode result = new PseudoNode(new PropertySourceCkanResource(ckanResource), ckanResourceAccessors);
		return result;
	}
	
	
}