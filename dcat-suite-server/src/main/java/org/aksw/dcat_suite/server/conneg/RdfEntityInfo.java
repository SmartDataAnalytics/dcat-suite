package org.aksw.dcat_suite.server.conneg;

import java.util.List;

import org.apache.jena.rdf.model.Resource;

public interface RdfEntityInfo
	extends Resource, EntityInfoCore
{
	RdfEntityInfo setContentEncodings(List<String> enocdings);
	RdfEntityInfo setContentType(String contentType);
	RdfEntityInfo setCharset(String charset);
	RdfEntityInfo setContentLength(Long length);
}
