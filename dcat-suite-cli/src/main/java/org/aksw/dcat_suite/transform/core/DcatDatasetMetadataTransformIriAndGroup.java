package org.aksw.dcat_suite.transform.core;

import org.aksw.dcat.jena.domain.api.DcatDataset;
import org.aksw.dcat.jena.domain.api.MavenEntity;
import org.aksw.dcat_suite.transform.api.DcatDatasetMetadataTransform;

public class DcatDatasetMetadataTransformIriAndGroup
    implements DcatDatasetMetadataTransform
{
    protected String baseIri;
    protected String groupId;



    @Override
    public DcatDataset apply(DcatDataset targetDcatDataset, DcatDataset sourceDcatDataset) {
        MavenEntity mvnEntity = targetDcatDataset.as(MavenEntity.class);

        String effectiveGroup = groupId != null ? groupId : sourceDcatDataset.as(MavenEntity.class).getGroupId();
        mvnEntity.setGroupId(effectiveGroup);

        return null;
    }
}
