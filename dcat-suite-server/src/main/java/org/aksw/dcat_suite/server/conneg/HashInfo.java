package org.aksw.dcat_suite.server.conneg;

import org.aksw.dcat.ap.domain.api.Spdx;
import org.aksw.jena_sparql_api.mapper.annotation.Iri;
import org.aksw.jena_sparql_api.mapper.annotation.ResourceView;
import org.apache.jena.rdf.model.Resource;

@ResourceView
public interface HashInfo
	extends Resource
{
	@Iri(Spdx._algorithm)
	String getAlgorithm();
	HashInfo setAlgorithm(String algorithm);
	
	@Iri(Spdx._checksum)
	String getChecksum();
	HashInfo setChecksum(String checksum);
}