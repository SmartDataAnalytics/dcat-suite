package org.aksw.ckan_deploy.core;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.dcat.jena.domain.api.DcatDataset;
import org.aksw.dcat.jena.domain.api.DcatDistribution;
import org.apache.jena.ext.com.google.common.collect.Sets;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.util.ResourceUtils;
import org.apache.jena.vocabulary.DCAT;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;

import com.google.common.base.Predicate;
import com.google.common.collect.Streams;

public class DcatUtils {
	
	public static final String extraNs = "http://ckan.aksw.org/ontology/extra/";
	public static final Property extraUri = ResourceFactory.createProperty(extraNs + "uri");
	
	public static <T extends PrefixMapping> T addPrefixes(T result) {
		result
			.setNsPrefixes(PrefixMapping.Extended)
			.setNsPrefix("dcat", DCAT.NS)
			.setNsPrefix("dct", DCTerms.NS)
			.setNsPrefix("extra", extraNs);
		

		return result;
	}
	
	public static Model createModelWithDcatFragment(Model model) {
		Model result = ModelFactory.createDefaultModel();
		createModelWithDcatFragment(result, model);
		return result;
	}
	
	public static Model createModelWithDcatFragment(Model result, Model model) {
		Collection<DcatDataset> ckanDatasets = DcatUtils.listDcatDatasets(model);

		ckanDatasets.stream()
			.map(ResourceUtils::reachableClosure)
			.forEach(result::add);
		
		return result;
	}

	public static Model createModelWithNormalizedDcatFragment(String fileOrUrl) {
		Dataset dataset = RDFDataMgr.loadDataset(fileOrUrl);
		Model result = DcatUtils.createModelWithNormalizedDcatFragment(dataset);
		return result;
	}

	public static Model createModelWithNormalizedDcatFragment(Dataset dataset) {
		Model result = createModelWithDcatFragment(dataset);
		DcatCkanRdfUtils.normalizeDcatModel(result);
		DcatUtils.addPrefixes(result);
		return result;
	}
	
	public static Model createModelWithDcatFragment(Dataset dataset) {
		Model result = ModelFactory.createDefaultModel();			
		
		Stream.concat(Stream.of(dataset.getDefaultModel()),
				Streams.stream(dataset.listNames())
					.map(dataset::getNamedModel))
			.forEach(m -> createModelWithDcatFragment(result, m));
			
		return result;
	}
	
	public static Collection<DcatDataset> listDcatDatasets(org.apache.jena.query.Dataset sparqlDataset) {
		Model model = sparqlDataset.getDefaultModel();
		Collection<DcatDataset> result = listDcatDatasets(model);

		return result;
	}

	public static Collection<DcatDataset> listDcatDatasets(Model model) {
		Collection<DcatDataset> result = 
				model.listSubjectsWithProperty(RDF.type, DCAT.Dataset).andThen(
				model.listSubjectsWithProperty(DCAT.distribution))
				.mapWith(r -> r.as(DcatDataset.class))
				.toSet();

		return result;
	}
	
	
	/**
	 * The behavior is as follows:
	 * If any accessURL matches the name of a graph, then
	 * - if all accessURLs point to a graph, the set of graph names is returned
	 * - if any accessURL does not point to a graph, an exception is thrown
	 * - returns an empty optional otherwise
	 * 
	 * 
	 * @param dcatDistribution
	 * @param isGraphName
	 * @return
	 */
	public static Optional<Collection<Resource>> tryGetGraphAccessURLs(DcatDistribution dcatDistribution, Predicate<String> isGraphName) {
		Set<Resource> accessURLs = dcatDistribution.getAccessURLs().stream()
				.map(ResourceFactory::createResource)
				.collect(Collectors.toSet());
		
		// The set of accessURLs that match graphs
		Set<Resource> graphAccessURLs = accessURLs.stream()
				.filter(Resource::isURIResource)
				.filter(r -> isGraphName.test(r.getURI()))
				.collect(Collectors.toSet());

		Optional<Collection<Resource>> result;
		
		if(!graphAccessURLs.isEmpty()) {
			Set<Resource> diff = Sets.difference(accessURLs, graphAccessURLs);
			if(!diff.isEmpty()) {
				accessURLs.forEach(r -> {
					System.out.println("  [x] means the dataset contains a graph with that IRI ");
					System.out.println("  " + r + " [" + (graphAccessURLs.contains(r) ? "x" : " ") + "]");
				});
				throw new RuntimeException("If any access URL maps to a graph, any others have to do so as well");
			}
			
			result = Optional.ofNullable(graphAccessURLs);
		} else {
			result = Optional.empty();
		}

		return result;
	}
}
