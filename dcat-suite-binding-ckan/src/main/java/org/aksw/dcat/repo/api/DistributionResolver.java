package org.aksw.dcat.repo.api;

import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;

import org.aksw.dcat.jena.domain.api.DcatDistribution;
import org.apache.jena.rdf.model.Resource;

import io.reactivex.rxjava3.core.Maybe;

public interface DistributionResolver {

    DatasetResolver getDatasetResolver();
    DcatDistribution getDistribution();

    /**
     * Open the distribution; throws an exception unless there is exactly 1 download url
     *
     * @return
     */
    InputStream open() throws Exception;
    InputStream open(String url) throws Exception;

    /**
     * Return the file path if it exists
     * @return
     */
    Path getPath();


    default CatalogResolver getCatalogResolver() {
        DatasetResolver datasetResolver = getDatasetResolver();
        CatalogResolver result = datasetResolver.getCatalogResolver();
        return result;
    }

    default InputStream open(Resource downloadUrl) throws Exception {
        InputStream result = open(downloadUrl.getURI());
        return result;
    }


    default Maybe<URL> resolveDownload(String downloadUri) throws Exception {
        CatalogResolver catalogResolver = getCatalogResolver();
        Maybe<URL> result = catalogResolver.resolveDownload(downloadUri);
        return result;
    }

    default Maybe<URL> resolveDownload() throws Exception {
        String downloadUri = getDistribution().getDownloadUrl();
        Maybe<URL> result = resolveDownload(downloadUri);
        return result;
    }


}