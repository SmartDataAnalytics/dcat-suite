package org.aksw.dcat.ap.playground.main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.aksw.dcat.ap.binding.ckan.domain.impl.PropertySource;
import org.aksw.dcat.ap.binding.ckan.domain.impl.PropertySourceCkanDataset;
import org.aksw.dcat.ap.binding.ckan.domain.impl.PropertySourceCkanResource;
import org.aksw.dcat.ap.binding.ckan.domain.impl.PropertySourcePrefix;
import org.aksw.dcat.ap.binding.jena.domain.impl.RdfDcatApDistribution;
import org.aksw.dcat.util.view.LazyCollection;
import org.aksw.dcat.util.view.SetFromConverter;
import org.aksw.dcat.util.view.SingleValuedAccessor;
import org.aksw.dcat.util.view.SingleValuedAccessorDirect;
import org.aksw.jena_sparql_api.utils.model.NodeMapper;
import org.aksw.jena_sparql_api.utils.model.NodeMapperFactory;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.enhanced.BuiltinPersonalities;
import org.apache.jena.graph.BlankNodeId;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.NodeVisitor;
import org.apache.jena.graph.Node_Concrete;
import org.apache.jena.graph.Triple;
import org.apache.jena.graph.impl.GraphBase;
import org.apache.jena.rdf.model.AnonId;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.RDFVisitor;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.sparql.vocabulary.FOAF;
import org.apache.jena.system.JenaSystem;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.util.iterator.NullIterator;
import org.apache.jena.util.iterator.WrappedIterator;
import org.apache.jena.vocabulary.DCAT;
import org.apache.jena.vocabulary.DCTerms;

import com.google.common.base.Converter;

import eu.trentorise.opendata.jackan.model.CkanDataset;
import eu.trentorise.opendata.jackan.model.CkanResource;

class CollectionTraits {
	boolean canRemove;
	boolean isSingleValued;
}

interface PseudoRdfProperty {
	/**
	 * A collection view of the property values
	 * Implementations should support removals
	 * If the underlying field is single valued, removal should unset the field
	 * 
	 * For additions, convert to {@link PseudoRdfSetObjectProperty} first
	 * 
	 * @return
	 */
	Collection<Node> getValues();
}


interface PseudoRdfSetObjectProperty {
	Set<Node> getValues();

	
	// TODO Probably we need to add a class parameter to support polymorphic elements
	// I.e. does this property support *creating and adding* a new instance of type X
	// Or maybe creation should be done elsewhere?
	// In jena there is the nodeFactory. however, in our case,
	// the underlying entity may not exist as a object, but only inside some attributes
	boolean canCreateNew();
	
	/**
	 * Creates a new instance that is
	 * appropriate to be added to the set of values
	 *  
	 * @return
	 */
	PseudoRdfResource createNew();
}

class PseudoRdfObjectPropertyImpl<T>
	implements PseudoRdfProperty
{
	// The accessor to retrieve the enity 
	//protected SingleValuedAccessor<T> accessor;
	protected SingleValuedAccessor<Collection<T>> accessor;
	
	//protected Function<T, PseudoRdfResource> backendToResource;
	protected NodeMapper<T> nodeMapper;
	//protected Function<T, Node> backendToNode;
	
	
	// The available schematic properties 
	//protected Map<String, Function<T, PseudoRdfProperty>> propertyToAccessor;
	
	
	public PseudoRdfObjectPropertyImpl(
			SingleValuedAccessor<Collection<T>> accessor,
			NodeMapper<T> nodeMapper) {
			//Function<T, Node> backendToNode) {
		super();
		this.accessor = accessor;
		//this.backendToNode = backendToNode;
		this.nodeMapper = nodeMapper;
	}
	
	@Override
	public Collection<Node> getValues() {
		//SetFromSingleValuedAccessor<T> backend = new SetFromSingleValuedAccessor<>(accessor);
		
		Converter<Node, T> converter = new Converter<Node, T>() {
			@Override
			protected T doForward(Node a) {
				T result = nodeMapper.toJava(a);
				return result;
			}

			@Override
			protected Node doBackward(T b) {
				Node result = nodeMapper.toNode(b);
				return result;
			}
		};
		
		
		Collection<T> backend = accessor.get();
		
		if(backend == null) {
			throw new RuntimeException("Got null value for " + accessor);
		}
		
		Collection<Node> result = new SetFromConverter<>(backend, converter);
		return result;
	}
}



//class PseudoRdfLiteralPropertyImpl<T>
//	implements PseudoRdfProperty
//{
//	protected SingleValuedAccessor<T> accessor;
//
//	public PseudoRdfLiteralPropertyImpl(SingleValuedAccessor<T> accessor) {
//		super();
//		Objects.requireNonNull(accessor);
//		this.accessor = accessor;
//	}
//
//	@Override
//	public Collection<Node> getValues() {
//		CollectionFromSingleValuedAccessor<T> backend = new CollectionFromSingleValuedAccessor<>(accessor);
//		
//		Converter<PseudoRdfNode, T> converter = new Converter<PseudoRdfNode, T>() {
//			@Override
//			protected T doForward(PseudoRdfNode a) {
//				return (T)a.getLiteralValue();
//			}
//
//			@Override
//			protected PseudoRdfNode doBackward(T b) {
//				return new PseudoRdfNodeImpl(accessor);
//			}
//		};
//		
//		
//		Collection<PseudoRdfNode> result = new SetFromConverter<>(backend, converter);
//		return result;
//	}
//	
//	
//}



/**
 * A property that can only have zero-or-one values
 * 
 * @author raven Apr 11, 2018
 *
 */
interface PseudoRdfObjectPropertyZeroOrOne {
	
}


/**
 * An unmodifiable property with exactly one value whose reference cannot be changed.
 * Useful when there is a object composition in the underlying objects,
 * e.g. the publisher of a CkanDataset cannot exist without the latter. 
 * 
 * 
 * @author raven Apr 11, 2018
 *
 */
interface PseudoRdfSingletonObjectProperty {
	
}


interface PseudoRdfNode
	extends RDFNode
{
	//Object getBackingEntity();

	boolean isResoure();
	PseudoRdfResource asResource();
	
	Object getLiteralValue();
}


class PseudoRdfNodeBase
	implements PseudoRdfNode
{

	@Override
	public Node asNode() {
		RDFDatatype dtype = TypeMapper.getInstance().getSafeTypeByName("http://pseudo.rdf/node");
		Node result = NodeFactory.createLiteralByValue(this, dtype);
		return result;
	}

	@Override
	public boolean isAnon() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isLiteral() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isURIResource() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isResource() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public <T extends RDFNode> T as(Class<T> view) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T extends RDFNode> boolean canAs(Class<T> view) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Model getModel() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RDFNode inModel(Model m) {
		return this;
	}

	@Override
	public Object visitWith(RDFVisitor rv) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public Literal asLiteral() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isResoure() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public PseudoRdfResource asResource() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getLiteralValue() {
		// TODO Auto-generated method stub
		return null;
	}	
}


abstract class PseudoRdfResourceBase
	extends PseudoRdfNodeBase
	implements PseudoRdfResource
{
	
	public abstract PseudoRdfProperty getProperty(String property);


	@Override
	public AnonId getId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Resource inModel(Model m) {
		return this;
	}

	@Override
	public boolean hasURI(String uri) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getURI() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getNameSpace() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getLocalName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Statement getRequiredProperty(Property p) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Statement getRequiredProperty(Property p, String lang) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public Statement getProperty(Property p, String lang) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public StmtIterator listProperties(Property p) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public StmtIterator listProperties(Property p, String lang) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public StmtIterator listProperties() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Resource addLiteral(Property p, boolean o) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Resource addLiteral(Property p, long o) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Resource addLiteral(Property p, char o) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Resource addLiteral(Property value, double d) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Resource addLiteral(Property value, float d) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Resource addLiteral(Property p, Object o) {
		PseudoRdfProperty pp = getProperty(p.getURI());
		//PseudoRdfLiteralProperty x = (PseudoRdfLiteralProperty)pp;
		
		Node node = null; //new PseudoRdfNodeImpl(new SingleValuedAccessorDirect<>(o));
		Collection<Node> values = pp.getValues();
		values.add(node);
		return this;
	}

	@Override
	public Resource addLiteral(Property p, Literal o) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Resource addProperty(Property p, String o) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Resource addProperty(Property p, String o, String l) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Resource addProperty(Property p, String lexicalForm, RDFDatatype datatype) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Resource addProperty(Property p, RDFNode o) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasProperty(Property p) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean hasLiteral(Property p, boolean o) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean hasLiteral(Property p, long o) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean hasLiteral(Property p, char o) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean hasLiteral(Property p, double o) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean hasLiteral(Property p, float o) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean hasLiteral(Property p, Object o) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean hasProperty(Property p, String o) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean hasProperty(Property p, String o, String l) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean hasProperty(Property p, RDFNode o) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Resource removeProperties() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Resource removeAll(Property p) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Resource begin() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Resource abort() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Resource commit() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Resource getPropertyResourceValue(Property p) {
		Statement stmt = getProperty(p);
		RDFNode o = stmt.getObject();
		Resource result = o.asResource();
		return result;
	}

	@Override
	public Object getBackingEntity() {
		// TODO Auto-generated method stub
		return null;
	}

}


//class PseudoRdfResourceImpl
//	extends PseudoRdfResourceBase
//{
//	
//	
//	//protected SingleValuedAccessor<T> accessor;
//	protected PropertySource propertySource;
//	
//	// The available schematic properties 
//	protected Map<String, Function<PropertySource, PseudoRdfProperty>> propertyToAccessor;
//	
//	public PseudoRdfResourceImpl(PropertySource propertySource,
//			Map<String, Function<PropertySource, PseudoRdfProperty>> propertyToAccessor) {
//		super();
//		this.propertySource = propertySource;
//		this.propertyToAccessor = propertyToAccessor;
//	}
//
//	public Object getBackingEntity() {
//		return propertySource;
//	}
//	
//	public PseudoRdfProperty getProperty(String property) {
//		Function<PropertySource, PseudoRdfProperty> schemaProperty = propertyToAccessor.get(property);
//		if(schemaProperty == null) {
//			throw new RuntimeException("Property " + property + " not mapped for access over " + propertySource);
//		}
//		
//		PseudoRdfProperty result = schemaProperty.apply(propertySource);
//		return result;
//	}
//	
//	
//	
//	/**
//	 * 
//	 * @param property
//	 * @return The set of values of the requested property. Null if the property does not exist in the schema.
//	 */
//	public Collection<Node> getPropertyValues(String property) {
//		PseudoRdfProperty p = getProperty(property);
//	
//		Collection<Node> result = p == null ? null : p.getValues();
//		
//		return result;
//	}
//
//	@Override
//	public StmtIterator listProperties(Property p) {
//		Set<String> pc = Collections.singleton(p.getURI());
//		StmtIterator result = listProperties(pc);
//		return result;
//	}
//	
//	@Override
//	public StmtIterator listProperties() {
//		Set<String> allKnownProperties = propertyToAccessor.keySet();
//		return listProperties(allKnownProperties);
//	}
//	
//
//	public StmtIterator listProperties(Set<String> names) {
//
//		ExtendedIterator<Statement> result = new NullIterator<>();
//		for(String str : names) {
//			Collection<? extend> values = getPropertyValues(str);
//			if(values != null) {
//				Iterator<? extends PseudoRdfNode> tmp = values.iterator();
//				ExtendedIterator<? extends PseudoRdfNode> it = WrappedIterator.create(tmp);
//				
//				//ExtendedIterator<? extends PseudoRdfNode> it = (ExtendedIterator<? extends PseudoRdfNode>)tmp;
//	
//				Property p = ResourceFactory.createProperty(str);
//	
//				result = result.andThen(it.mapWith(o -> new StatementImpl(this, p, o)));
//			}			
//			//((ExtendedIterator<PseudoRdfNode>>)getPropertyValues(str).iterator())
//		}
//		
//		StmtIterator r = new StmtIteratorImpl(result);
//		return r;
//	}
//	
//	@Override
//	public boolean isResoure() {
//		return true;
//	}
//	
//	@Override
//	public PseudoRdfResource asResource() {
//		return this;
//	}
//	
//	@Override
//	public boolean isAnon() {
//		return true;
//	}
//
//	@Override
//	public Statement getProperty(Property p) {
//		StmtIterator tmp = listProperties(p);
//		Statement result = tmp.nextOptional().orElse(null);
//		return result;
//	}
//
//}


interface PseudoRdfResource
	extends Resource, PseudoRdfNode
{
	Object getBackingEntity();
	
}

class PseudoRdfNodeImpl
	extends PseudoRdfNodeBase
{
	protected SingleValuedAccessor<?> accessor;
	
	public PseudoRdfNodeImpl(SingleValuedAccessor<?> accessor) {
		super();
		this.accessor = accessor;
	}

	@Override
	public PseudoRdfResource asResource() {
		throw new RuntimeException("Not a resource");
	}

	@Override
	public String toString() {
		return "PseudoRdfNodeImpl [accessor=" + accessor + "]";
	}

	@Override
	public Object getLiteralValue() {
		return accessor.get();
	}

}


/**
 * Expose information about the datatype of the property.
 * 
 * 
 * @author raven Apr 11, 2018
 *
 */
class PropertyDescriptor {
	
}

interface Schema {
	List<Property> listProperties();
}

class SchemaResource {
	Schema getSchema() {
		return null;
	}

	public void addProperty(String property, PseudoRdfNode value) {
		// Get a descriptor from the schema
	}

	public PseudoRdfNode getProperty(String property) {
		return null;
	}
	
}


class CkanDatasetSchema {
	
	public void initMappings() {
		/*
		 * return a factory for a single valued property
		 * that accesses the title field
		 * 
		 * 
		 */
		
		
		//propertyToAccessor.put(DCAT.distribution.getURI(), null);
	}
	
}

class DistributionAccessor {
	
}

/**
 * Would it make sense to wrap objects as Node - rather than RDFNode?
 * A node can participate in a model after all.
 * And we can have a Resource view over it, from which we could view it as a PseudoRdfResource
 * 
 * But this has very confusing behavior:
 * - Resource r = Model.createResource(wrappenNode(entity));
 * 
 * r.listStatements() -> returns all statements in the model
 * r.as(PseudoRdfResource.class).listStatements() -> returns all statements derived from the entity
 * 
 * 
 * 
 * @author raven Apr 13, 2018
 *
 */
interface PseudoNodeFactory {
	Node createResource(Object entity, Map<String, Function<Object, PseudoRdfProperty>> entityToTriple);
}

class CkanPseudoNodeFactory {

	protected Map<String, Function<PropertySource, PseudoRdfProperty>> ckanDatasetAccessors = new HashMap<>();
	protected Map<String, Function<PropertySource, PseudoRdfProperty>> ckanResourceAccessors = new HashMap<>();

	protected Map<String, Function<PropertySource, PseudoRdfProperty>> ckanDatasetPublisherAccessors = new HashMap<>();
	
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
	
	public void initDefaults() {

		/*
		 * datasaset mappings
		 */
		
		ckanDatasetAccessors.put(DCTerms.title.getURI(),
				s -> new PseudoRdfObjectPropertyImpl<>(
							new SingleValuedAccessorDirect<>(
									new CollectionFromSingleValuedAccessor<>(
											s.getProperty("title", String.class))),
							NodeMapperFactory.string));
		
		


		/**
		 * requesting properties by the value 
		 * 
		 */
		ckanDatasetAccessors.put(DCAT.distribution.getURI(),
				s -> new PseudoRdfObjectPropertyImpl<>(
						new SingleValuedAccessorDirect<>(new LazyCollection<>(
								s.getCollectionProperty("resources", CkanResource.class),
								ArrayList::new, true)),
						new PseudoNodeMapper<CkanResource>(CkanResource.class,
								ckanResource ->  new PropertySourceCkanResource(ckanResource),
								ckanResourceAccessors)));
		/*
		 * dataset -> publisher
		 */

//		PropertySource x;
//		new SingleValuedAccessorFromSet<>(new SetFromSingleValuedAccessor<>(x.getProperty("publisher", CkanDataset.class)));

		
		// TODO Finish
		
		// the publisher property is *not* based on any value
		// possible ways to deal with insertion of that property
		// (a) - the first addition of x publisher y marks y an alias for the publisher value
		//     -the next addition will raise an exception because the publisher already exists
		// (b) - these kind of properties can neither be 'added' nor 'set', but only 'get'
		//       - its like a singleton instance; one can not change or set its reference
		//         - a .isSingleton() method on the property could indicate this.
		//
		// Actually, the whole addition model works differently for pseudo object properties:
		// - if we wanted to add a statement to a an object property, there are these possibilities:
		//   - the property is a singleton, then we have to 'get' the node referenced by the property
		//   - the property is collection based, then we get the collection, and allocate new records via the 'newItem' method
        //     - There are two types of object properties:
		//       - One where creating a new item already makes it part of the collection
		//         - similar to appending an item to an array of structs
		//       - These where the created item has yet to be placed into the collection
		//   - adding a value to a property that can only hold a single value is equivalent to
		//     adding an item to a set with max cardinality 1
		//
		// Collection of collections
		// - The RDF model itself does not not directly have a notion a collections -
		// there are only triples; in our case, (subject predicate collection-of-objects)
		// - RDF specifies how to model collections/containers
		// - The jena abstraction can be used to access properly modeled resources as e.g. RDFList
		// - The items of a collection are nodes; and may represent nested collections and may
		// thus be be viewed as such.
		//
		// Ordering

		//PropertySource x;
		//Function<PropertySource, ? extends SingleValuedAccessor<Collection<PropertySource>>> getSelf = 
				//SingleValuedAccessor<? extends Collection<PropertySource>> foo =
		
		// SingleValuedAccessor<Collection<PropertySource>>
		//SingleValuedAccessor<Collection<PropertySource>> foo = new SingleValuedAccessorDirect<>(new CollectionFromSingleValuedAccessor<>(new SingleValuedAccessorDirect<>(x)));

//		Function<? super PropertySource, ? extends PseudoRdfSetObjectProperty> itemToProperty = new PseudoRdfResourceImpl(
//				new PropertySourcePrefix("publisher_", ckanDataset, ckanDatasetPublisherAccessors));
		
		
		NodeMapper<CkanDataset> nm;
		
		// Given the property source s, return an accessor to itself
		// For each source s (itself), wrap it as a new PseudoRdfResource
		ckanDatasetAccessors.put(DCTerms.publisher.getURI(),
				s -> new PseudoRdfObjectPropertyImpl<>(
						new SingleValuedAccessorDirect<>(new CollectionFromSingleValuedAccessor<>(new SingleValuedAccessorDirect<>((CkanDataset)s.getSource()))),
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
		
		ckanDatasetPublisherAccessors.put(FOAF.name.getURI(),
				s -> new PseudoRdfObjectPropertyImpl<>(
							new SingleValuedAccessorDirect<>(
									new CollectionFromSingleValuedAccessor<>(
											s.getProperty("name", String.class))),
							NodeMapperFactory.string));
	
		
		/*
		 * distribution mappings 
		 */
		
//		ckanResourceAccessors.put(DCTerms.description.getURI(),
//				s -> Optional.ofNullable(s.getProperty("description", String.class)).map(PseudoRdfLiteralPropertyImpl::new).orElse(null));
		ckanResourceAccessors.put(DCTerms.description.getURI(),
				s -> new PseudoRdfObjectPropertyImpl<>(
							new SingleValuedAccessorDirect<>(
									new CollectionFromSingleValuedAccessor<>(
											s.getProperty("description", String.class))),
							NodeMapperFactory.string));
	}
	
	Node createDataset() {
		CkanDataset ckanDataset = new CkanDataset();
		PseudoNode result = new PseudoNode(new PropertySourceCkanDataset(ckanDataset), ckanDatasetAccessors);
		return result;
	}
	
	Node createDistribution() {
		CkanResource ckanResource = new CkanResource();
		PseudoNode result = new PseudoNode(new PropertySourceCkanResource(ckanResource), ckanResourceAccessors);
		return result;
	}
}

class PseudoNode
	extends Node_Concrete
{
	//protected PropertySource entity;
	protected Map<String, Function<PropertySource, PseudoRdfProperty>> propertyToAccessor;

	protected PropertySource source;
	
	// The property source acts as the label
	public PseudoNode(PropertySource source, Map<String, Function<PropertySource, PseudoRdfProperty>> propertyToAccessor) {
		super(source);
		this.source = source;
		this.propertyToAccessor = propertyToAccessor;
	}
	
    public Map<String, Function<PropertySource, PseudoRdfProperty>> getPropertyToAccessor() {
		return propertyToAccessor;
	}

	public PropertySource getSource() {
		return source;
	}




	@Override
    public boolean isBlank() { return true; }

    @Override
    public BlankNodeId getBlankNodeId()  { return (BlankNodeId) label; }
    
    @Override
    public Object visitWith(NodeVisitor v) {
    	throw new RuntimeException("Visiting not supported");
    }
    
    @Override
    public boolean equals( Object other )
    {
    	return false;
    }
    
    
    
	public PseudoRdfProperty getProperty(String property) {
		Function<PropertySource, PseudoRdfProperty> schemaProperty = propertyToAccessor.get(property);
		if(schemaProperty == null) {
			throw new RuntimeException("Property " + property + " not mapped for access over " + source);
		}
		
		PseudoRdfProperty result = schemaProperty.apply(source);
		return result;
	}

	public ExtendedIterator<Triple> listProperties(String p) {
		Set<String> pc = Collections.singleton(p);
		ExtendedIterator<Triple> result = listProperties(pc);
		return result;
	}
	
	public Collection<Node> getPropertyValues(String property) {
		PseudoRdfProperty p = getProperty(property);
	
		Collection<Node> result;
		try {
			result = p == null ? null : p.getValues();
		} catch(Exception e) {
			throw new RuntimeException("Error accessing property " + property, e);
		}
		return result;
	}

	
	public ExtendedIterator<Triple> listProperties() {
		Set<String> allKnownProperties = propertyToAccessor.keySet();
		return listProperties(allKnownProperties);
	}
    
	public ExtendedIterator<Triple> listProperties(Set<String> names) {

		ExtendedIterator<Triple> result = new NullIterator<>();
		for(String str : names) {
			Collection<Node> values = getPropertyValues(str);
			if(values != null) {
				Iterator<Node> tmp = values.iterator();
				ExtendedIterator<Node> it = WrappedIterator.create(tmp);
				
				//ExtendedIterator<? extends PseudoRdfNode> it = (ExtendedIterator<? extends PseudoRdfNode>)tmp;
	
				//Property p = ResourceFactory.createProperty(str);
				Node p = NodeFactory.createURI(str);
	
				result = result.andThen(it.mapWith(o -> new Triple(this, p, o)));//new StatementImpl(this, p, o)));
			}			
			//((ExtendedIterator<PseudoRdfNode>>)getPropertyValues(str).iterator())
		}
		
		return result;
	}

    
//        if ( this == other ) return true ;
//        return other instanceof Node_Blank && label.equals( ((Node_Blank) other).label ); }
}
	

class PseudoGraph
	extends GraphBase
{

	public PseudoNode getSubjectAsNode(Triple tp) {
		Node node = tp.getSubject();
		
		if(!(node instanceof PseudoNode)) {
			throw new RuntimeException("Only nodes of type " + PseudoNode.class.getName() + " supported, got: " + node);
		}
		
		PseudoNode s = (PseudoNode)node;

		return s;
	}

	@Override
	public void performAdd(Triple t) {
		PseudoNode s = getSubjectAsNode(t);
		
		Collection<Node> values = s.getPropertyValues(t.getPredicate().getURI());
		values.add(t.getObject());
	}
	
	@Override
	public void performDelete(Triple t) {
		PseudoNode s = getSubjectAsNode(t);
		
		Collection<Node> values = s.getPropertyValues(t.getPredicate().getURI());
		values.remove(t.getObject());
	}

	@Override
	protected ExtendedIterator<Triple> graphBaseFind(Triple tp) {
		PseudoNode s = getSubjectAsNode(tp);

		Node p = tp.getMatchPredicate();
		
		ExtendedIterator<Triple> result = p == null
				? s.listProperties()
				: s.listProperties(p.getURI());
		
		// TODO filter by object
				
		return result;
	}

	
}

class PseudoNodeMapper<T>
	implements NodeMapper<T>
{
	protected Class<T> clazz;
	protected Function<T, PropertySource> wrapper;
	protected Map<String, Function<PropertySource, PseudoRdfProperty>> propertyToAccessor;

	

	public PseudoNodeMapper(Class<T> clazz, Function<T, PropertySource> wrapper,
			Map<String, Function<PropertySource, PseudoRdfProperty>> propertyToAccessor) {
		super();
		this.clazz = clazz;
		this.wrapper = wrapper;
		this.propertyToAccessor = propertyToAccessor;
	}

	@Override
	public Class<?> getJavaClass() {
		return clazz;
	}

	@Override
	public boolean canMap(Node node) {
		boolean result = node instanceof PseudoNode; // and the wrapped entity must be an instance of clazz && ((PseudoNode)node);
		return result;
	}

	@Override
	public Node toNode(T obj) {
		PropertySource ps = wrapper.apply(obj);
		Node result = new PseudoNode(ps, propertyToAccessor);
		return result;
	}

	@Override
	public T toJava(Node node) {
		PseudoNode pseudoNode = (PseudoNode)node;
		T result = (T)pseudoNode.getSource().getSource();
		return result;
	}
	
}

public class PseudoRdfConcept {
	public static void main(String[] args) {

		JenaSystem.init();
		RdfDcatApPersonalities.init(BuiltinPersonalities.model);
		/*
		 * playground
		 */
		
		Graph g = new PseudoGraph();
		Model m = ModelFactory.createModelForGraph(g);
		
		

		CkanDataset ckanDataset = new CkanDataset();
		CkanResource ckanResource = new CkanResource();
		ckanResource.setDescription("test description");
		
		ckanDataset.setResources(new ArrayList<>(Arrays.asList(ckanResource)));
		
		ckanDataset.setTitle("test");
				
		
		// Abstract the bean as something that has properties - so it could also be plain json
		//PropertySource s = new PropertySourceCkanDataset(ckanDataset);

		
		Resource dataset = m.asRDFNode(CkanPseudoNodeFactory.get().createDataset()).asResource();
		
		
		Resource distribution = m.asRDFNode(CkanPseudoNodeFactory.get().createDistribution()).asResource();
		
		dataset.addProperty(DCAT.distribution, distribution);
		//distribution.addProperty(DCTerms.description, "Test distri");
		
		RdfDcatApDistribution view = distribution.as(RdfDcatApDistribution.class);
		view.setDescription("Yay");
		
		
//		PseudoRdfResourceImpl dataset = new PseudoRdfResourceImpl(
//				new PropertySourceCkanDataset(ckanDataset), ckanDatasetAccessors);

		
		Resource publisher = dataset.getProperty(DCTerms.publisher)
			.getObject().asResource();//.changeObject("Test");

		publisher.addProperty(FOAF.name, "Test");
//		dataset.getProperty(DCTerms.publisher)
//			.changlit
		//.addLiteral(FOAF.name, "Test");

		
		dataset.listProperties(DCTerms.publisher).forEachRemaining(stmt -> {
			System.out.println("Publisher: " + stmt);
			

			stmt.getObject().asResource().listProperties().forEachRemaining(stmt2 -> {
				System.out.println("  Attr: " + stmt2);
			});
		});
		
//		Collection<? extends PseudoRdfNode> distributions = dataset.getPropertyValues(DCAT.distribution.getURI());
//		
//		System.out.println("Distributions: " + distributions);
		
		
//		RdfNode newDist = new PseudoRdfResourceImpl(
//				new PropertySourceCkanDataset(ckanDataset), ckanDatasetAccessors);
//		
//		dataset.addProperty(DCAT.distribution, newDist);
		
		dataset.listProperties(DCAT.distribution).forEachRemaining(stmt -> {
			System.out.println("Distribution: " + stmt);
			
			Resource dist = stmt.getObject().asResource();
			
			Statement distDescription = dist.getProperty(DCTerms.description);
			System.out.println("  Description: " + distDescription);
		});
		
		
		
//		dataset.getProperty(DCA)
		
		/*
		SingleValuedAccessor<Collection<CkanResource>> test = s.getCollectionProperty("resources", CkanResource.class);
		test.get().iterator().next().setDescription("Test description");
		
		
		System.out.println("Collection test: " + test.get());
		
		PseudoRdfProperty node = datasetAccessor.get(DCTerms.title.getURI()).apply(s);
		
		System.out.println(node.getValues());
		
		node.getValues().clear();
		System.out.println(node.getValues());
		
		System.out.println("title: " + ckanDataset.getTitle());
		*/
	}

}
