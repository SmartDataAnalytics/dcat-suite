package org.aksw.dcat_suite.transform.api;

import org.aksw.dcat.jena.domain.api.DcatDistribution;

public interface DcatDistributionTransform {
    public DcatDistribution transform(DcatDistribution sourceDcatDistribution);
}
