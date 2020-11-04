package org.aksw.dcat_suite.cli.cmd;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import org.aksw.dcat.repo.api.CatalogResolver;
import org.aksw.dcat_suite.cli.main.MainCliDcatSuite;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "data", description = "Show data")
public class CmdData
    implements Callable<Integer>
{
    @Parameters(description = "Non option args")
    protected List<String> nonOptionArgs;

//		// ArtifactID - can refer to any dataset, distribution, download
//		protected String artifactId;
    @Option(names={"-c", "--catalog"}, description = "Catalog reference")
    protected List<String> catalogs = Collections.emptyList();

    // Note: format is more generic than content-type as csv or rdf.gzip are valid formats
    // So a format is any string from which content type and encoding can be inferred
    @Option(names={"-f", "--format"}, description = "Preferred format / content type")
    protected String contentType = "text/turtle";

    @Option(names={"-e", "--encoding"}, description = "Preferred encoding(s)")
    protected List<String> encodings = Collections.emptyList();

    @Option(names={"-l", "--link"}, description = "Instead of returning the content directly, return a file url in the cache")
    protected boolean link = false;

    @Option(names = "--help", usageHelp = true)
    protected boolean help = false;

    @Override
    public Integer call() throws Exception {
        CmdData cmData = this;

        List<String> noas = cmData.nonOptionArgs;
        if(noas.size() != 1) {
            throw new RuntimeException("Only one non-option argument expected for the artifact id");
        }
        String artifactId = noas.get(0);

        CatalogResolver effectiveCatalogResolver = MainCliDcatSuite.createEffectiveCatalogResolver(cmData.catalogs);
        MainCliDcatSuite.showData(effectiveCatalogResolver, artifactId, cmData.contentType, cmData.encodings, cmData.link);

        return 0;
    }
}