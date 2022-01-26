package org.aksw.dcat.jena.domain.annotation;

import java.util.Collection;
import java.util.Set;

import org.aksw.dcat.jena.domain.api.DcatDataset;
import org.aksw.dcat.jena.domain.api.DcatDistribution;
import org.aksw.dcat.jena.term.DcatTerms;
import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.RdfType;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.apache.jena.rdf.model.Resource;

@ResourceView(DcatDataset.class)
@RdfType(DcatTerms.Dataset)
public interface DcatDatasetDefault
    extends DcatEntityDefault, DcatDataset
{
    @Override
    default DcatDistribution createDistribution() {
        return getModel().createResource().as(DcatDistribution.class);
    }

    @Override
    @Iri(DcatTerms.distribution)
    //@PolymorphicOnly
    <T extends Resource> Set<T> getDistributionsAs(Class<T> clazz);

    @Override
    @Iri(DcatTerms.keyword)
    Collection<String> getKeywords();
}
