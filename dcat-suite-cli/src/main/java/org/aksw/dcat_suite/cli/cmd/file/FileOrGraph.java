package org.aksw.dcat_suite.cli.cmd.file;

/**
 * Helper class to disambiguate whether the content referred to by an IRI is
 * obtained by accessing a named graph with that name or whether to
 * resolve the IRI.
 *
 * @author raven
 *
 */
public class FileOrGraph {
    protected boolean isFile;
    protected String iri;

    public FileOrGraph(boolean isFile, String iri) {
        super();
        this.isFile = isFile;
        this.iri = iri;
    }
    public boolean isFile() {
        return isFile;
    }

    public String getIri() {
        return iri;
    }
}
