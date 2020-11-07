package org.aksw.ckan_deploy.core;

import org.aksw.jena_sparql_api.pseudo_rdf.NodeView;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.graph.NodeTransform;

/**
 * This transform substitutes each {@link NodeView} with its
 * internal blank node.
 *
 * @author raven
 *
 */
public class NodeTransformNodeViewToBlankNode
    implements NodeTransform
{
    public static final NodeTransform INSTANCE = new NodeTransformNodeViewToBlankNode();

    @Override
    public Node apply(Node t) {
        Node result = t instanceof NodeView
                ? ((NodeView)t).getBlankNode()
                : t;
        return result;
    }

}
