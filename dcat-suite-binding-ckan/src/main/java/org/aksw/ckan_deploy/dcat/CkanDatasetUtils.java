package org.aksw.ckan_deploy.dcat;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import eu.trentorise.opendata.jackan.model.CkanDataset;
import eu.trentorise.opendata.jackan.model.CkanPair;

public class CkanDatasetUtils {

    public static Map<String, String> getExtrasAsMap(CkanDataset ckanDataset) {
        Map<String, String> result = getExtrasAsMap(ckanDataset.getExtras());

        return result;
    }

    public static Map<String, String> getExtrasAsMap(Iterable<? extends CkanPair> extras) {
        Map<String, String> result = StreamSupport.stream(extras.spliterator(), false)
                .collect(Collectors.toMap(CkanPair::getKey, CkanPair::getValue));
        return result;
    }

    public static void putExtras(CkanDataset ckanDataset) {
        // TODO Implement
    }

}
