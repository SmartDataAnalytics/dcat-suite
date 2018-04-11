package org.aksw.dcat.ap.playground.main;

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import org.aksw.commons.collections.SinglePrefetchIterator;
import org.aksw.dcat.ap.binding.ckan.domain.impl.PropertySource;
import org.aksw.dcat.ap.binding.ckan.domain.impl.PropertySourceCkanDataset;
import org.aksw.dcat.ap.binding.ckan.domain.impl.PropertySourceCkanResource;
import org.aksw.dcat.util.view.SetFromConverter;
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
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.rdf.model.impl.StatementImpl;
import org.apache.jena.rdf.model.impl.StmtIteratorImpl;
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
	Collection<? extends PseudoRdfNode> getValues();
}


interface PseudoRdfSetObjectProperty {
	Set<PseudoRdfResource> getValues();

	
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

class SetFromSingleValuedAccessor<T>
	extends AbstractSet<T>
{
	protected SingleValuedAccessor<T> accessor;

	public SetFromSingleValuedAccessor(SingleValuedAccessor<T> accessor) {
		super();
		this.accessor = accessor;
	}

	@Override
	public boolean add(T e) {
		Objects.requireNonNull(e);

		T value = accessor.get();
		// Added value must be reference equal
		if(value != null && value != e) {
			throw new RuntimeException("Cannot add because a value already exists: " + value);
		}
		accessor.set(e);
		return true;
	}
	
	@Override
	public Iterator<T> iterator() {
		return new SinglePrefetchIterator<T>() {
			T value = accessor.get();
			int emitted = 0;
			
			@Override
			protected T prefetch() throws Exception {
				
				return emitted++ != 0 || value == null ? finish() : value;
			}
			
			@Override
			protected void doRemove() {
				accessor.set(null);
			}
		};		
	}

	@Override
	public int size() {
		T value = accessor.get();
		int result = value == null ? 0 : 1;
		return result;
	}
}

class PseudoRdfObjectPropertyImpl<T>
	implements PseudoRdfProperty
{
	// The accessor to retrieve the enity 
	//protected SingleValuedAccessor<T> accessor;
	protected SingleValuedAccessor<Collection<T>> accessor;
	
	protected Function<T, PseudoRdfResource> backendToResource;
	// The available schematic properties 
	//protected Map<String, Function<T, PseudoRdfProperty>> propertyToAccessor;
	
	
	public PseudoRdfObjectPropertyImpl(
			SingleValuedAccessor<Collection<T>> accessor,
			Function<T, PseudoRdfResource> backendToResource) {
		super();
		this.accessor = accessor;
		this.backendToResource = backendToResource;
	}
	
	@Override
	public Collection<PseudoRdfResource> getValues() {
		//SetFromSingleValuedAccessor<T> backend = new SetFromSingleValuedAccessor<>(accessor);
		
		Converter<PseudoRdfResource, T> converter = new Converter<PseudoRdfResource, T>() {
			@Override
			protected T doForward(PseudoRdfResource a) {
				//return (T)a.getLiteralValue();
				return (T)a.getBackingEntity();
			}

			@Override
			protected PseudoRdfResource doBackward(T b) {
				//return new PseudoRdfResourceImpl(accessor, propertyToAccessor);
				return backendToResource.apply(b);
//				return null;
			}
		};
		
		
		Collection<T> backend = accessor.get();
		
		Collection<PseudoRdfResource> result = new SetFromConverter<>(backend, converter);
		return result;
	}
}



class PseudoRdfLiteralPropertyImpl<T>
	implements PseudoRdfProperty
{
	protected SingleValuedAccessor<T> accessor;

	public PseudoRdfLiteralPropertyImpl(SingleValuedAccessor<T> accessor) {
		super();
		Objects.requireNonNull(accessor);
		this.accessor = accessor;
	}

	@Override
	public Collection<? extends PseudoRdfNode> getValues() {
		SetFromSingleValuedAccessor<T> backend = new SetFromSingleValuedAccessor<>(accessor);
		
		Converter<PseudoRdfNode, T> converter = new Converter<PseudoRdfNode, T>() {
			@Override
			protected T doForward(PseudoRdfNode a) {
				return (T)a.getLiteralValue();
			}

			@Override
			protected PseudoRdfNode doBackward(T b) {
				return new PseudoRdfNodeImpl(accessor);
			}
		};
		
		
		Collection<PseudoRdfNode> result = new SetFromConverter<>(backend, converter);
		return result;
	}
	
	
}



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
	public Statement getProperty(Property p) {
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
		// TODO Auto-generated method stub
		return null;
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getBackingEntity() {
		// TODO Auto-generated method stub
		return null;
	}

}


class PseudoRdfResourceImpl
	extends PseudoRdfResourceBase
{
	
	
	//protected SingleValuedAccessor<T> accessor;
	protected PropertySource propertySource;
	
	// The available schematic properties 
	protected Map<String, Function<PropertySource, PseudoRdfProperty>> propertyToAccessor;
	
	public PseudoRdfResourceImpl(PropertySource propertySource,
			Map<String, Function<PropertySource, PseudoRdfProperty>> propertyToAccessor) {
		super();
		this.propertySource = propertySource;
		this.propertyToAccessor = propertyToAccessor;
	}

	public Object getBackingEntity() {
		return propertySource;
	}
	
	public PseudoRdfProperty getProperty(String property) {
		Function<PropertySource, PseudoRdfProperty> schemaProperty = propertyToAccessor.get(property);
		if(schemaProperty == null) {
			throw new RuntimeException("Property " + property + " not found on " + propertySource);
		}
		
		PseudoRdfProperty result = schemaProperty.apply(propertySource);
		return result;
	}
	
	
	
	/**
	 * 
	 * @param property
	 * @return The set of values of the requested property. Null if the property does not exist in the schema.
	 */
	public Collection<? extends PseudoRdfNode> getPropertyValues(String property) {
		PseudoRdfProperty p = getProperty(property);
	
		Collection<? extends PseudoRdfNode> result = p == null ? null : p.getValues();
		
		return result;
	}

	@Override
	public StmtIterator listProperties() {
		
		ExtendedIterator<Statement> result = new NullIterator<>();
		for(String str : propertyToAccessor.keySet()) {
			Collection<? extends PseudoRdfNode> values = getPropertyValues(str);
			if(values != null) {
				Iterator<? extends PseudoRdfNode> tmp = values.iterator();
				ExtendedIterator<? extends PseudoRdfNode> it = WrappedIterator.create(tmp);
				
				//ExtendedIterator<? extends PseudoRdfNode> it = (ExtendedIterator<? extends PseudoRdfNode>)tmp;
	
				Property p = ResourceFactory.createProperty(str);
	
				result = result.andThen(it.mapWith(o -> new StatementImpl(this, p, o)));
			}			
			//((ExtendedIterator<PseudoRdfNode>>)getPropertyValues(str).iterator())
		}
		
		StmtIterator r = new StmtIteratorImpl(result);
		return r;
	}
}


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
	public boolean isResoure() {
		return false;
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

public class PseudoRdfConcept {
	public static void main(String[] args) {

		Map<String, Function<PropertySource, PseudoRdfProperty>> ckanDatasetAccessors = new HashMap<>();
		Map<String, Function<PropertySource, PseudoRdfProperty>> ckanResourceAccessors = new HashMap<>();

		/*
		 * datasaset mappings
		 */
		
		ckanDatasetAccessors.put(DCTerms.title.getURI(),
				s -> new PseudoRdfLiteralPropertyImpl(s.getProperty("title", String.class)));
		
		/**
		 * requesting properties by the value 
		 * 
		 */
		ckanDatasetAccessors.put(DCAT.distribution.getURI(),
				s -> new PseudoRdfObjectPropertyImpl<>(
						s.getCollectionProperty("resources", CkanResource.class),
						// Map the backing ckanResource to a resource view
						ckanResource -> new PseudoRdfResourceImpl(
								new PropertySourceCkanResource(ckanResource), ckanResourceAccessors)
						));

		
		/*
		 * distribution mappings 
		 */
		
		ckanDatasetAccessors.put(DCTerms.description.getURI(),
				s -> Optional.ofNullable(s.getProperty("description", String.class)).map(PseudoRdfLiteralPropertyImpl::new).orElse(null));


		/*
		 * playground
		 */

		CkanDataset ckanDataset = new CkanDataset();
		CkanResource ckanResource = new CkanResource();
		
		ckanDataset.setResources(new ArrayList<>(Arrays.asList(ckanResource)));
		
		ckanDataset.setTitle("test");
				
		
		// Abstract the bean as something that has properties - so it could also be plain json
		//PropertySource s = new PropertySourceCkanDataset(ckanDataset);

		
		
		PseudoRdfResourceImpl dataset = new PseudoRdfResourceImpl(
				new PropertySourceCkanDataset(ckanDataset), ckanDatasetAccessors);

		
		Collection<? extends PseudoRdfNode> distributions = dataset.getPropertyValues(DCAT.distribution.getURI());
		
		System.out.println("Distributions: " + distributions);
		
		
		dataset.listProperties().forEachRemaining(stmt -> {
			System.out.println("Statement: " + stmt);
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
