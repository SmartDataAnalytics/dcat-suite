package org.aksw.dcat_suite.algebra;

import java.util.Collections;
import java.util.List;

import org.aksw.jena_sparql_api.mapper.annotation.IriNs;
import org.aksw.jena_sparql_api.mapper.annotation.PolymorphicOnly;

public interface Op1
	extends Op
{
	@IriNs("eg")
	@PolymorphicOnly
	Op getSubOp();
	Op1 setSubOp(Op subOp);

	default List<Op> getSubOps() {
		return Collections.singletonList(getSubOp());
	}
}