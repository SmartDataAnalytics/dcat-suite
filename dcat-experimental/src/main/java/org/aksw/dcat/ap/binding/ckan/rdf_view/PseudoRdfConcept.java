package org.aksw.dcat.ap.binding.ckan.rdf_view;

import java.util.ArrayList;
import java.util.Arrays;

import org.aksw.dcat.ap.binding.jena.domain.impl.DcatApDataset;
import org.aksw.dcat.ap.binding.jena.domain.impl.DcatApDistribution;
import org.aksw.dcat.ap.binding.jena.domain.impl.RdfDcatApAgent;
import org.aksw.dcat.ap.binding.jena.domain.impl.RdfDcatApDatasetImpl;
import org.aksw.dcat.ap.playground.main.RdfDcatApPersonalities;
import org.aksw.jena_sparql_api.pseudo_rdf.GraphCopy;
import org.aksw.jena_sparql_api.pseudo_rdf.PseudoGraph;
import org.aksw.jena_sparql_api.pseudo_rdf.PseudoNode;
import org.apache.jena.enhanced.BuiltinPersonalities;
import org.apache.jena.graph.Graph;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.sparql.vocabulary.FOAF;
import org.apache.jena.system.JenaSystem;
import org.apache.jena.vocabulary.DCAT;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import eu.trentorise.opendata.jackan.model.CkanDataset;
import eu.trentorise.opendata.jackan.model.CkanResource;



public class PseudoRdfConcept {
	
	private static final Logger logger = LoggerFactory.getLogger(PseudoRdfConcept.class);


	public static void main2(String[] args) {

		JenaSystem.init();
		RdfDcatApPersonalities.init(BuiltinPersonalities.model);

		Resource r = RDFDataMgr.loadModel("dcat-ap-test01.ttl").createResource("http://www.example.org/LinkedGeoData");
		
		DcatApDataset ds = r.as(DcatApDataset.class);
		System.out.println(ds.getPublisher().getName());
		ds.getPublisher().setName("Test");
		System.out.println(ds.getPublisher().getName());

		RDFDataMgr.write(System.out, r.getModel(), RDFFormat.TURTLE_PRETTY);
	}
	
	public static void main(String[] args) {

		JenaSystem.init();
		RdfDcatApPersonalities.init(BuiltinPersonalities.model);
		/*
		 * playground
		 */
		
		Graph g = new PseudoGraph();
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
		rdfDataset.setDescription("The master plan");

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

		System.out.println(((CkanDataset)((PseudoNode)dataset.asNode()).getSource().getSource()).getTitle());
		System.out.println(((CkanDataset)((PseudoNode)dataset.asNode()).getSource().getSource()).getNotes());
		
		//RDFDataMgr.write(System.out, dataset, lang);
		
		Resource distribution = m.asRDFNode(CkanPseudoNodeFactory.get().createDistribution()).asResource();
		
		dataset.addProperty(DCAT.distribution, distribution);
		//distribution.addProperty(DCTerms.description, "Test distri");
		
		DcatApDistribution view = distribution.as(DcatApDistribution.class);
		view.setDescription("Download of the master plan");
		
		
//		PseudoRdfResourceImpl dataset = new PseudoRdfResourceImpl(
//				new PropertySourceCkanDataset(ckanDataset), ckanDatasetAccessors);

		
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
		
		
		
//		Collection<? extends PseudoRdfNode> distributions = dataset.getPropertyValues(DCAT.distribution.getURI());
//		
//		System.out.println("Distributions: " + distributions);
		
		
//		RdfNode newDist = new PseudoRdfResourceImpl(
//				new PropertySourceCkanDataset(ckanDataset), ckanDatasetAccessors);
//		
//		dataset.addProperty(DCAT.distribution, newDist);
		
		dataset.listProperties(DCAT.distribution).forEachRemaining(stmt -> {
			System.out.println("Distribution: " + stmt);
			
			Resource dist = stmt.getObject().asResource();
			
			Statement distDescription = dist.getProperty(DCTerms.description);
			System.out.println("  Description: " + distDescription);
		});
		
		
		// The idea of the mapping model is, that given a complete dcat graph,
		// attempt to instantiate the dcat model backed by the ckan model
		// Note, that in general *all* information must be available, as rdfTypes
		// may take properties of nodes as ctor arguments
//
//		Model mappingModel = RDFDataMgr.loadModel("dcat-ap-ckan-mapping.ttl");
//		
//		
//		List<Resource> mappings = mappingModel.listObjectsOfProperty(MappingVocab.mapping)
//				.filterKeep(RDFNode::isResource).mapWith(RDFNode::asResource).toList();
//
//		//Map<String, MappingProcessor> mappingProcessorRegistry = new HashMap<>();
//		
//		Map<RDFNode, Map<String, Function<PropertySource, PseudoRdfProperty>>> targetToAccessors = new HashMap<>();
//		targetToAccessors.put(DCAT.Dataset, CkanPseudoNodeFactory.get().ckanDatasetAccessors);
//		targetToAccessors.put(DCAT.Distribution, CkanPseudoNodeFactory.get().ckanResourceAccessors);
//		targetToAccessors.put(FOAF.Agent, CkanPseudoNodeFactory.get().ckanDatasetPublisherAccessors);
//		
//		
//		for(Resource mapping : mappings) {
//			MappingUtils.applyMappingDefaults(mapping);
//			
//			//RDFDataMgr.write(System.out, mapping.getModel(), RDFFormat.TURTLE_PRETTY);
//		
//			String mappingType = mapping.getPropertyResourceValue(RDF.type).getURI();
//			//type.getURI();
//			
//			// Get the mapping processor for the type
//			//MappingProcessor mappingProcessor = mappingProcessorRegistry.get(type);
//
//			RDFNode target = mapping.getProperty(MappingVocab.target).getObject();
//			// Resolve the target to the mapping registry
//			Map<String, Function<PropertySource, PseudoRdfProperty>> mappingRegistry = targetToAccessors.computeIfAbsent(target, (k) -> new HashMap<>());
//			
//			
//			TypeMapper typeMapper = TypeMapper.getInstance();
//			if(mappingType.equals(MappingVocab.LiteralMapping.getURI())) {
//
//				Resource dtype = mapping.getProperty(MappingVocab.type).getObject().asResource();				
//				String predicate = mapping.getProperty(MappingVocab.predicate).getObject().asResource().getURI();
//				String key = mapping.getProperty(MappingVocab.key).getString();
//				
//				//typeMapper.getTypeByName(dtype.getURI());
//				
//				System.out.println("Adding " + predicate + " -> " + key);
//				CkanPseudoNodeFactory.addLiteralMapping(mappingRegistry, predicate, key, typeMapper, dtype.getURI());
//				//if(type.get)
//				
//			} else if(mappingType.equals(MappingVocab.CollectionMapping.getURI())) {
//				Resource dtype = mapping.getProperty(MappingVocab.type).getObject().asResource();				
//				String predicate = mapping.getProperty(MappingVocab.predicate).getObject().asResource().getURI();
//				String key = mapping.getProperty(MappingVocab.key).getString();
//
//				CkanPseudoNodeFactory.addCollectionMapping(mappingRegistry, predicate, key, typeMapper, dtype.getURI());
//				
//			} else if(mappingType.equals(MappingVocab.JsonArrayMapping.getURI())) {
//				Resource dtype = mapping.getProperty(MappingVocab.type).getObject().asResource();				
//				String predicate = mapping.getProperty(MappingVocab.predicate).getObject().asResource().getURI();
//				String key = mapping.getProperty(MappingVocab.key).getString();
//				
//				CkanPseudoNodeFactory.addExtraJsonArrayMapping(mappingRegistry, predicate, key, typeMapper, dtype.getURI());
//			} else {
//				logger.warn("Unknown mapping type: " + mappingType);
//			}
//			
//			//mappingProcessor.apply(mapping, mappingRegistry);
//			
//			
//		}
//		
//		
		
		
		Model inputModel = RDFDataMgr.loadModel("dcat-ap-test01.ttl");
		Resource rootA = inputModel.listSubjectsWithProperty(RDF.type, DCAT.Dataset).next();
		PseudoNode rootB = CkanPseudoNodeFactory.get().createDataset();

		GraphCopy.copy(rootA, rootB);
		
		
		CkanDataset xxx = (CkanDataset)rootB.getSource().getSource();
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		String json = gson.toJson(xxx);
		
		System.out.println(json);
//		Node rootA = NodeFactory.createBlankNode();
//		Graph mappingGraph = new MappingGraph(new PseudoGraph(), rootA, rootB);
//		
//		Model mm = ModelFactory.createModelForGraph(mappingGraph);
//		
//		Model inputModel = RDFDataMgr.loadModel("dcat-ap-test01.ttl");
//		Resource r = inputModel.listSubjectsWithProperty(RDF.type, DCAT.Dataset).next();
//		
//		Resource s = mm.asRDFNode(rootA).asResource();
		
		
//		dataset.getProperty(DCA)
		
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
	}

}
