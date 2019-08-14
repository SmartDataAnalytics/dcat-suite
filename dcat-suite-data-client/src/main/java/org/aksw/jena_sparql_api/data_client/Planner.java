package org.aksw.jena_sparql_api.data_client;

import org.apache.jena.update.UpdateRequest;

public interface Planner {
	ModelFlow plan(ModelFlow modelFlow, UpdateRequest updateRequest) {
		if(modelFlow.isFile()) {
			
		}
	}

}
