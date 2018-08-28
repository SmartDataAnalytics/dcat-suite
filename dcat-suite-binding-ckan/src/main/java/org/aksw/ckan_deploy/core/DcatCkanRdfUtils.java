package org.aksw.ckan_deploy.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.aksw.commons.util.strings.StringUtils;
import org.aksw.dcat.jena.domain.api.DcatDataset;
import org.aksw.dcat.jena.domain.api.DcatDistribution;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.util.ResourceUtils;
import org.apache.jena.vocabulary.DCAT;
import org.apache.jena.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.trentorise.opendata.jackan.model.CkanDataset;
import eu.trentorise.opendata.jackan.model.CkanPair;
import eu.trentorise.opendata.jackan.model.CkanResource;
import eu.trentorise.opendata.jackan.model.CkanTag;

public class DcatCkanRdfUtils {
	private static final Logger logger = LoggerFactory.getLogger(DcatCkanRdfUtils.class);

	
	public static void copyPropertyValues(Resource r, Property target, Property source) {
//		org.aksw.jena_sparql_api.utils.model.ResourceUtils
//			.listPropertyValues(r, source)
//			.forEach(v -> r.addProperty(target, v));		
		List<RDFNode> values = org.aksw.jena_sparql_api.utils.model.ResourceUtils
			.listPropertyValues(r, source).toList();
		
		values.forEach(v -> r.addProperty(target, v));		
	}
	
	public static void normalizeDcatModel(Model model) {
		DcatUtils.listDcatDatasets(model).forEach(DcatCkanRdfUtils::normalizeDataset);
	}
	
	public static void normalizeDataset(DcatDataset dcatDataset) {
		normalizeCommon(dcatDataset);

		for(DcatDistribution dcatDistribution : dcatDataset.getDistributions()) {
			normalizeDistribution(dcatDistribution);
		}
	}


	public static void normalizeDistribution(DcatDistribution dcatDistribution) {
		normalizeCommon(dcatDistribution);
	}

	public static void normalizeCommon(Resource r) {
		copyPropertyValues(r, DcatDeployVirtuosoUtils.dcatDefaultGraph, DcatDeployVirtuosoUtils.sdDefaultGraph);
	}
	
	public static Resource skolemizeClosureUsingCkanConventions(Resource r) {
		// Skolemize the entry resource
		Resource result = DcatCkanRdfUtils.skolemizeUsingCkanConventions(r);

		List<Resource> reachableSubjects = ResourceUtils.reachableClosure(result).listSubjects().toList();
		for(Resource s : reachableSubjects) {
			skolemizeUsingCkanConventions(s.inModel(result.getModel()));
		}
		
		return result;
	}
	
//	public static void skolemizeUsingCkanConventions(Model dcatModel) {
//		Collection<Resource> rs = dcatModel.listSubjects().toList();
//		for(Resource r : rs) {
//			skolemizeUsingCkanConventions(r);
//		}
//	}

//	public static Resource skolemizeClosure(Resource r) {
//		Collection<Resource> rs = dcatModel.listSubjects().toList();
//		for(Resource r : rs) {
//			skolemizeUsingCkanConventions(r);
//		}
//	}

	/**
	 * Rename blank nodes that have an extra:uri property
	 * 
	 * @param dcatEntity
	 * @return
	 */
	public static Resource skolemizeUsingCkanConventions(Resource dcatEntity) {
		Resource result;
		
		if(dcatEntity.isAnon()) {
			
	//		RDFDataMgr.write(System.err, dcatEntity.getModel(), RDFFormat.TURTLE_PRETTY);
			
			// Check if there is an extra:uri attribute
			String uri = org.aksw.jena_sparql_api.utils.model.ResourceUtils.tryGetPropertyValue(dcatEntity, DcatUtils.extraUri)
				.filter(RDFNode::isURIResource)
				.map(RDFNode::asResource)
				.map(Resource::getURI)
				.orElse(null);
	
			if(uri != null) {
				// remove the property
				org.aksw.jena_sparql_api.utils.model.ResourceUtils.setProperty(dcatEntity, DcatUtils.extraUri, null);
			}
			
			result = uri == null
					? dcatEntity
					: ResourceUtils.renameResource(dcatEntity, uri);
		} else {
			result = dcatEntity;
		}

		return result;
	}

	@SuppressWarnings("unlikely-arg-type")
	public static DcatDataset assignFallbackIris(DcatDataset dcatDataset, String baseIri) {
		// NOTE Create a copy to avoid concurrent modification
		for (DcatDistribution dcatDistribution : new ArrayList<>(dcatDataset.getDistributions())) {
			String iri = generateFallbackIri(dcatDistribution, baseIri);

			// Avoid cryptic errors about resources not found for badly modeled data
			if (!dcatDistribution.equals(dcatDataset)) {
				ResourceUtils.renameResource(dcatDistribution, iri);
			}
		}

		DcatDataset result;
		if (dcatDataset.isAnon()) {
			String iri = generateFallbackIri(dcatDataset, baseIri);
			result = ResourceUtils.renameResource(dcatDataset, iri).as(DcatDataset.class);
		} else {
			result = dcatDataset;
		}

		return result;
	}

	/**
	 * Default strategy to assign URIs under a given prefix.
	 * 
	 * TODO: Make use of extra:uri field if present
	 * 
	 * dataset IRI pattern: baseIri-dataset-${dct:identifier} resource IRI pattern:
	 * baseIri-distribution-${dct:title}
	 * 
	 * @param dcatDataset
	 * @param baseIri
	 * @return
	 */
	public static String generateFallbackIri(DcatDataset dcatDataset, String baseIri) {
		String result = Optional.ofNullable(dcatDataset.getIdentifier())
				.map(id -> baseIri + "dataset/" + StringUtils.urlEncode(id)).orElseThrow(
						() -> new RuntimeException("Cannot generate a IRI for a dataset without a local identifier"));

		return result;
	}

	public static String generateFallbackIri(DcatDistribution dcatDistribution, String baseIri) {
		String result = Optional.ofNullable(dcatDistribution.getIdentifier())
				.map(id -> baseIri + "distribution/" + StringUtils.urlEncode(id)).orElseThrow(
						() -> new RuntimeException("Cannot generate a IRI for a dataset without a local identifier"));

		return result;
	}

	/**
	 * This will create blank node resources for representing the ckan datsaet. You
	 * can use assignUris() for the default strategy to get rid of blank nodes - or
	 * use your own.
	 * 
	 * @param ckanDataset
	 * @return
	 */
	public static DcatDataset convertToDcat(CkanDataset ckanDataset, PrefixMapping pm) {
		Model model = ModelFactory.createDefaultModel();
		DcatDataset result = convertToDcat(model, ckanDataset, pm);
		return result;
	}

	public static DcatDataset convertToDcat(Model model, CkanDataset ckanDataset, PrefixMapping pm) {
		DcatDataset dcatDataset = model.createResource().as(DcatDataset.class);
		convertToDcat(dcatDataset, ckanDataset, pm);

		for (CkanResource ckanResource : ckanDataset.getResources()) {
			DcatDistribution dcatDistribution = model.createResource().as(DcatDistribution.class);
			dcatDataset.getDistributions(DcatDistribution.class).add(dcatDistribution);

			convertToDcat(dcatDistribution, ckanResource, pm);
		}

		return dcatDataset;
	}

	public static String tryAsIri(String str, PrefixMapping pm) {
		String result;

		if (str.startsWith("http://")) {
			result = str;
		} else {
			String expanded = pm.expandPrefix(str);

			result = expanded.equals(str) ? null : expanded;
		}

		return result;
	}

	public static void convertToDcat(DcatDataset dcatDataset, CkanDataset ckanDataset, PrefixMapping pm) {

		dcatDataset.addProperty(RDF.type, DCAT.Dataset);

		dcatDataset.setIdentifier(ckanDataset.getName());
		dcatDataset.setTitle(ckanDataset.getTitle());
		dcatDataset.setDescription(ckanDataset.getNotes());

		for (CkanTag ckanTag : Optional.ofNullable(ckanDataset.getTags()).orElse(Collections.emptyList())) {
			String tagName = ckanTag.getName();
			dcatDataset.getKeywords().add(tagName);

			Optional.ofNullable(ckanTag.getVocabularyId()).ifPresent(vocabId -> {
				logger.warn("Tag had a vocabulary id which is not exported " + tagName + " " + vocabId);
			});
		}

		for (CkanPair entry : Optional.ofNullable(ckanDataset.getExtras()).orElse(Collections.emptyList())) {
			String k = entry.getKey();
			String v = entry.getValue();

			tagToRdf(dcatDataset, k, v, pm);
		}

		//
		// for(Entry<String, Object> entry :
		// Optional.ofNullable(ckanDataset.getOthers()).orElse(Collections.emptyMap()).entrySet())
		// {
		// // auto-expand all keys for which a prefix is registered
		// String k = entry.getKey();
		//
		// // TODO Support datatypes
		// String v = "" + entry.getValue();
		//
		// String kIri = tryAsIri(k, pm);
		//
		// if(kIri != null) {
		// Property p = ResourceFactory.createProperty(kIri);
		//
		// String vIri = tryAsIri(v, pm);
		// RDFNode o = vIri != null ? ResourceFactory.createResource(vIri) :
		// ResourceFactory.createPlainLiteral(v);
		//
		// dcatDataset.addProperty(p, o);
		// }
		// }
	}

	public static void othersToRdf(Resource s, Map<String, Object> others, PrefixMapping pm) {
		others = Optional.ofNullable(others).orElse(Collections.emptyMap());

		for (Entry<String, Object> entry : others.entrySet()) {
			String k = entry.getKey();
			Object v = entry.getValue();

			tagToRdf(s, k, v, pm);
		}
	}

	public static void tagToRdf(Resource s, String k, Object v, PrefixMapping pm) {
		// auto-expand all keys for which a prefix is registered

//		logger.info("  Seen tag: " + k + ", " + v);
		
		String vStr = "" + v;
		String kIri = tryAsIri(k, pm);

		if (kIri != null) {
			Property p = ResourceFactory.createProperty(kIri);

			String vIri = tryAsIri(vStr, pm);
			RDFNode o = vIri != null ? ResourceFactory.createResource(vIri) : ResourceFactory.createPlainLiteral(vStr);

//			logger.info("    Obtained RDF: " + kIri + " " + o);
			s.addProperty(p, o);
		}
	}

	public static void convertToDcat(DcatDistribution dcatDistribution, CkanResource ckanResource, PrefixMapping pm) {

		dcatDistribution.addProperty(RDF.type, DCAT.Distribution);

		//dcatDistribution.setName(ckanResource.getName());
		dcatDistribution.setTitle(ckanResource.getName());
		dcatDistribution.setDescription(ckanResource.getDescription());
		dcatDistribution.setFormat(ckanResource.getFormat());

		Optional.ofNullable(ckanResource.getUrl())
				.ifPresent(dcatDistribution::setDownloadURL);

		othersToRdf(dcatDistribution, ckanResource.getOthers(), pm);
	}

	/**
	 * Create a consumer that adds item to the collection returned by the getter
	 * when they are not contained. If the collection does not yet exist, it is
	 * created via ctor and passed to the setter.
	 * 
	 * @param getter
	 * @param setter
	 * @param ctor
	 * @return
	 */
	public static <T, C extends Collection<T>> Consumer<T> uniqueAdder(Supplier<C> getter, Consumer<C> setter,
			Supplier<C> ctor) {
		return (item) -> setter.accept(addIfNotContainsAndCreateIfAbsent(getter.get(), item, ctor));
	}

	/**
	 * Adds items to a collection that are not yet contained and creates the
	 * collection if it does not exist
	 * 
	 * @param collection
	 * @param item
	 * @param ctor
	 * @return
	 */
	public static <T, C extends Collection<T>> C addIfNotContainsAndCreateIfAbsent(C collection, T item,
			Supplier<C> ctor) {
		C result = addIfNotContains(Optional.ofNullable(collection).orElse(ctor.get()), item);
		return result;
	}

	public static <T, C extends Collection<T>> C addIfNotContains(C collection, T item) {
		// Stream.of(item).filter(x ->
		// !collection.contains(item)).forEach(collection::add);
		if (!collection.contains(item)) {
			collection.add(item);
		}
		return collection;
	}

	// TODO Move to ResourceUtils
	public static Optional<String> getUri(Resource r, Property p) {
		Optional<String> result = org.aksw.jena_sparql_api.utils.model.ResourceUtils.tryGetPropertyValue(r, p)
				.filter(RDFNode::isURIResource).map(RDFNode::asResource).map(Resource::getURI);

		return result;
	}

	public static void convertToCkan(CkanDataset ckanDataset, DcatDataset dcatDataset) {

		Optional.ofNullable(dcatDataset.getIdentifier()).ifPresent(ckanDataset::setName);
		Optional.ofNullable(dcatDataset.getTitle()).ifPresent(ckanDataset::setTitle);
		Optional.ofNullable(dcatDataset.getDescription()).ifPresent(ckanDataset::setNotes);

		Consumer<CkanPair> uniqueTagAdder = uniqueAdder(ckanDataset::getExtras, ckanDataset::setExtras, ArrayList::new);
		//
//		getUri(dcatDataset, DcatDeployVirtuosoUtils.dcatDefaultGraphGroup)
//				.ifPresent(v -> uniqueTagAdder.accept(new CkanPair("dcat:defaultGraphGroup", v)));

		DcatDeployVirtuosoUtils.findUri(dcatDataset, DcatDeployVirtuosoUtils.defaultGraphGroupProperties)
			.ifPresent(v -> uniqueTagAdder.accept(new CkanPair("dcat:defaultGraphGroup", v)));

		// TODO Wrap these lookups with an interface
		DcatDeployVirtuosoUtils.findUri(dcatDataset, DcatDeployVirtuosoUtils.defaultGraphProperties)
				.ifPresent(v -> uniqueTagAdder.accept(new CkanPair("dcat:defaultGraph", v)));

//		getUri(dcatDataset, DcatDeployVirtuosoUtils.dcatDefaultGraph)
//		.ifPresent(v -> uniqueTagAdder.accept(new CkanPair("dcat:defaultGraph", v)));

		if (dcatDataset.isURIResource()) {
			uniqueTagAdder.accept(new CkanPair("extra:uri", dcatDataset.getURI()));
		}

		// getUri(dcatDataset, DcatDeployVirtuosoUtils.defaultGraphGroup)
		// .ifPresent(v -> ckanDataset.putOthers("dcat:defaultGraphGroup", v));
		//
		// getUri(dcatDataset, DcatDeployVirtuosoUtils.defaultGraph)
		// .ifPresent(v -> ckanDataset.putOthers("dcat:defaultGraph", v));

	}

	public static void convertToCkan(CkanResource ckanResource, DcatDistribution dcatDistribution) {

		Optional.ofNullable(dcatDistribution.getTitle()).ifPresent(ckanResource::setName);
		// Optional.ofNullable(res.getTitle()).ifPresent(remote::setna);
		Optional.ofNullable(dcatDistribution.getDescription()).ifPresent(ckanResource::setDescription);

		getUri(dcatDistribution, DcatDeployVirtuosoUtils.dcatDefaultGraphGroup)
				.ifPresent(v -> ckanResource.putOthers("dcat:defaultGraphGroup", v));

		getUri(dcatDistribution, DcatDeployVirtuosoUtils.dcatDefaultGraph)
				.ifPresent(v -> ckanResource.putOthers("dcat:defaultGraph", v));

		Optional.ofNullable(dcatDistribution.getFormat())
			.ifPresent(v -> ckanResource.setFormat(v));

		if (dcatDistribution.isURIResource()) {
			ckanResource.putOthers("extra:uri", dcatDistribution.getURI());
		}

	}

}
