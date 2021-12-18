package org.aksw.dcat_suite.cli.cmd.catalog;

import java.util.ArrayList;
import java.util.List;

import picocli.CommandLine.Option;

public class DcatCatalogFilter {

    @Option(names = { "--filter-search" },
            description = "Filter by the given search strings")
    protected List<String> searchTerms = new ArrayList<>();

    @Option(names = { "--filter-format" },
            description = "Filter by the given format")
    protected List<String> searchFormats = new ArrayList<>();
}
