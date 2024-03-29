package org.aksw.dcat_suite.cli.cmd.catalog;

import org.aksw.jenax.dataaccess.sparql.factory.datasource.RdfDataSourceSpecBasicFromMap;
import org.aksw.jenax.dataaccess.sparql.factory.datasource.RdfDataSourceSpecBasicMutable;

import picocli.CommandLine.Option;

/**
 * Cli Parameters for configuring the engine used for processing datasets
 *
 * @author raven
 *
 */
public class CmdMixinDatasetEngine
    extends RdfDataSourceSpecBasicFromMap
{
    @Option(names = { "--data-engine" }, description="SPARQL Engine. Supported: 'mem', 'tdb2', 'difs'")
    @Override
    public RdfDataSourceSpecBasicMutable setEngine(String engine) { return super.setEngine(engine); }

    @Option(names = { "--data-fs" }, description="FileSystem URL against which to interpret --db-location (e.g. for webdav, leave empty for local fs).")
    @Override
    public RdfDataSourceSpecBasicMutable setLocationContext(String locationContext) { return super.setLocationContext(locationContext); }

//    @Option(names = { "--data-loc" }, description="Access location to the database; interpreted w.r.t. engine. May be an URL, directory or file.")
//    @Override
//    public RdfDataSourceSpecBasicMutable getLocation() { return super.getLocation(); }

    @Option(names = { "--data-loader" }, description="Wrap a datasource's default loading strategy with a different one. Supported values: sansa")
    @Override
    public RdfDataSourceSpecBasicMutable setLoader(String loader) { return super.setLoader(loader); }
}

