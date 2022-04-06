package org.aksw.dcat_suite.cli.cmd.file;

import org.aksw.dcat.jena.domain.api.MavenEntity;
import org.apache.jena.rdf.model.Resource;

public class DcatIdUtils {
    public static String createDatasetId(Resource dcatDataset) {
        MavenEntity mvnEntity = dcatDataset.as(MavenEntity.class);
        String result = mvnEntity.getGroupId() + ":" + mvnEntity.getArtifactId() + ":" + mvnEntity.getVersion();
        return result;
    }
}
