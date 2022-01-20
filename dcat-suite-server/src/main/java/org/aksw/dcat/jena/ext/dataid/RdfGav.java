package org.aksw.dcat.jena.ext.dataid;

import org.aksw.jenax.annotation.reprogen.IriNs;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.apache.jena.rdf.model.Resource;

@ResourceView
public interface RdfGav
	extends Resource
{
	@IriNs("eg")
	String getGroupId();
	RdfGav setGroupId(String groupId);

	@IriNs("eg")
	String getArtifactId();
	RdfGav setArtifactId(String artifactId);

	@IriNs("eg")
	String getVersion();
	RdfGav setVersion(String version);
}
