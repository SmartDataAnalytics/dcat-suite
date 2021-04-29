package org.aksw.rdf_view;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.aksw.dcat.ap.binding.ckan.rdf_view.CkanPseudoNodeFactory;
import org.aksw.dcat.ap.binding.ckan.rdf_view.GraphView;
import org.aksw.dcat.ap.binding.jena.domain.impl.DcatApDataset;
import org.aksw.dcat.ap.binding.jena.domain.impl.DcatApDistribution;
import org.aksw.dcat.ap.binding.jena.domain.impl.RdfDcatApAgent;
import org.aksw.dcat.ap.playground.main.RdfDcatApPersonalities;
import org.aksw.jena_sparql_api.pseudo_rdf.GraphCopy;
import org.aksw.jena_sparql_api.pseudo_rdf.NodeView;
import org.apache.jena.enhanced.BuiltinPersonalities;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.sparql.graph.NodeTransform;
import org.apache.jena.sparql.graph.NodeTransformLib;
import org.apache.jena.sparql.vocabulary.FOAF;
import org.apache.jena.sys.JenaSystem;
import org.apache.jena.util.ResourceUtils;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.DCAT;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import eu.trentorise.opendata.jackan.model.CkanDataset;
import eu.trentorise.opendata.jackan.model.CkanResource;
import eu.trentorise.opendata.jackan.model.CkanTag;


class NodeTransformSkolemizePseudoNode
	implements NodeTransform
{
	protected Map<Node, Node> map;
	protected Function<? super Node, ? extends Node> nodeFactory;

	public NodeTransformSkolemizePseudoNode() {
		this(new HashMap<>(), NodeTransformSkolemizePseudoNode::defaultNodeFactory);
	}	

	public NodeTransformSkolemizePseudoNode(Function<? super Node, ? extends Node> nodeFactory) {
		this(new HashMap<>(), nodeFactory);
	}	
	
	public NodeTransformSkolemizePseudoNode(Map<Node, Node> map, Function<? super Node, ? extends Node> nodeFactory) {
		super();
		this.map = map;
		this.nodeFactory = nodeFactory;
	}

	public static Node defaultNodeFactory(Node node) {
		Node result = node instanceof NodeView ? NodeFactory.createBlankNode() : node;
		return result;
	}
	
	@Override
	public Node apply(Node t) {
		Node result = map.computeIfAbsent(t, nodeFactory);
		return result;
	}
}

public class PseudoRdfConcept {
	
	private static final Logger logger = LoggerFactory.getLogger(PseudoRdfConcept.class);

	public static Model transform(NodeTransform nodeTransform, Model model) {
		Graph graph = model.getGraph();
		transform(nodeTransform, graph);
		return model;
	}

	public static Graph transform(NodeTransform nodeTransform, Graph graph) {
		Set<Triple> tmp = new HashSet<>();
		ExtendedIterator<Triple> it = graph.find();
		try {
			while(it.hasNext()) {
				Triple t = it.next();
				Triple u = NodeTransformLib.transform(nodeTransform, t);
				if(!Objects.equals(t, u)) {
					it.remove();
					tmp.add(u);
				}
			}
		} finally {
			it.close();
		}

		tmp.forEach(graph::add);
		
		return graph;
	}
	
//	@Test
//	public void jena() {
//		Var x = Var.alloc("x");
//		Accumulator acc = new AggMax(new ExprVar(x)).createAccumulator();
//		acc.accumulate(BindingFactory.binding(x, NodeFactory.createURI("http://foo")), null);
//		acc.accumulate(BindingFactory.binding(x, NodeValue.makeInteger(1).asNode()), null);
//		acc.accumulate(BindingFactory.binding(x, NodeValue.makeInteger(8).asNode()), null);
//		acc.accumulate(BindingFactory.binding(x, NodeValue.makeInteger(10).asNode()), null);
//		acc.accumulate(BindingFactory.binding(x, NodeValue.makeInteger(5).asNode()), null);
//		
//		NodeValue nv = acc.getValue();
//		
//		System.out.println(nv.getNode().getLiteralDatatypeURI());
//		System.out.println("Matches: " + nv.getNode().matches(XSD.xint.asNode()));
//		System.out.println("Matches: " + nv.getNode().matches(XSD.integer.asNode()));
//		
//		System.out.println(nv);
//	}
//
//	//@Test
//	public void jenaTime() {
//	QueryExecutionFactory.sparqlService("http://localhost:5820/qrowd/query", "SELECT * { ?s ?p ?o } LIMIT 10").execSelect().forEachRemaining(System.out::println);
//		Var x = Var.alloc("x");
//		Accumulator acc = new AggAvg(new ExprVar(x)).createAccumulator();
//		acc.accumulate(BindingFactory.binding(x, ExprUtils.eval(new E_Now()).asNode()), null);
//		acc.accumulate(BindingFactory.binding(x, ExprUtils.eval(new E_Now()).asNode()), null);
//		
//		NodeValue nv = acc.getValue();
//		System.out.println(nv);
//	}
	
	@Test
	public void testRdfDcatView() {
		
		JenaSystem.init();
		RdfDcatApPersonalities.init(BuiltinPersonalities.model);
		/*
		 * playground
		 */
		
		Graph g = new GraphView();
		Model m = ModelFactory.createModelForGraph(g);
		
		

		CkanDataset ckanDataset = new CkanDataset();
		CkanResource ckanResource = new CkanResource();
		

		ckanResource.setDescription("test description");
		
		ckanDataset.setResources(new ArrayList<>(Arrays.asList(ckanResource)));
		
		ckanDataset.setTitle("test");
		
		
		
		Model rdfModel = ModelFactory.createDefaultModel();
		DcatApDataset rdfDataset = rdfModel.createResource("http://my.data/set").as(DcatApDataset.class);
		DcatApDistribution rdfDistribution = rdfModel.createResource("http://my.dist/ribution").as(DcatApDistribution.class);
		RdfDcatApAgent rdfPublisher = rdfModel.createResource("http://my.agent").as(RdfDcatApAgent.class);
		
		rdfDataset.setTitle("My dataset");
		rdfDataset.addLiteral(DCTerms.description, "The slave plan");
		Assert.assertEquals("The slave plan", rdfDataset.getDescription());
		
//		System.out.println(rdfDataset.getDescription());
		
		rdfDataset.setDescription("The master plan");
		Assert.assertEquals("The master plan", rdfDataset.getDescription());

		
		rdfDataset.getDistributions(DcatApDistribution.class).add(rdfDistribution);
		rdfDataset.setPublisher(rdfPublisher);

		rdfPublisher.setName("Some Publisher");
		rdfDistribution.setTitle("Some Distribtion");
		
		rdfDataset.getPublisher().setMbox("mailto:foo@bar.baz");
		// Abstract the bean as something that has properties - so it could also be plain json
		//PropertySource s = new PropertySourceCkanDataset(ckanDataset);

		
		DcatApDataset dataset = m.asRDFNode(CkanPseudoNodeFactory.get().createDataset()).as(DcatApDataset.class);
		System.out.println("TITLE: " + dataset.getTitle());
		dataset.setDescription("Tunnelsystem");
		System.out.println("TITLE: " + dataset.getTitle());

		System.out.println(((CkanDataset)((NodeView)dataset.asNode()).getSource().getSource()).getTitle());
		System.out.println(((CkanDataset)((NodeView)dataset.asNode()).getSource().getSource()).getNotes());
		
		//RDFDataMgr.write(System.out, dataset, lang);
		
		Resource distribution = m.asRDFNode(CkanPseudoNodeFactory.get().createDistribution()).asResource();
		
		dataset.addProperty(DCAT.distribution, distribution);
		//distribution.addProperty(DCTerms.description, "Test distri");
		
		DcatApDistribution view = distribution.as(DcatApDistribution.class);
		view.setDescription("Download of the master plan");
		
		Resource publisher = dataset.getProperty(DCTerms.publisher)
			.getObject().asResource();//.changeObject("Test");

		publisher.addProperty(FOAF.name, "Test");
//		dataset.getProperty(DCTerms.publisher)
//			.changlit
		//.addLiteral(FOAF.name, "Test");

		dataset.getThemes().add("http://foo.bar/theme/baz");
		dataset.getThemes().add("http://moo.boo/coo");
		
		System.out.println("Themes: " + dataset.getThemes());
		dataset.getThemes().remove("http://foo.bar/theme/baz");
		System.out.println("Themes: " + dataset.getThemes());
		
		dataset.listProperties().forEachRemaining(stmt -> {
			System.out.println("Dataset property: " + stmt);
			
		});
		
		dataset.listProperties(DCTerms.publisher).forEachRemaining(stmt -> {
			System.out.println("Publisher: " + stmt);
			

			stmt.getObject().asResource().listProperties().forEachRemaining(stmt2 -> {
				System.out.println("  Attr: " + stmt2);
			});
		});
		
		
		dataset.listProperties(DCAT.distribution).forEachRemaining(stmt -> {
			System.out.println("Distribution: " + stmt);
			
			Resource dist = stmt.getObject().asResource();
			
			Statement distDescription = dist.getProperty(DCTerms.description);
			System.out.println("  Description: " + distDescription);
		});
		

		// Load a dcat description of a dataset into a model
		Model inputModel = RDFDataMgr.loadModel("dcat-ap-test01.ttl");
		
		// Get a the dataset resource
		Resource rootA = inputModel.listSubjectsWithProperty(RDF.type, DCAT.Dataset).next();
		
		// Create a Jena Node that is actually backed by a CKAN domain object
		NodeView rootB = CkanPseudoNodeFactory.get().createDataset();
		DcatApDataset rB = m.asRDFNode(rootB).asResource().as(DcatApDataset.class);
		
		CkanDataset yyy = (CkanDataset)((NodeView)rootB).getSource().getSource();

		// Copy the properties of the input dataset to the CKAN domain object (via the RDF abstraction)
		GraphCopy.copy(rootA, rootB);
		
		System.out.println("Got tags from underyling CKAN entity: " + yyy.getTags().stream().map(CkanTag::getName).collect(Collectors.toList()));		
		yyy.getTags().add(new CkanTag("FOOOOOOO", null));
		
		System.out.println("Got tags via RDF API: " +
				org.aksw.jena_sparql_api.rdf.collections.ResourceUtils.listPropertyValues(rB, DCAT.keyword).toList());

		// Remove via java API
		Iterator<?> it = rB.getKeywords().iterator(); it.next(); it.remove();
		System.out.println("Got tags from underyling CKAN entity: " + yyy.getTags().stream().map(CkanTag::getName).collect(Collectors.toList()));		

		// Remove first tag via RDF level
		org.aksw.jena_sparql_api.rdf.collections.ResourceUtils.listPropertyValues(rB, DCAT.keyword).removeNext();
		System.out.println("Got tags from underyling CKAN entity: " + yyy.getTags().stream().map(CkanTag::getName).collect(Collectors.toList()));		

		rB.getKeywords().add("test");
		System.out.println("Got tags from underyling CKAN entity: " + yyy.getTags().stream().map(CkanTag::getName).collect(Collectors.toList()));		

		
		
		System.out.println("Input model size: " + inputModel.size());
		RDFDataMgr.write(System.out, inputModel, RDFFormat.TURTLE_PRETTY);

		// Expose the CKAN domain object as RDF
		Model outputModel = ResourceUtils.reachableClosure(m.asRDFNode(rootB).asResource());

		// Add prefixes for nicer output
		outputModel.setNsPrefixes(inputModel.getNsPrefixMap());

		System.out.println("output model size: " + outputModel.size());
		RDFDataMgr.write(System.out, outputModel, RDFFormat.TURTLE_PRETTY);
		
//		ModelDiff diff = ModelDiff.create(inputModel, outputModel);
//		System.out.println("Diff: " + diff);
		System.out.println("Done.");
		
		System.out.println("JSON Output:");
		CkanDataset xxx = (CkanDataset)rootB.getSource().getSource();
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		String json = gson.toJson(xxx);
		
		System.out.println(json);
		
	}

	
	
//	Collection<? extends PseudoRdfNode> distributions = dataset.getPropertyValues(DCAT.distribution.getURI());
//	
//	System.out.println("Distributions: " + distributions);
	
	
//	RdfNode newDist = new PseudoRdfResourceImpl(
//			new PropertySourceCkanDataset(ckanDataset), ckanDatasetAccessors);
//	
//	dataset.addProperty(DCAT.distribution, newDist);
	

//	Node rootA = NodeFactory.createBlankNode();
//	Graph mappingGraph = new MappingGraph(new PseudoGraph(), rootA, rootB);
//	
//	Model mm = ModelFactory.createModelForGraph(mappingGraph);
//	
//	Model inputModel = RDFDataMgr.loadModel("dcat-ap-test01.ttl");
//	Resource r = inputModel.listSubjectsWithProperty(RDF.type, DCAT.Dataset).next();
//	
//	Resource s = mm.asRDFNode(rootA).asResource();
	
	
//	dataset.getProperty(DCA)
	
	/*
	SingleValuedAccessor<Collection<CkanResource>> test = s.getCollectionProperty("resources", CkanResource.class);
	test.get().iterator().next().setDescription("Test description");
	
	
	System.out.println("Collection test: " + test.get());
	
	PseudoRdfProperty node = datasetAccessor.get(DCTerms.title.getURI()).apply(s);
	
	System.out.println(node.getValues());
	
	node.getValues().clear();
	System.out.println(node.getValues());
	
	System.out.println("title: " + ckanDataset.getTitle());
	*/

	// The idea of the mapping model is, that given a complete dcat graph,
	// attempt to instantiate the dcat model backed by the ckan model
	// Note, that in general *all* information must be available, as rdfTypes
	// may take properties of nodes as ctor arguments
//
//	Model mappingModel = RDFDataMgr.loadModel("dcat-ap-ckan-mapping.ttl");
//	
//	
//	List<Resource> mappings = mappingModel.listObjectsOfProperty(MappingVocab.mapping)
//			.filterKeep(RDFNode::isResource).mapWith(RDFNode::asResource).toList();
//
//	//Map<String, MappingProcessor> mappingProcessorRegistry = new HashMap<>();
//	
//	Map<RDFNode, Map<String, Function<PropertySource, PseudoRdfProperty>>> targetToAccessors = new HashMap<>();
//	targetToAccessors.put(DCAT.Dataset, CkanPseudoNodeFactory.get().ckanDatasetAccessors);
//	targetToAccessors.put(DCAT.Distribution, CkanPseudoNodeFactory.get().ckanResourceAccessors);
//	targetToAccessors.put(FOAF.Agent, CkanPseudoNodeFactory.get().ckanDatasetPublisherAccessors);
//	
//	
//	for(Resource mapping : mappings) {
//		MappingUtils.applyMappingDefaults(mapping);
//		
//		//RDFDataMgr.write(System.out, mapping.getModel(), RDFFormat.TURTLE_PRETTY);
//	
//		String mappingType = mapping.getPropertyResourceValue(RDF.type).getURI();
//		//type.getURI();
//		
//		// Get the mapping processor for the type
//		//MappingProcessor mappingProcessor = mappingProcessorRegistry.get(type);
//
//		RDFNode target = mapping.getProperty(MappingVocab.target).getObject();
//		// Resolve the target to the mapping registry
//		Map<String, Function<PropertySource, PseudoRdfProperty>> mappingRegistry = targetToAccessors.computeIfAbsent(target, (k) -> new HashMap<>());
//		
//		
//		TypeMapper typeMapper = TypeMapper.getInstance();
//		if(mappingType.equals(MappingVocab.LiteralMapping.getURI())) {
//
//			Resource dtype = mapping.getProperty(MappingVocab.type).getObject().asResource();				
//			String predicate = mapping.getProperty(MappingVocab.predicate).getObject().asResource().getURI();
//			String key = mapping.getProperty(MappingVocab.key).getString();
//			
//			//typeMapper.getTypeByName(dtype.getURI());
//			
//			System.out.println("Adding " + predicate + " -> " + key);
//			CkanPseudoNodeFactory.addLiteralMapping(mappingRegistry, predicate, key, typeMapper, dtype.getURI());
//			//if(type.get)
//			
//		} else if(mappingType.equals(MappingVocab.CollectionMapping.getURI())) {
//			Resource dtype = mapping.getProperty(MappingVocab.type).getObject().asResource();				
//			String predicate = mapping.getProperty(MappingVocab.predicate).getObject().asResource().getURI();
//			String key = mapping.getProperty(MappingVocab.key).getString();
//
//			CkanPseudoNodeFactory.addCollectionMapping(mappingRegistry, predicate, key, typeMapper, dtype.getURI());
//			
//		} else if(mappingType.equals(MappingVocab.JsonArrayMapping.getURI())) {
//			Resource dtype = mapping.getProperty(MappingVocab.type).getObject().asResource();				
//			String predicate = mapping.getProperty(MappingVocab.predicate).getObject().asResource().getURI();
//			String key = mapping.getProperty(MappingVocab.key).getString();
//			
//			CkanPseudoNodeFactory.addExtraJsonArrayMapping(mappingRegistry, predicate, key, typeMapper, dtype.getURI());
//		} else {
//			logger.warn("Unknown mapping type: " + mappingType);
//		}
//		
//		//mappingProcessor.apply(mapping, mappingRegistry);
//		
//		
//	}
//	
//	
	

}
