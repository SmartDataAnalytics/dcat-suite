package org.aksw.dcat.ap.binding.jena.domain.impl;

import java.util.Set;

import org.aksw.dcat.ap.domain.api.DcatApDatasetCore;
import org.aksw.dcat.jena.domain.api.DcatDataset;

public interface DcatApDataset
    extends DcatDataset, DcatApDatasetCore
{
    @Override
    default Set<? extends DcatApDistribution> getDistributions() {
        return getDistributionsAs(DcatApDistribution.class);
    }

    default Set<DcatApDistribution> getDcatApDistributions() {
        return getDistributionsAs(DcatApDistribution.class);
    }

    
    DcatApDistribution createDistribution();

//    default <T extends Resource> Set<? extends DcatApDistribution> getDistributions() {
//        return getDistributions(DcatApDistribution.class);
//    }
}
