package org.aksw.dcat_suite.cli.main;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Consumer;

import org.aksw.dcat.jena.domain.api.DcatDataset;
import org.aksw.dcat.jena.domain.api.DcatDistribution;
import org.aksw.dcat.utils.DcatUtils;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.Op;
import org.aksw.jena_sparql_api.conjure.fluent.ConjureBuilderImpl;
import org.aksw.jena_sparql_api.conjure.fluent.JobUtils;
import org.aksw.jena_sparql_api.conjure.job.api.Job;
import org.aksw.jena_sparql_api.conjure.job.api.JobInstance;
import org.aksw.jena_sparql_api.conjure.resourcespec.RPIF;
import org.aksw.jena_sparql_api.rdf.collections.ResourceUtils;
import org.apache.jena.ext.com.google.common.collect.Iterators;
import org.apache.jena.ext.com.google.common.collect.Maps;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.DCAT;

import com.github.jsonldjava.shaded.com.google.common.collect.Iterables;



public class DcatOps {
	
	public static Entry<String, String> parseEntry(String str) {
		String[] kv = str.split("=", 2);
		String k = kv[0];
		String v = kv[1];
		Entry<String, String> result = Maps.immutableEntry(k, v);

		return result;
	}
	
	public static void transformAllDists(Model model, Consumer<Resource> consumer) {
		Collection<DcatDataset> dcatDatasets = DcatUtils.listDcatDatasets(model);
		
		for(DcatDataset ds : dcatDatasets) {
			transformAllDists(ds, consumer);
		}
	}
	
	public static void transformAllDists(DcatDataset ds, Consumer<Resource> consumer) {
		
		for(DcatDistribution dist : ds.getDistributions()) {
			consumer.accept(dist);
		}
	}
	
	/**
	 * Convenience method to apply a transformation by means of a sequence of SPARQL queries
	 * to a distribution.
	 * 
	 * 
	 * @param distribution
	 * @param sparqlFile
	 * @param targetFolder
	 * @throws Exception 
	 */
	public static Consumer<Resource> createDistTransformer(
//			Resource distribution,
			List<String> sparqlFiles,
			Map<String, Node> env,
			Path targetFolder) throws Exception {
		
		// TODO Extend to multiple files
		String sparqlFile = Iterables.getOnlyElement(sparqlFiles);
		
		Job job = JobUtils.fromSparqlFile(sparqlFile);

		Consumer<Resource> result = distribution -> {
			
			// Check whether this distribution qualifies for transform:
			// (.) it must either have a downloadURL, or
			// (.) it must have a rpif:op operation attached 
			String downloadURL = org.aksw.dcat.ap.utils.DcatUtils.getFirstDownloadUrlFromDistribution(distribution);
			
			// If there is a downloadURL, create a DataRefUrl from it
			// otherwise, if there is an op, inject this one instead
			
			Op op;
			if(downloadURL != null) {
				op = ConjureBuilderImpl.start().fromUrl(downloadURL).getOp();
			} else {
				op = ResourceUtils.getLiteralPropertyValue(distribution, RPIF.op, Op.class);
			}
			
			if(op != null) {
				//DataRef dataRef = DataRefUrl.create(ModelFactory.createDefaultModel(), "http://foo.bar/baz");
	//			JenaSystem.init();
	
				Set<String> opVars = job.getOpVars();
				String opVar = Iterables.getOnlyElement(opVars);
				
				Map<String, Op> map = Collections.singletonMap(opVar, op);
				JobInstance ji = JobUtils.createJobInstance(job, env, map);
				Op inst = JobUtils.materializeJobInstance(ji);

				distribution.getModel().add(inst.getModel());

				Iterators.removeIf(
						ResourceUtils.listPropertyValues(distribution, DCAT.downloadURL),
						x -> true);
				Iterators.removeIf(
						ResourceUtils.listPropertyValues(distribution, RPIF.op),
						x -> true);
					
				distribution.addProperty(RPIF.op, inst);
	
			}
			
		};
		
		return result;
		// This is the execution part that should be separate

//		Map<String, Node> env = ImmutableMap.<String, Node>builder()
//				.put("SOURCE_NS", NodeFactory.createLiteral("https://portal.limbo-project.org"))
//				.put("TARGET_NS", NodeFactory.createLiteral("https://data.limbo-project.org"))
//				.build();
		
//		String downloadUrl = org.aksw.dcat.ap.utils.DcatUtils.getFirstDownloadUrlFromDistribution(distribution);
//
//
//		try(RdfDataPod pod = ExecutionUtils.executeJob(inst)) {
//			Model m = pod.getModel();
//			System.out.println("Transformed Model:");
//			RDFDataMgr.write(System.out, m, RDFFormat.TURTLE_PRETTY);
//		}

		// TODO Where to write the data?
		
		// Convert the given sparql query to a conjure job
		
		// If the distribution is already a conjure distribution, extend upon it
		// Otherwise, wrap it as a one
		// JenaPluginUtils.copyClosureInto(rdfNode, viewClass, target)
		
	}
	
	/**
	 * Copy all referenced files into a target folder
	 * Performs in-place update of download links
	 * 
	 * @param dataset
	 * @throws IOException 
	 */
	public static void createLocalCopyDataset(DcatDataset dataset, Path targetFolder) throws IOException {
		Files.createDirectories(targetFolder);
		
		Collection<DcatDistribution> dists = dataset.getDistributions(DcatDistribution.class);
		for(DcatDistribution dist : dists) {
			for(String url : dist.getDownloadURLs()) {
				// TODO Reuse util function to resolve path to file
				// If we have a remote url, download it and get the filename
				// Use a flag for caching the downloads -
				
				Path src = null;
				Path tgt = null;
				Files.copy(src, tgt);
			}
		}
	}
	
	public static void createLocalCopy(DcatDistribution distribution, Path targetFolder) throws IOException {
		//return null;
	}

	
	// Apply encoding - actually this is just passing files / byte streams
	// to some function
	// ByteStreamProcessorFactory.accepts({hot-file, regular-file, input-stream})
	//   (actually a hot-file is more like an input stream however with an associated path)
	public static void sortNtriples() {
		
	}
	
	
	/**
	 * pass each distribution to the provided command / script
	 * 
	 */
	public static void applyTransform() {
		
	}
}
