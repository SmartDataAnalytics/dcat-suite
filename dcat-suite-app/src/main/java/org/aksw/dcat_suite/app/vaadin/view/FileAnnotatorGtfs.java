package org.aksw.dcat_suite.app.vaadin.view;

import java.io.IOException;
import java.nio.file.Path;

import org.aksw.dcat_suite.enrich.GtfsUtils;
import org.aksw.jena_sparql_api.rdf.collections.ResourceUtils;
import org.apache.jena.geosparql.implementation.GeometryWrapper;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDFS;
import org.onebusaway.gtfs.services.GtfsDao;

public class FileAnnotatorGtfs {


	public static void annotateGtfs(Path absPath, Resource r) throws IOException {
		GtfsDao gtfsDao = GtfsUtils.load(absPath);
		
		GeometryWrapper geom = summarizeSpatialExtent(gtfsDao);
		if (geom != null) {
			ResourceUtils.setProperty(r, RDFS.label, geom.asLiteral());
		}
	}

	public static GeometryWrapper summarizeSpatialExtent(GtfsDao dao) {
      GeometryWrapper result = InvokeUtils.tryCall(() -> {
			GeometryWrapper tmp = GtfsUtils.collectGtfsPoints(dao);
			tmp = tmp.convexHull();
			return tmp;
      }).orElse(null);

      return result;
	}

}
