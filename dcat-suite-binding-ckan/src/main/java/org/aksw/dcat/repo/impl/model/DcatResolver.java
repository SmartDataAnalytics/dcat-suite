package org.aksw.dcat.repo.impl.model;

import java.util.List;

import org.aksw.jena_sparql_api.conjure.dataref.rdf.api.RdfDataRef;
import org.aksw.jenax.annotation.reprogen.IriNs;
import org.aksw.jenax.annotation.reprogen.RdfTypeNs;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.apache.jena.rdf.model.Resource;

@ResourceView
@RdfTypeNs("eg")
public interface DcatResolver
	extends Resource
{
	@IriNs("eg")
	RdfDataRef getDataRef();
	RdfDataRef setDataRef(RdfDataRef dataRef);
	
	@IriNs("eg")
	List<String> getViews();
	DcatResolver setViews(List<String> views);
}
