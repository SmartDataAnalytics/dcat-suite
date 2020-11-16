package org.aksw.jena_sparql_api.conjure.graph.fs;

import java.nio.file.Path;

import org.aksw.jena_sparql_api.http.repository.impl.UriToPathUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.DatasetChanges;
import org.apache.jena.sparql.core.DatasetGraphMonitor;
import org.apache.jena.sparql.core.Quad;

public class DatasetGraphIndexerFromFileSystem {
    protected DatasetGraphMonitor monitoredGraph;
    protected DatasetChanges monitor;

    protected Path basePath;

    // We need that graph in order to re-use its mapping
    // from (subjcet) iris to paths
    protected DatasetGraphFromFileSystem syncedGraph;

    protected void onChange(Quad quad) {

        Node s = quad.getSubject();
        String tgtIri = s.getURI();
        Path tgtPat = syncedGraph.getRelPathForIri(tgtIri);
        String fileName = syncedGraph.getFilename();

    }



    public void test() {
        // monitoredGraph.getMonitor().change(qaction, g, s, p, o);
    }
}
