package org.aksw.ckan_deploy.dcat;

import java.util.Map;
import java.util.stream.Collectors;

import eu.trentorise.opendata.jackan.model.CkanDataset;
import eu.trentorise.opendata.jackan.model.CkanPair;

public class CkanDatasetUtils {
	
	public static Map<String, Object> getExtrasAsMap(CkanDataset ckanDataset) {
		Map<String, Object> result = ckanDataset.getExtras().stream().
				collect(Collectors.toMap(CkanPair::getKey, CkanPair::getValue));
		return result;
	}
	
	public static void putExtras(CkanDataset ckanDataset) {
		// TODO Implement
	}

}
