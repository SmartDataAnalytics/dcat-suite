package org.aksw.ckan_deploy.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.aksw.ckan.domain.CkanEntity;
import org.aksw.commons.util.strings.StringUtils;
import org.aksw.dcat.ap.binding.ckan.rdf_view.CkanPseudoNodeFactory;
import org.aksw.dcat.ap.binding.ckan.rdf_view.GraphView;
import org.aksw.dcat.jena.domain.api.DcatDataset;
import org.aksw.dcat.jena.domain.api.DcatDistribution;
import org.aksw.dcat.utils.DcatUtils;
import org.aksw.jena_sparql_api.pseudo_rdf.GraphCopy;
import org.aksw.jena_sparql_api.pseudo_rdf.MappingVocab;
import org.aksw.jena_sparql_api.pseudo_rdf.NodeView;
import org.aksw.jena_sparql_api.transform.result_set.QueryExecutionTransformResult;
import org.apache.jena.graph.Graph;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.util.ResourceUtils;
import org.apache.jena.vocabulary.DCAT;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.XSD;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.trentorise.opendata.jackan.model.CkanDataset;
import eu.trentorise.opendata.jackan.model.CkanPair;
import eu.trentorise.opendata.jackan.model.CkanResource;
import eu.trentorise.opendata.jackan.model.CkanTag;

public class DcatCkanRdfUtils {
    private static final Logger logger = LoggerFactory.getLogger(DcatCkanRdfUtils.class);


    public static Model createModelWithNormalizedDcatFragment(String fileOrUrl) {
        Dataset dataset = RDFDataMgr.loadDataset(fileOrUrl);
        Model result = DcatCkanRdfUtils.createModelWithNormalizedDcatFragment(dataset);
        return result;
    }

    public static Model createModelWithNormalizedDcatFragment(Dataset dataset) {
        Model result = DcatUtils.createModelWithDcatFragment(dataset);
        DcatCkanRdfUtils.normalizeDcatModel(result);
        DcatUtils.addPrefixes(result);
        return result;
    }

    public static void copyPropertyValues(Resource r, Property target, Property source) {
//		org.aksw.jena_sparql_api.utils.model.ResourceUtils
//			.listPropertyValues(r, source)
//			.forEach(v -> r.addProperty(target, v));
        List<RDFNode> values = org.aksw.jena_sparql_api.rdf.collections.ResourceUtils
            .listPropertyValues(r, source).toList();

        values.forEach(v -> r.addProperty(target, v));
    }

    public static void normalizeDcatModel(Model model) {
        for(DcatDataset dcatDataset : DcatUtils.listDcatDatasets(model)) {
            DcatCkanRdfUtils.normalizeDataset(dcatDataset);
        }
    }

    public static void normalizeDataset(DcatDataset dcatDataset) {
        normalizeCommon(dcatDataset);

        for(DcatDistribution dcatDistribution : dcatDataset.getDistributions()) {
            normalizeDistribution(dcatDistribution);
        }

        // Skolemize dcat distributions
        // TODO We should natively support anonymous distributions and
        // thus get rid of the need of the subsequent snippet
        //String datasetNamespace = dcatDataset.getNameSpace();

        if(dcatDataset.isURIResource()) {
            for(DcatDistribution dcatDistribution : new ArrayList<>(dcatDataset.getDistributions())) {
                Resource downloadUrl = org.aksw.jena_sparql_api.rdf.collections.ResourceUtils.listPropertyValues(dcatDistribution, DCAT.downloadURL)
                        .toList().stream()
                        .filter(RDFNode::isURIResource)
                        .map(RDFNode::asResource)
                        .findFirst().orElse(null);

                if(downloadUrl != null && dcatDistribution.isAnon()) {
                    // Allocate an id
                    String id = dcatDataset.getURI() + "/distribution-" + downloadUrl.getLocalName();
                    logger.info("Skolemized a blank node to " + id);
                    ResourceUtils.renameResource(dcatDistribution, id);
                }
            }
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
            String uri = org.aksw.jena_sparql_api.rdf.collections.ResourceUtils.tryGetPropertyValue(dcatEntity, DcatUtils.extraUri)
                .filter(RDFNode::isURIResource)
                .map(RDFNode::asResource)
                .map(Resource::getURI)
                .orElse(null);

            if(uri != null) {
                // remove the property
                org.aksw.jena_sparql_api.rdf.collections.ResourceUtils.setProperty(dcatEntity, DcatUtils.extraUri, null);
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
        CkanEntity ckanEntity = dcatDataset.as(CkanEntity.class);
        String result = Stream.of(ckanEntity.getCkanName(), dcatDataset.getIdentifier(), ckanEntity.getCkanId(), dcatDataset.getTitle())
                .filter(Objects::nonNull)
                .map(id -> baseIri + "dataset/" + StringUtils.urlEncode(id))
                .findFirst()
                .orElseThrow(
                        () -> new RuntimeException("Cannot generate a IRI for a dataset without a local identifier"));

        return result;
    }

    public static String generateFallbackIri(DcatDistribution dcatDistribution, String baseIri) {
        CkanEntity ckanEntity = dcatDistribution.as(CkanEntity.class);

        // CKAN distributions often (always?) don't have an id but only a title
//        String result = Stream.of(dcatDistribution.getIdentifier(), dcatDistribution.getTitle())
        String result = Stream.of(ckanEntity.getCkanName(), dcatDistribution.getIdentifier(), ckanEntity.getCkanId(), dcatDistribution.getTitle())
                .filter(Objects::nonNull)
                .map(id -> baseIri + "distribution/" + StringUtils.urlEncode(id))
                .findFirst()
                .orElseThrow(
                        () -> new RuntimeException("Cannot generate a IRI for a distribution without a local identifier"));

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
//    public static DcatDataset convertToDcat(CkanDataset ckanDataset, PrefixMapping pm) {
//        Model model = ModelFactory.createDefaultModel();
//        DcatDataset result = convertToDcat(model, ckanDataset, pm);
//        return result;
//    }

    public static DcatDataset convertToDcat(Model model, CkanDataset ckanDataset, PrefixMapping pm) {
        // DcatDataset dcatDataset = model.createResource().as(DcatDataset.class);
        DcatDataset dcatDataset = convertToDcat(ckanDataset, pm);

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

    /**
     * Remove triples with literal values that are an empty string or triples
     * with blank nodes that do not have any attributes
     *
     *
     * @param r
     */
    public static void removeTriplesWithImplicitNullValues(Resource s) {
        removeTriplesWithImplicitNullValues(s, new HashSet<>());
    }

    public static void removeTriplesWithImplicitNullValues(Resource s, Set<RDFNode> seen) {
        if (seen.contains(s)) {
            return;
        }
        seen.add(s);

        List<Statement> stmts = s.listProperties().toList();
        for (Statement stmt : stmts) {
            Property p = stmt.getPredicate();
            RDFNode o = stmt.getObject();

            boolean removeStmt = false;

            if (o.isResource()) {
                Resource r = o.asResource();
                removeTriplesWithImplicitNullValues(r, seen);

                // TODO r.getURI() should never be null here but it seeps
                // in through the mapping
                removeStmt =
                          (r.isAnon() && r.listProperties().toList().isEmpty()) ||
                          (r.isURIResource() && (r.getURI() == null || r.getURI().isEmpty()));
            } else if (o.isLiteral()) {
                Literal l = o.asLiteral();
                String dtypeUri = l.getDatatypeURI();

                removeStmt = (XSD.xstring.getURI().equals(dtypeUri) || RDF.langString.getURI().equals(dtypeUri))
                            && l.getString().equals("");
            }

            if (removeStmt) {
                s.getModel().removeAll(s, p, o);
            }
        }
    }


//    public static void getString(RDFNode rdfNode) {
//    	o.isLiteral()) {
//            Literal l = o.asLiteral();
//            String dtypeUri = l.getDatatypeURI();
//    	XSD.xstring.getURI().equals(dtypeUri) || RDF.langString.getURI().equals(dtypeUri))
//    && l.getString().equals("")
//    }

    public static RDFNode rename(RDFNode rdfNode, Property identity) {
        RDFNode result;

        Map<Resource, String> remap = createRenameMap(rdfNode.getModel(), identity);
        Map<Resource, Resource> map = org.aksw.jena_sparql_api.rdf.collections.ResourceUtils.renameResources(remap);

        if (rdfNode.isResource()) {
            Resource r = rdfNode.asResource();
            result = map.getOrDefault(r, r);
        } else {
            result = rdfNode;
        }

        return result;
    }


    public static Map<Resource, String> createRenameMap(Model model, Property identity) {
        List<Statement> stmts = model.listStatements(null, identity, (RDFNode)null).toList();
        Map<Resource, String> result = new HashMap<>();
        for (Statement stmt : stmts) {
            Resource s = stmt.getSubject();
            String iri = stmt.getString();
            result.put(s, iri);
        }

        return result;
    }


    public static DcatDataset convertToDcat(CkanDataset ckanDataset, PrefixMapping pm) {


        NodeView source = CkanPseudoNodeFactory.get().wrap(ckanDataset);
        Graph g = new GraphView();
        Model m = ModelFactory.createModelForGraph(g);

        Resource s = m.wrapAsResource(source);
//        s.listProperties().toList().forEach(System.out::println);
        Model closure = ResourceUtils.reachableClosure(s);

        Resource tmp = closure.wrapAsResource(source);
        Resource newS = QueryExecutionTransformResult.applyNodeTransform(NodeTransformNodeViewToBlankNode.INSTANCE, tmp)
            .asResource();

//        System.out.println("Bnode: " + source.getBlankNode());
//        System.out.println("Size: " + newS.listProperties().toList().size());

        removeTriplesWithImplicitNullValues(newS);
        newS = rename(newS, MappingVocab.iri).asResource();

        newS.getModel().removeAll(null, MappingVocab.iri, null);

//        System.err.println("OUTPUT");
//        RDFDataMgr.write(System.out, newS.getModel(), RDFFormat.TURTLE_BLOCKS);

        DcatDataset dcatDataset = newS.as(DcatDataset.class);


        dcatDataset.addProperty(RDF.type, DCAT.Dataset);
//        dcatDataset.setIdentifier(ckanDataset.getName());
//        dcatDataset.setTitle(ckanDataset.getTitle());
//        dcatDataset.setDescription(ckanDataset.getNotes());

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

        return dcatDataset;
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
        Optional<String> result = org.aksw.jena_sparql_api.rdf.collections.ResourceUtils.tryGetPropertyValue(r, p)
                .filter(RDFNode::isURIResource).map(RDFNode::asResource).map(Resource::getURI);

        return result;
    }

    public static void convertToCkan(CkanDataset ckanDataset, DcatDataset dcatDataset) {

//		DcatApDataset dataset = m.asRDFNode(CkanPseudoNodeFactory.get().createDataset()).as(DcatApDataset.class);
        NodeView target = CkanPseudoNodeFactory.get().wrap(ckanDataset);

        GraphCopy.copy(dcatDataset, target);

        // Unset resources that may have set by graph copy;
        // FIXME Is there a better way to avoid this in the first place?
        ckanDataset.setResources(null);

//        Optional.ofNullable(dcatDataset.getIdentifier()).ifPresent(ckanDataset::setName);
//        Optional.ofNullable(dcatDataset.getTitle()).ifPresent(ckanDataset::setTitle);
//        Optional.ofNullable(dcatDataset.getDescription()).ifPresent(ckanDataset::setNotes);

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
            // extra:uri
            uniqueTagAdder.accept(new CkanPair("uri", dcatDataset.getURI()));
        }

        // getUri(dcatDataset, DcatDeployVirtuosoUtils.defaultGraphGroup)
        // .ifPresent(v -> ckanDataset.putOthers("dcat:defaultGraphGroup", v));
        //
        // getUri(dcatDataset, DcatDeployVirtuosoUtils.defaultGraph)
        // .ifPresent(v -> ckanDataset.putOthers("dcat:defaultGraph", v));

    }

    public static void convertToCkan(CkanResource ckanResource, DcatDistribution dcatDistribution) {

        NodeView target = CkanPseudoNodeFactory.get().wrap(ckanResource);

        GraphCopy.copy(dcatDistribution, target);


        Optional.ofNullable(dcatDistribution.getTitle()).ifPresent(ckanResource::setName);
        // Optional.ofNullable(res.getTitle()).ifPresent(remote::setna);
        // Should be covered by the graph copy
//        Optional.ofNullable(dcatDistribution.getDescription()).ifPresent(ckanResource::setDescription);

        getUri(dcatDistribution, DcatDeployVirtuosoUtils.dcatDefaultGraphGroup)
                .ifPresent(v -> ckanResource.putOthers("dcat:defaultGraphGroup", v));

        getUri(dcatDistribution, DcatDeployVirtuosoUtils.dcatDefaultGraph)
                .ifPresent(v -> ckanResource.putOthers("dcat:defaultGraph", v));

//        Optional.ofNullable(dcatDistribution.getFormat())
//            .ifPresent(v -> ckanResource.setFormat(v));

        if (dcatDistribution.isURIResource()) {
            // resource:uri
            ckanResource.putOthers("uri", dcatDistribution.getURI());
        }
    }

}
