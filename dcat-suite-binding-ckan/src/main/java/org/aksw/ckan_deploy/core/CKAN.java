package org.aksw.ckan_deploy.core;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResourceFactory;

/**
 * Vocabulary for capturing CKAN specific aspects in RDF
 *
 * @author raven
 *
 */
public class CKAN {
    public static final String ns = "http://ckan.aksw.org/ontology/";

    /** Property for a link between a resource and its owning dataset */
    public static final Property resource = ResourceFactory.createProperty(Strs.resource);

    /** A CKAN entity's identifier */
    public static final Property id = ResourceFactory.createProperty(Strs.id);

    /** A CKAN entity's name */
    public static final Property name = ResourceFactory.createProperty(Strs.name);


    public static class Strs {
        public static final String resource = ns + "resource";

        /** A CKAN entity's identifier */
        public static final String id = ns + "id";

        /** A CKAN entity's name */
        public static final String name = ns + "name";

    }
}
