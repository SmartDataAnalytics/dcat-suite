package org.aksw.dcat_suite.server.conneg.torename;

import org.aksw.dcat.jena.ap.vocab.Spdx;
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
