package org.aksw.dcat_suite.transform.api;

import org.aksw.dcat.jena.domain.api.DcatDataset;

public interface DcatDatasetTransform {
    DcatDataset apply(DcatDataset dataset);
}
