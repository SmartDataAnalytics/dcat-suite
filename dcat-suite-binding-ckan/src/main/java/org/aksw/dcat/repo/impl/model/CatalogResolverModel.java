package org.aksw.dcat.repo.impl.model;

import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.aksw.dcat.jena.domain.api.DcatDataset;
import org.aksw.dcat.jena.domain.api.DcatDistribution;
import org.aksw.dcat.repo.api.CatalogResolver;
import org.aksw.dcat.repo.api.DatasetResolver;
import org.aksw.dcat.repo.api.DistributionResolver;
import org.aksw.dcat.repo.impl.core.DatasetResolverImpl;
import org.aksw.dcat.repo.impl.core.DistributionResolverImpl;
import org.aksw.jena_sparql_api.rdf.collections.ResourceUtils;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.DCAT;

import com.google.common.collect.Maps;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;

// Use catalog resolver Sparql instead
@Deprecated
public class CatalogResolverModel
    implements CatalogResolver
{
    protected Model model;

    public CatalogResolverModel(Model model) {
        this.model = model;
    }

    @Override
    public Flowable<Resource> search(String pattern) {
        throw new RuntimeException("not implemented");
    }


    @Override
    public Maybe<DatasetResolver> resolveDataset(String datasetId) {
        Resource r = model.getResource(datasetId);

        Maybe<DatasetResolver> result = r == null ? Maybe.empty() : Maybe.just(
                new DatasetResolverImpl(this, r.as(DcatDataset.class)));
        return result;
    }

    @Override
    public Flowable<DistributionResolver> resolveDistribution(String distributionId) {
        DcatDistribution rResource = model.createResource(distributionId).as(DcatDistribution.class);
        DcatDistribution rBlankNode = model.wrapAsResource(NodeFactory.createBlankNode(distributionId)).as(DcatDistribution.class);

        List<DistributionResolver> list =
            Arrays.asList(rResource, rBlankNode).stream().flatMap(r ->
                ResourceUtils.listReversePropertyValues(r, DCAT.distribution).toList().stream()
                    .map(s -> Maps.immutableEntry(s, r)))
            .map(e -> (DistributionResolver)new DistributionResolverImpl(new DatasetResolverImpl(this, e.getKey().as(DcatDataset.class)), e.getValue()))
            .collect(Collectors.toList());

        Flowable<DistributionResolver> result = Flowable.fromIterable(list);
        return result;
    }

    @Override
    public Maybe<URL> resolveDownload(String downloadUri) throws Exception {
        Maybe<URL> result = Maybe.just(new URL(downloadUri));
        return result;
    }

    @Override
    public Flowable<DistributionResolver> resolveDistribution(String datasetId, String distributionId) {
        return resolveDistribution(distributionId)
            .filter(r -> r.getDatasetResolver().getDataset().getURI().equals(datasetId));
    }

    @Override
    public Flowable<DistributionResolver> resolveDistribution(DcatDataset dataset, String distributionId) {
        return resolveDistribution(dataset.getURI(), distributionId);
    }

}
