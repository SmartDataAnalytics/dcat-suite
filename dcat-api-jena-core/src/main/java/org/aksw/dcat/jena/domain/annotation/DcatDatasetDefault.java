package org.aksw.dcat.jena.domain.annotation;

import java.util.Collection;
import java.util.Set;

import org.aksw.dcat.jena.domain.api.DcatDataset;
import org.aksw.dcat.jena.domain.api.DcatDistribution;
import org.aksw.jena_sparql_api.mapper.annotation.Iri;
import org.aksw.jena_sparql_api.mapper.annotation.RdfType;
import org.aksw.jena_sparql_api.mapper.annotation.ResourceView;
import org.aksw.jena_sparql_api.vocab.DCAT;
import org.apache.jena.rdf.model.Resource;

@ResourceView(DcatDataset.class)
@RdfType(DCAT.Strs.Dataset)
public interface DcatDatasetDefault
	extends DcatEntityDefault, DcatDataset
{
	@Override
	default DcatDistribution createDistribution() {
		return getModel().createResource().as(DcatDistribution.class);
	}
	
	@Override
	@Iri(DCAT.Strs.distribution)
	//@PolymorphicOnly
	<T extends Resource> Set<T> getDistributions(Class<T> clazz);

	@Override
	@Iri(DCAT.Strs.keyword)
	Collection<String> getKeywords();
}
