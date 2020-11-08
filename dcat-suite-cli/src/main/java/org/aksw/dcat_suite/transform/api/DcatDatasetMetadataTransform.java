package org.aksw.dcat_suite.transform.api;

import org.aksw.dcat.jena.domain.api.DcatDataset;

/**
 * Interface to initialize or enrich a {@link DcatDataset} that represents
 * the result of a dataset transformation
 * with information based on the source dataset used in the transformation.
 *
 *
 * @author raven
 *
 */
public interface DcatDatasetMetadataTransform {

    /**
     * Enrich targetDcatDataset based on the information in sourceDcatDataset
     *
     * @param targetDcatDataset
     * @param sourceDcatDataset
     * @return Either targetDcatDataset or a fresh resource with the appropriate enrichment
     */
    DcatDataset apply(DcatDataset targetDcatDataset, DcatDataset sourceDcatDataset);

}
