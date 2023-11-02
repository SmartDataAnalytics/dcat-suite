package org.aksw.dcat_suite.cli.cmd.catalog;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.aksw.dcat.repo.api.CatalogResolver;
import org.aksw.dcat.repo.api.DatasetResolver;
import org.aksw.dcat.repo.impl.core.CatalogResolverUtils;
import org.aksw.jenax.dataaccess.sparql.dataengine.RdfDataEngine;
import org.aksw.jenax.dataaccess.sparql.factory.datasource.RdfDataSources;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.system.Txn;

import io.reactivex.rxjava3.core.Flowable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Parameters;

/**
 * Enrich data
 *
 * @author raven
 *
 */
@Command(name = "testing", mixinStandardHelpOptions = true, separator = "=", description="Derive new datasets from existing ones")
public class CmdDcatCatalogEnrich
    implements Callable<Integer>
{
    @Mixin
    public CmdMixinCatalogEngine catalogEngine = new CmdMixinCatalogEngine();

    @Mixin
    public DcatCatalogFilter distributionSelector = new DcatCatalogFilter();

    @Mixin
    public CmdMixinDatasetEngine datasetEngine = new CmdMixinDatasetEngine();

    // The suffix which to append to the distribution identifier in order to obtain the target graph name
    public String xformSuffix;


    @Parameters(arity = "0..*", description = "The names or identifiers of transformations which to apply")
    public List<String> transformationIds = new ArrayList<>();

    @Override
    public Integer call() throws Exception {

        // Set up the catalog data source

        // Apply the selector

        // Provision each dataset for the specified engine; this yields a new data source

        // Run the transformation on the provisioned dataset; obtain the target dataset


        RdfDataEngine catalogDataSource = RdfDataSources.setupRdfDataSource(catalogEngine.getMap());

        try (RDFConnection conn = catalogDataSource.getConnection()) {
            CatalogResolver catalog = CatalogResolverUtils.createCatalogResolver(conn, transformationIds);
            // long numItems = flow.count().blockingGet();
            List<Resource> list = Txn.calculateRead(conn, () -> {
                Flowable<DatasetResolver> flow = catalog.search("");
                List<Resource> r = flow.map(dr -> dr.getDataset().asResource()).toList().blockingGet();
                return r;
            });

            System.out.println("List: " + list);

        }

        catalogDataSource.close();
        // RdfDataSource catalogDataSource = RdfDataSources.setupRdfDataSource(catalogEngine.getMap());



        System.out.println("Working!");
        System.out.println(catalogEngine);
        System.out.println(datasetEngine);
        return 0;
    }
}
