package org.aksw.jena_sparql_api.pseudo_rdf;

import java.util.Map;

import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.XSD;

public class MappingUtils {
    public static Map<String, Object> getMappingRegistry() {
        Map<String, Object> result = null;
        return result;
    }

    /**
     * Apply defaults to resources that represent DCAT-CKAN mappings.
     * <ul>
     *   <li>If the resource does not have a concrete mapping type assume a literal mapping</li>
     *   <li>If the literal mapping is without type then apply xsd:string</li>
     * </ul>
     * 
     * @param r A Resource which is assumed to represent a DCAT-CKAN  mapping.
     */
    public static void applyMappingDefaults(Resource r) {
        // If the resource does not have a concrete mapping type assume a literal mapping
        if (!r.hasProperty(RDF.type)) {
            r.addProperty(RDF.type, MappingVocab.LiteralMapping);
        }

        // If the literal mapping is without type then apply xsd:string
        if(r.hasProperty(RDF.type, MappingVocab.LiteralMapping) || r.hasProperty(RDF.type, MappingVocab.CollectionMapping)) {
            if (!r.hasProperty(MappingVocab.type)) {
                r.addProperty(MappingVocab.type, XSD.xstring);
            }
        }
    }
}