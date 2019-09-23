package org.aksw.dcat_suite.algebra;

import java.util.Collections;
import java.util.List;

public interface Op0
	extends Op
{
	@Override
	default List<Op> getSubOps() {
		return Collections.emptyList();
	}
}