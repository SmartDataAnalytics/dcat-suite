package org.aksw.dcat.repo.impl.fs;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

import org.aksw.dcat.repo.api.CatalogResolver;
import org.aksw.dcat.repo.api.DatasetResolver;
import org.aksw.dcat.repo.api.DistributionResolver;

import io.reactivex.rxjava3.core.Maybe;

public interface CatalogResolverCacheCapable
    extends CatalogResolver
{
     DistributionResolver doCacheDistribution(
        String requestDistributionId,
        DistributionResolver dr);

     Maybe<URL> doCacheDownload(URL downloadUrl) throws IOException;

     CompletableFuture<Path> doCacheDistribution(
        String datasetId,
        String requestDistributionId,
        DistributionResolver dr, URL urlObj);

     DatasetResolver doCacheDataset(String requestId, DatasetResolver dr) throws Exception;
}
