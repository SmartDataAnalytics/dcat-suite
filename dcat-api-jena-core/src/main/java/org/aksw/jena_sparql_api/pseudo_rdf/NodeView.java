package org.aksw.jena_sparql_api.pseudo_rdf;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

import org.aksw.commons.accessors.PropertySource;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.NodeVisitor;
import org.apache.jena.graph.Node_Ext;
import org.apache.jena.graph.Triple;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.util.iterator.NullIterator;
import org.apache.jena.util.iterator.WrappedIterator;

/**
 * A node implementation which conceptually allows enumerating a collection of
 * triples of which this node acts as a subject.
 *
 * This enumeration is based on a map whose keys correspond to predicates,
 * and the values to functions that, for a given {@link PropertySource}, yield a
 * {@link PseudoRdfProperty}. The latter combines obtaining a reference to
 * (a collection of) property values from the PropertySource with exposing it as a collection of
 * {@link Node} instances.
 *
 * @author Claus Stadler, May 16, 2018
 *
 */
public class NodeView
    extends Node_Ext<Node>
{
    //protected PropertySource entity;
    //protected Map<String, Function<PropertySource, PseudoRdfProperty>> propertyToAccessor;

    protected Map<String, Function<PropertySource, PseudoRdfProperty>> predicateToAccessor;
    protected PropertySource source;


    //protected BlankNodeId blankNodeId;
    protected Node blankNodeId;
    //protected PropertySource source;
    //protected Function<? super PropertySource, ? extends Object> getIdentifier;


    // The property source acts as the label
    public NodeView(
            PropertySource source,
            Map<String, Function<PropertySource, PseudoRdfProperty>> propertyToAccessor) {
            //Function<? super PropertySource, ? extends Object> getIdentifier) {
        super(NodeFactory.createBlankNode());

        // this.blankNodeId = NodeFactory.createBlankNode(); //BlankNodeId.create();

        this.source = source;
        this.predicateToAccessor = propertyToAccessor;
    }

    public Map<String, Function<PropertySource, PseudoRdfProperty>> getPropertyToAccessor() {
        return predicateToAccessor;
    }

    public PropertySource getSource() {
        return source;
    }

//	public Object getIdentifier() {
//		Object result = getIdentifier.apply(source);
//		return result;
//	}



    @Override
    public boolean isBlank() { return true; }

    public Node getBlankNode() {
        return blankNodeId;
    }

    @Override
    public String getBlankNodeLabel()  { return get().getBlankNodeLabel(); } //return (BlankNodeId) label; }

    @Override
    public Object visitWith(NodeVisitor v) {
        throw new RuntimeException("Visiting not supported");
    }

    @Override
    public boolean equals(Object other)
    {
        boolean result;

        if (other instanceof NodeView) {
            NodeView o = (NodeView)other;
            result = this == other ||
                      (Objects.equals(source, o.source) &&
                      Objects.equals(predicateToAccessor, o.predicateToAccessor));
        } else {
//            Node otherNode = other instanceof NodeView
//                    ? ((NodeView)other).getBlankNode()
//                    : other instanceof Node
//                        ? (Node)other
//                        : null;
//
//            result = otherNode == null
//                    ? false
//                    : this.blankNodeId.equals(otherNode);
            result = false;
        }

        return result;
    }

//    @Override
//    public int hashCode() {
//        int result = this.blankNodeId.hashCode(); // Objects.hash(source, predicateToAccessor);
//        return result;
//    }

    public PseudoRdfProperty getProperty(String property) {
        Function<PropertySource, PseudoRdfProperty> schemaProperty = predicateToAccessor.get(property);
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
        Set<String> allKnownProperties = predicateToAccessor.keySet();
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

                result = result.andThen(it.mapWith(o -> Triple.create(this, p, o)));//new StatementImpl(this, p, o)));
            }
            //((ExtendedIterator<PseudoRdfNode>>)getPropertyValues(str).iterator())
        }

        return result;
    }

    @Override
    public String toString() {
        return get().getBlankNodeLabel();
    }

    @Override
    public String toString(PrefixMapping pmap) {
        return toString();
    }

//    @Override
//    public String toString() {
//        return super.toString() + " " + blankNodeId.getBlankNodeLabel();
//    }

//        if ( this == other ) return true ;
//        return other instanceof Node_Blank && label.equals( ((Node_Blank) other).label ); }
}
