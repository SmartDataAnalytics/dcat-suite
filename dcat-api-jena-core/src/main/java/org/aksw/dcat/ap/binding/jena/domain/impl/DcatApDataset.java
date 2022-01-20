package org.aksw.dcat.ap.binding.jena.domain.impl;

import java.util.Set;

import org.aksw.dcat.ap.domain.api.DcatApDatasetCore;
import org.aksw.dcat.jena.domain.api.DcatDataset;
import org.aksw.jenax.annotation.reprogen.Iri;
import org.apache.jena.rdf.model.Resource;

public interface DcatApDataset
    extends DcatDataset, DcatApDatasetCore
{
    @Iri("dcat:distribution")
    @Override
    <T extends Resource> Set<T> getDistributions(Class<T> clazz);

    @Override
    default Set<? extends DcatApDistribution> getDistributions() {
        return getDistributions(DcatApDistribution.class);
    }

    DcatApDistribution createDistribution();

//    default <T extends Resource> Set<? extends DcatApDistribution> getDistributions() {
//        return getDistributions(DcatApDistribution.class);
//    }
}
