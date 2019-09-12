package org.aksw.jena_sparql_api.data_client.algebra;

import org.aksw.jena_sparql_api.mapper.annotation.IriNs;
import org.aksw.jena_sparql_api.mapper.annotation.PolymorphicOnly;

public interface Op1
	extends Op
{
	@IriNs("eg")
	@PolymorphicOnly
	Op getSubOp();
	OpUpdateRequest setSubOp(Op op);		
}
