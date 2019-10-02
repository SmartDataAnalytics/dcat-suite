package org.aksw.dcat.jena.domain.annotation;

import java.util.Set;

import org.aksw.dcat.jena.domain.api.DcatDistribution;
import org.aksw.jena_sparql_api.mapper.annotation.Iri;
import org.aksw.jena_sparql_api.mapper.annotation.RdfType;
import org.aksw.jena_sparql_api.mapper.annotation.ResourceView;
import org.aksw.jena_sparql_api.vocab.DCAT;

@ResourceView(DcatDistribution.class)
@RdfType(DCAT.Strs.Distribution)
public interface DcatDistributionDefault
	extends DcatDistribution
{	
	@Override
	@Iri(DCAT.Strs.accessURL)
	Set<String> getAccessURLs();

	@Override
	@Iri(DCAT.Strs.downloadURL)
	Set<String> getDownloadURLs();

	@Override
	@Iri("http://purl.org/dc/terms/format")
	String getFormat();
}
