package org.aksw.dcat_suite.app.vaadin.view;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.commons.io.util.PathUtils;
import org.aksw.jena_sparql_api.conjure.fluent.JobUtils;
import org.aksw.jena_sparql_api.conjure.job.api.Job;
import org.aksw.jenax.arq.dataset.api.DatasetOneNg;
import org.aksw.jenax.sparql.rx.op.FlowOfDatasetOps;
import org.aksw.jenax.stmt.core.SparqlStmt;
import org.aksw.jenax.stmt.core.SparqlStmtMgr;
import org.aksw.jenax.stmt.resultset.SPARQLResultEx;
import org.aksw.jenax.stmt.util.SparqlStmtUtils;
import org.apache.jena.geosparql.configuration.GeoSPARQLConfig;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.system.Txn;
import org.apache.jena.util.ResourceUtils;

public class FileAnnotatorRdfPerQuad
	// implements FileAnnotator
{	
	
	public static void main(String[] args) throws Exception {
		Path basePath = Path.of("/home/raven/tmp/dman/org/foo/bar");
		Path relPath = Path.of("moin.ttl");
		
		Dataset projectModel = DatasetFactory.create();
		
		annotate(basePath, relPath, projectModel);
		
		RDFDataMgr.write(System.out, projectModel, RDFFormat.TRIG_PRETTY);
	}

	public static DatasetOneNg annotate(Path basePath, Path relPath, Dataset dataset) throws Exception {
		List<SparqlStmt> sparqlStmts = SparqlStmtMgr.loadSparqlStmts("spatial-extent.rq");
		
		return annotateWithSparql(basePath, relPath, dataset, sparqlStmts);
	}
	
	
	// @Override
	public static DatasetOneNg annotateWithSparql(Path basePath, Path relPath, Dataset dataset, List<SparqlStmt> sparqlStmts) throws Exception {
		
		GeoSPARQLConfig.setupNoIndex();
		
		Path fullPath = basePath.resolve(relPath);
		
		String[] segments = PathUtils.getPathSegments(relPath);
		
		// Get a graph for annotating relPath
		DatasetOneNg metadata = Txn.calculateWrite(dataset, () -> {
			return GraphEntityUtils.getOrCreateModel(dataset, relPath.toString(), "#annotator-geo");
		});	
		
		// ConjureBuilderImpl
		
		// Write prov - note: we want prov for the triples - not the file itself!
		if (false) {
			Set<String> optionalArgs = new HashSet<>();
			Map<Var, Expr> bindingMap = new HashMap<>();
			
			Job job = JobUtils.fromSparqlStmts(sparqlStmts, optionalArgs, bindingMap);
			
			Path outFile = basePath.resolve(relPath).resolveSibling(relPath.getFileName() + ".prov.ttl");
			try (OutputStream out = Files.newOutputStream(outFile)) {
				RDFDataMgr.write(out, job.getModel(), RDFFormat.TURTLE_BLOCKS);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		Dataset in = DatasetFactory.create();
		try (InputStream is = Files.newInputStream(fullPath)) {
			RDFDataMgr.read(in, is, Lang.TRIG);
		}
		
		Dataset out = DatasetFactory.create();
		try (RDFConnection conn = RDFConnection.connect(in)) {
			for (SparqlStmt stmt : sparqlStmts) {
				SPARQLResultEx tmp = SparqlStmtUtils.execAny(conn, stmt);
				
				if (tmp.isQuads()) {
					tmp.getQuads().forEachRemaining(out.asDatasetGraph()::add);
				} else if (tmp.isDataset()) {
					out.asDatasetGraph().addAll(tmp.getDataset().asDatasetGraph());
				}
				tmp.close();
			}
		}		
		Txn.executeWrite(dataset, () -> {
			FlowOfDatasetOps.namedGraphs(out).forEach(gds -> {
				ResourceUtils.renameResource(gds.getSelfResource(), metadata.getSelfResource().getURI());
				metadata.getModel().add(gds.getModel());
			});
		});
		
		//
//		try (RdfDataPod dataPod = ExecutionUtils.executeJob(job.getOp())) {
//			Txn.executeWrite(dataset, () -> {
//				FlowOfRdfNodesInDatasetsOps.naturalResources(dataPod.getDataset()).forEach(r -> {
//					metadata.getModel().add(r.getModel());
//					ResourceUtils.renameResource(r, metadata.getSelfResource().getURI());
//				});			
//			});	
//		}
		
		return metadata;
	}
}
