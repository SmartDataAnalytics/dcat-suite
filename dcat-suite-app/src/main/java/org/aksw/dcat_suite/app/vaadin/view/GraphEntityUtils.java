package org.aksw.dcat_suite.app.vaadin.view;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.aksw.commons.collections.IterableUtils;
import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.rdf.collections.ListFromRDFList;
import org.aksw.jenax.arq.util.syntax.ElementUtils;
import org.aksw.jenax.arq.util.var.Vars;
import org.aksw.jenax.sparql.query.rx.SparqlRx;
import org.aksw.jenax.sparql.relation.api.UnaryRelation;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.graph.NodeTransform;
import org.apache.jena.sparql.graph.NodeTransformLib;
import org.apache.jena.sparql.syntax.ElementNamedGraph;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

public class GraphEntityUtils {
	
	/**
	 * Derive a new dataset with nodes substituted with strings derived from their compositeIds
	 * TODO Not implemented yet
	 * 
	 * @param dataset
	 * @return
	 */
	public static Dataset resolveIds(Dataset dataset) {
		Dataset result = DatasetFactory.create();	
		result.asDatasetGraph().find(Node.ANY, Node.ANY, IdVocab.compositeId.asNode(), Node.ANY);
		return null;
	}

	public static LocalDateTime createTimestamp(ZoneOffset zoneOffset) {
		Instant now = Instant.now();
		return LocalDateTime.ofEpochSecond(now.getEpochSecond(), now.getNano(), zoneOffset);
	}

	public static String createUtcGraphTimestamp() {
		LocalDateTime ldt = createTimestamp(ZoneOffset.UTC);
		return "#" + ldt.toString();
	}

	public static Model getOrCreateModel(Dataset dataset, RDFNode ... rdfNodes) {
		return getOrCreateModel(dataset, Arrays.asList(rdfNodes).stream().map(RDFNode::asNode).collect(Collectors.toList()));
	}

	public static Model getOrCreateModel(Dataset dataset, Node ... nodes) {
		return getOrCreateModel(dataset, Arrays.asList(nodes));
	}
	
	public static Model getOrCreateModel(Dataset dataset, List<Node> nodes) {
		return getOrCreateModel(dataset, nodes, GraphEntityUtils::createUtcGraphTimestamp);
	}
	
	
	public static List<Node> findEntities(Dataset dataset, List<Node> nodes) {
		UnaryRelation ur = createRelationForCompositeId(nodes);
		// Wrap the relation with a GRAPH ?s { ... } element
		ur = new Concept(new ElementNamedGraph(ur.getVar(), ur.getElement()), ur.getVar());			
		Query query = ur.asQuery();
		
		List<Node> result = SparqlRx.execConcept(() -> QueryExecutionFactory.create(query, dataset), ur.getVar()).map(RDFNode::asNode).toList().blockingGet();
		return result;
	}

	public static Node findEntity(Dataset dataset, List<Node> nodes) {
		return IterableUtils.expectZeroOrOneItems(findEntities(dataset, nodes));		
	}
	
	public static Model getOrCreateModel(Dataset dataset, List<Node> nodes, Supplier<String> graphNameFactory) {
		Model[] result = {null};
		
		// Note that the code below could be changed such that the txn gets promoted from read to write as necessary
		// For now we just lock as write
		Node match = findEntity(dataset, nodes);

		if (match != null) {
			result[0] = dataset.getNamedModel(match.getURI());
		} else {
			String newGraphName = graphNameFactory.get();
			result[0] = dataset.getNamedModel(newGraphName);
			ListFromRDFList.create(result[0].createResource(newGraphName), IdVocab.compositeId).asNodes().addAll(nodes);
		}

		return result[0];
	}
	
	public static UnaryRelation createRelationForCompositeId(List<Node> nodes) {
		
		// First create a temporary model with a blank node subject
		// then attach an RDFList to that subject
		// and finally export the triples to a BGP thereby substituting the subject with a variable
		Var v = Vars.s;
		Model m = ModelFactory.createDefaultModel();
		Resource r = m.createResource();
		NodeTransform subst = n -> Objects.equals(n, r.asNode()) ? v : (n.isBlank() ? Var.alloc(n.getBlankNodeLabel()) : n);
		ListFromRDFList.create(r, IdVocab.compositeId).asNodes().addAll(nodes);

		UnaryRelation result = new Concept(
				ElementUtils.createElementTriple(m.getGraph().find().mapWith(t -> NodeTransformLib.transform(subst, t)).toList()), v);

		return result;
	}
	
	
	public static void main(String[] args) {
		List<Node> id1 = Arrays.asList(RDF.Nodes.type, RDFS.Nodes.label);
		List<Node> id2 = Arrays.asList(RDF.Nodes.type, RDFS.Nodes.comment);

		System.out.println(createRelationForCompositeId(id1).asQuery());
	
		Dataset ds = DatasetFactory.create();
		getOrCreateModel(ds, id1).add(RDF.type, RDF.type, RDF.type);

		RDFDataMgr.write(System.out, ds, RDFFormat.TRIG);

		getOrCreateModel(ds, id1).add(RDF.type, RDF.type, RDF.type);


		getOrCreateModel(ds, id2).add(RDFS.comment, RDFS.comment, RDFS.comment);

		RDFDataMgr.write(System.out, ds, RDFFormat.TRIG);
	}
}
