package org.aksw.dcat_suite.enrich;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import org.aksw.jena_sparql_api.sparql.ext.geosparql.GeoSparqlExAggregators;
import org.aksw.jena_sparql_api.sparql.ext.geosparql.GeometryWrapperUtils;
import org.aksw.jenax.arq.util.binding.BindingEnv;
import org.aksw.jenax.arq.util.var.Vars;
import org.apache.jena.geosparql.implementation.GeometryWrapper;
import org.apache.jena.geosparql.implementation.vocabulary.SRS_URI;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.expr.ExprVar;
import org.onebusaway.gtfs.impl.GtfsDaoImpl;
import org.onebusaway.gtfs.serialization.GtfsReader;
import org.onebusaway.gtfs.services.GtfsDao;
import org.onebusaway.gtfs.services.GtfsMutableDao;

public abstract class GtfsUtils {

    public static GtfsMutableDao load(Path path) throws IOException {

        GtfsMutableDao result;
        GtfsReader reader = new GtfsReader();
        try {
            reader.setInputLocation(path.toFile());
            result = new GtfsDaoImpl();
            reader.setEntityStore(result);
            reader.run();
        } finally {
            reader.close();
        }
        return result;
    }

    /**
     * Returns a GeometryWrapper because its easy to go 'up' to the RDF level and 'down' to the JTS level.
     * Use the extract methods of {@link GeometryWrapper} or {@link GeometryWrapperUtils} in order
     * to obtain the plain jts geometry
     */
    public static GeometryWrapper collectGtfsPoints(GtfsDao gtfsDao) {
//		GeoSPARQLConfig.setupNoIndex();
//
//		FlowableTransformer<Binding, Binding> mapper = QueryFlowOps.createMapperBindings(QueryFactory.create(
//				String.join("\n",
//				  "PREFIX geof: <http://www.opengis.net/def/function/geosparql/>\n",
//				  "PREFIX spatialF: <http://jena.apache.org/function/spatial#>\n",
//				  "SELECT (geof:collect(?point) AS ?a) {\n",
//				  "  BIND(spatialF:convertLatLon(?y, ?x) AS ?point)\n",
//				  "}")));
        GeometryWrapper result =
            GeoSparqlExAggregators.aggGeometryWrapperCollection(new ExprVar(Vars.x), false).accumulateAll(
                gtfsDao.getAllStops().stream()
                .map(stop -> BindingFactory.binding(
                        Vars.x,
                        // ConvertLatLon.toLiteral(stop.getLat(), stop.getLon()).asNode()
                        // Note: ConvertLatLon is GeometryWrapper.fromPoint + validation; we skip validation
                        GeometryWrapper.fromPoint(stop.getLat(), stop.getLon(), SRS_URI.WGS84_CRS).asNode()
                        )),
               null)
            // .map(GeometryWrapper::extract)
            .orElse(null);


        return result;
    }

    public static String concatDate(int year, int month, int day) {
        String date = String.valueOf(year)
                .concat("-")
                .concat(String.valueOf(month)
                .concat("-")
                .concat(String.valueOf(day)));
        return date;
    }

    public static String createBaseUri(String prefix, String type, String idString) throws UnsupportedEncodingException {
        String resourceUri = prefix
                .concat(type)
                .concat("/")
                .concat(URLEncoder
                        .encode(idString, StandardCharsets.UTF_8.toString()
                                .toString()));
        return resourceUri;
    }
}
