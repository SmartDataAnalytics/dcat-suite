package org.aksw.jena_sparql_api.conjure.graph.fs;

public interface DatasetGraphIndexPluginFactory {
    DatasetGraphIndexPlugin create(DatasetGraphWithSync graphWithSync);
}
