package org.aksw.dcat_suite.cli.cmd;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import org.aksw.dcat.repo.api.CatalogResolver;
import org.aksw.dcat_suite.cli.main.MainCliDcatSuite;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;


@Command(name = "search", separator = "=", description = "Search DCAT catalogs")
public class CmdSearch
    implements Callable<Integer>
{
    @Parameters(description = "Search pattern (regex)")
    public List<String> nonOptionArgs;

//		// ArtifactID - can refer to any dataset, distribution, download
//		protected String artifactId;
    @Option(names={"-c", "--catalog"}, description = "Catalog reference")
    public List<String> catalogs = Collections.emptyList();

    // json output for processing by tools such as jq
    @Option(names = "--jq", description = "json output")
    public boolean jsonOutput = false;

    @Option(names = "--help", help = true)
    public boolean help = false;


    @Override
    public Integer call() throws Exception {
        CmdSearch cmSearch = this;

        List<String> noas = cmSearch.nonOptionArgs;
        if(noas.size() != 1) {
            throw new RuntimeException("Only one non-option argument expected for the artifact id");
        }
        String pattern = noas.get(0);

        CatalogResolver effectiveCatalogResolver = MainCliDcatSuite.createEffectiveCatalogResolver(cmSearch.catalogs);
        MainCliDcatSuite.searchDcat(effectiveCatalogResolver, pattern, cmSearch.jsonOutput);

        return 0;
    }
}
