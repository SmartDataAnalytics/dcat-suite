package org.aksw.dcat_suite.app;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

import com.google.gson.JsonObject;



public class DCATProvider {
	//private static final String DCAT_AP_API = "http://localhost:8080/shacl/dcatapde/api/validate";
	private static final String DCAT_AP_API = "https://www.itb.ec.europa.eu/shacl/dcat-ap.de/api/validate";
	private static final String SHACL_PREFIX = "http://www.w3.org/ns/shacl#"; 
	private final CloseableHttpClient httpClient = HttpClients.createDefault();
	
	public List<ResultItem> getTestReport (String fileContent) throws ClientProtocolException, URISyntaxException, IOException {
		Model model = null;
		CloseableHttpResponse response = sendRequest(fileContent);  
		if (response.getStatusLine().getStatusCode() == 200) {
		 HttpEntity entity = response.getEntity();
	        if (entity != null) {
	        	model = ModelFactory.createDefaultModel();
	        	String entityString = new String(EntityUtils.toByteArray(entity), "UTF-8");
	            model.read(new ByteArrayInputStream(entityString.getBytes()),null, "TURTLE");
	        }
		}
		
		List<ResultItem> resultItems = new ArrayList<ResultItem>(); 
		String queryString = "prefix sh:    <http://www.w3.org/ns/shacl#> "
				+ "prefix rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "SELECT ?severity ?message WHERE { "
				+ "?result a sh:ValidationResult; "
				+ "sh:resultSeverity ?severity; "
				+ "sh:resultMessage ?message . }";
		Query query = QueryFactory.create(queryString);
        // Execute the query and obtain results
        QueryExecution qe = QueryExecutionFactory.create(query, model);
        ResultSet results = qe.execSelect();
        while ( results.hasNext() ) {
            final QuerySolution qs = results.next();
            String severity = qs.get("?severity").toString().replace(SHACL_PREFIX, ""); 
            String message = qs.get("?message").toString();
            ResultItem resultItem = new ResultItem(severity, message); 
            resultItems.add(resultItem);
        }
		return resultItems;
	}
	
	
	private CloseableHttpResponse  sendRequest (String fileContent) throws URISyntaxException, ClientProtocolException, IOException {
		String encodedContent = Base64.getEncoder().encodeToString(fileContent.getBytes());
		URIBuilder builder = new URIBuilder(DCAT_AP_API);
		HttpPost postRequest = new HttpPost(builder.build()); 
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("contentToValidate", encodedContent);
		jsonObject.addProperty("validationType", "v11_de_spec");
		jsonObject.addProperty("reportSyntax", "text/turtle");
		jsonObject.addProperty("contentSyntax", "text/turtle");
		postRequest.setHeader("Content-type",String.valueOf(ContentType.APPLICATION_JSON));
		postRequest.setEntity(new StringEntity(jsonObject.toString()));
		return httpClient.execute(postRequest);
	}

}
