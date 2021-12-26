package org.aksw.dcat.repo.impl.ckan;

import java.net.URL;

import org.aksw.ckan_deploy.core.DcatCkanRdfUtils;
import org.aksw.commons.io.util.UrlUtils;
import org.aksw.dcat.jena.domain.api.DcatDataset;
import org.aksw.dcat.jena.domain.api.DcatDistribution;
import org.aksw.dcat.repo.api.CatalogResolver;
import org.aksw.dcat.repo.api.DatasetResolver;
import org.aksw.dcat.repo.api.DistributionResolver;
import org.aksw.dcat.repo.impl.core.DatasetResolverImpl;
import org.aksw.dcat.utils.DcatUtils;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.shared.impl.PrefixMappingImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.trentorise.opendata.jackan.CkanClient;
import eu.trentorise.opendata.jackan.model.CkanDataset;
import eu.trentorise.opendata.jackan.model.CkanResource;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;

public class CatalogResolverCkan
    implements CatalogResolver
{
    private static final Logger logger = LoggerFactory.getLogger(CatalogResolverCkan.class);

    protected CkanClient ckanClient;
    //protected String prefix;

    public CatalogResolverCkan(CkanClient ckanClient) {
//		this(ckanClient, null);
        super();
        this.ckanClient = ckanClient;
    }

//	public CatalogResolverCkan(CkanClient ckanClient, String prefix) {
//		this.ckanClient = ckanClient;
//		this.prefix = prefix;
//	}

    @Override
    public Flowable<Resource> search(String pattern) {
        throw new RuntimeException("Not supported yet");
    }

    @Override
    public Maybe<DatasetResolver> resolveDataset(String datasetId) {
        return Maybe.fromCallable(() -> {
            CkanDataset ckanDataset = ckanClient.getDataset(datasetId);

            PrefixMapping pm = DcatUtils.addPrefixes(new PrefixMappingImpl());

            DcatDataset dcatDataset = DcatCkanRdfUtils.convertToDcat(ckanDataset, pm);

            try {
                // Skolemize the resource first (so we have a reference to the resource)
                dcatDataset = DcatCkanRdfUtils.skolemizeClosureUsingCkanConventions(dcatDataset).as(DcatDataset.class);
    //			if(prefix != null) {
    //				dcatDataset = DcatCkanRdfUtils.assignFallbackIris(dcatDataset, prefix).as(DcatDataset.class);
    //			}

            } catch(Exception e) {
                logger.warn("Error processing dataset: " + datasetId, e);
            }

            return new DatasetResolverImpl(this, dcatDataset);
        });

        //RDFDataMgr.write(System.out, dcatDataset.getModel(), RDFFormat.NTRIPLES);

//		return Maybe.just(new DatasetResolverCkan(this, dcatDataset));
    }

    @Override
    public Flowable<DistributionResolver> resolveDistribution(String distributionId) {

        // TODO Search ckan by extra:uri field - but this needs reconfiguration of ckan...
        CkanResource ckanResource = ckanClient.getResource(distributionId);
        String datasetId = ckanResource.getPackageId();

        Flowable<DistributionResolver> result = resolveDataset(datasetId)
                .toFlowable().flatMap(dr -> dr.resolveDistribution(distributionId));

        return result;
    }

    @Override
    public Flowable<DistributionResolver> resolveDistribution(String datasetId, String distributionId) {
        Maybe<DatasetResolver> datasetResolver = resolveDataset(datasetId);

        Flowable<DistributionResolver> result = datasetResolver.toFlowable().flatMap(dr ->
            Flowable.fromIterable(dr.getDataset().getDistributions2())
                .filter(d -> d.isURIResource() && d.getURI().equals(distributionId))
                .map(d -> d.as(DcatDistribution.class))
                .map(dcatDistribution -> new DistributionResolverCkan(dr, dcatDistribution)));

        return result;
    }

    @Override
    public Flowable<DistributionResolver> resolveDistribution(DcatDataset dataset, String distributionId) {
        Flowable<DistributionResolver> result = resolveDataset(dataset.getURI()).toFlowable()
                .flatMap(d -> d.resolveDistribution(distributionId));
        // atasetResolver::resolveDistribution
        return result;
    }

    @Override
    public Maybe<URL> resolveDownload(String downloadUri) {
        URL url = UrlUtils.newURL(downloadUri);
        return Maybe.just(url);
    }
}