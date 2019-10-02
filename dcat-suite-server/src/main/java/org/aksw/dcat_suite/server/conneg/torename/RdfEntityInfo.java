package org.aksw.dcat_suite.server.conneg.torename;

import java.util.Collection;
import java.util.List;

import org.aksw.jena_sparql_api.http.repository.api.EntityInfoCore;
import org.apache.jena.rdf.model.Resource;

public interface RdfEntityInfo
	extends Resource, EntityInfoCore
{
	RdfEntityInfo setContentEncodings(List<String> enocdings);
	RdfEntityInfo setContentType(String contentType);
	RdfEntityInfo setCharset(String charset);
	RdfEntityInfo setContentLength(Long length);	
	
	Collection<HashInfo> getHashes();
	
//	@ToString
//	default $toString() {
//		
//	}
	
	default HashInfo getHash(String algo) {
		HashInfo result = getHashes().stream()
			.filter(x -> algo.equalsIgnoreCase(x.getAlgorithm()))
			.findAny()
			.orElse(null);
		return result;
	}
}
