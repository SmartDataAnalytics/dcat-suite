package org.aksw.jena_sparql_api.pseudo_rdf;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.aksw.dcat.util.view.SingleValuedAccessor;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.AnonId;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.RDFVisitor;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

import com.google.common.collect.Range;

class CollectionTraits {
	boolean canRemove;
	boolean isSingleValued;
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

class NodeSchemaBuilder {
	String buildProperty(String uri) {
		return null;
	}
}


class PropertySchemaBuilder {
	Integer minCardinality;
	Integer maxCardinality;
	
//	setMinCardinality()
//	setMaxCardinality()
}


enum PropertyType {
	DEFAULT(Range.closed(0l, 1l)),
	
	/**
	 * Characteristic of properties that 'simply exists' 
	 * Such properties cannot be added nor removed - only retrieved.
	 */
	SINGLETON(Range.closed(1l, 1l)),
	
	/**
	 * Characteristic of properties with multiple values.
	 * They can be added and removed and retrieved without constraints
	 * 
	 */
	COLLECTION(Range.atLeast(0l));
	
	/**
	 * 
	 */

	protected Range<Long> range;

	PropertyType(Range<Long> range) {
		this.range = range;
	}
	
	
}



class ResourceCopy {
	public void copy(Resource src, Resource target) {
		StmtIterator it = src.listProperties();
		while(it.hasNext()) {
			Statement stmt = it.next();
			System.out.println("Copy: " + stmt);
		}
	}
}
