package org.aksw.dcat_suite.core.util;

import java.io.IOException;
import java.util.List;

import org.aksw.dcat.repo.api.CatalogResolver;
import org.aksw.dcat.repo.api.DatasetResolver;
import org.aksw.dcat.repo.api.DistributionResolver;
import org.aksw.jena_sparql_api.http.repository.api.HttpResourceRepositoryFromFileSystem;
import org.aksw.jena_sparql_api.http.repository.api.RdfHttpEntityFile;
import org.aksw.jena_sparql_api.http.repository.impl.HttpResourceRepositoryFromFileSystemImpl;
import org.apache.http.Header;
import org.apache.http.HttpRequest;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;

public class CatalogRequestUtils {
    public static HttpUriRequest createRequest(String id, Header[] apacheHeaders) {
        HttpUriRequest request = RequestBuilder.get(id).build();

        request.setHeaders(apacheHeaders);
//
//		//List<Entry<String, String>> tmp = flattenHeaders(springHeaders).collect(Collectors.toList());
//		List<Entry<String, String>> tmp = HttpHeaderUtils.toEntries(apacheHeaders).collect(Collectors.toList());
//		for(Entry<String, String> e : tmp) {
//			String k = e.getKey();
//			String v = e.getValue();
//			request.addHeader(k, v);
//		}

        return request;
    }

    public static RdfHttpEntityFile resolveEntity(CatalogResolver catalogResolver,
            HttpResourceRepositoryFromFileSystem datasetRepository, HttpRequest r
    // String id,
    // Header[] apacheHeaders
    ) {
        String id = r.getRequestLine().getUri();
        Header[] apacheHeaders = r.getAllHeaders();

        RdfHttpEntityFile result = null;

        DatasetResolver datasetResolver = catalogResolver.resolveDataset(id).blockingGet();

        if (datasetResolver != null) {

            List<DistributionResolver> dists = datasetResolver.resolveDistributions().toList().blockingGet();

            if (!dists.isEmpty()) {
                DistributionResolver dist = dists.iterator().next();
                String downloadUrl = dist.getDistribution().getDownloadUrl();

                if (downloadUrl != null) {
                    // Header[] apacheHeaders = springToApache(springHeaders);

                    HttpUriRequest request = createRequest(downloadUrl, apacheHeaders);

                    // HttpUriRequest request = RequestBuilder
                    // .get(downloadUrl)
                    // .build();
                    // //List<Entry<String, String>> tmp =
                    // flattenHeaders(springHeaders).collect(Collectors.toList());
                    // List<Entry<String, String>> tmp =
                    // HttpHeaderUtils.toEntries(apacheHeaders).collect(Collectors.toList());
                    // for(Entry<String, String> e : tmp) {
                    // String k = e.getKey();
                    // String v = e.getValue();
                    // request.addHeader(k, v);
                    // }

                    try {
                        result = datasetRepository.get(request,
                                HttpResourceRepositoryFromFileSystemImpl::resolveRequest);
                    } catch (IOException e1) {
                        throw new RuntimeException(e1);
                    }
                    //
                    // if(entity == null) {
                    // throw new RuntimeException("Should not happen");
                    // }
                }
            }
        }

        return result;
    }
}
