package org.aksw.dcat_suite.cli.cmd.catalog;

import org.aksw.jenax.arq.datasource.RdfDataSourceSpecBasicFromMap;
import org.aksw.jenax.arq.datasource.RdfDataSourceSpecBasicMutable;

import picocli.CommandLine.Option;

public class CmdMixinCatalogEngine
    extends RdfDataSourceSpecBasicFromMap
{
    @Option(names = { "--cat-engine" }, description="SPARQL Engine. Supported: 'mem', 'tdb2', 'difs'")
    @Override
    public RdfDataSourceSpecBasicMutable setEngine(String engine) { return super.setEngine(engine); }

    @Option(names = { "--cat-fs" }, description="FileSystem URL against which to interpret --db-location (e.g. for webdav, leave empty for local fs).")
    @Override
    public RdfDataSourceSpecBasicMutable setLocationContext(String locationContext) { return super.setLocationContext(locationContext); }

    @Option(names = { "--cat-loc" }, description="Access location to the database; interpreted w.r.t. engine. May be an URL, directory or file.")
    @Override
    public RdfDataSourceSpecBasicMutable setLocation(String location) { return super.setLocation(location); }

    @Option(names = { "--cat-loader" }, description="Wrap a datasource's default loading strategy with a different one. Supported values: sansa")
    @Override
    public RdfDataSourceSpecBasicMutable setLoader(String loader) {return super.setLoader(loader); }
}

