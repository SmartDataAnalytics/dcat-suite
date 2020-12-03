package org.aksw.ckan.domain;

import org.aksw.ckan_deploy.core.CKAN;
import org.aksw.jena_sparql_api.mapper.annotation.Iri;
import org.apache.jena.rdf.model.Resource;

public interface CkanEntity
    extends Resource
{
    @Iri(CKAN.Strs.id)
    String getCkanId();
    CkanEntity setCkanId(String id);

    @Iri(CKAN.Strs.name)
    String getCkanName();
    CkanEntity setCkanName(String name);
}
