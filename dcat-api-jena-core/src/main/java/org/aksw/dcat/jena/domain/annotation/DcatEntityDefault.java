package org.aksw.dcat.jena.domain.annotation;

import org.aksw.dcat.jena.domain.api.DcatEntity;
import org.aksw.jena_sparql_api.mapper.annotation.Iri;
import org.aksw.jena_sparql_api.mapper.annotation.ResourceView;


@ResourceView(DcatEntity.class)
public interface DcatEntityDefault
	extends DcatEntity
{
	@Override
	@Iri("http://purl.org/dc/terms/identifier")
	String getIdentifier();
	

	@Override
	@Iri("http://purl.org/dc/terms/title")
	String getTitle();
	

	@Override
	@Iri("http://purl.org/dc/terms/description")
	String getDescription();
}
