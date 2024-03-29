package org.aksw.dcat.jena.domain.annotation;

import java.util.Set;

import org.aksw.dcat.jena.domain.api.DcatDistribution;
import org.aksw.dcat.jena.term.DcatTerms;
import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.RdfType;
import org.aksw.jenax.annotation.reprogen.ResourceView;

@ResourceView(DcatDistribution.class)
@RdfType(DcatTerms.Distribution)
public interface DcatDistributionDefault
    extends DcatDistribution
{
    @Override
    @Iri(DcatTerms.accessURL)
    Set<String> getAccessUrls();

    @Override
    @Iri(DcatTerms.downloadURL)
    Set<String> getDownloadUrls();

    @Override
    @Iri("http://purl.org/dc/terms/format")
    String getFormat();
}
