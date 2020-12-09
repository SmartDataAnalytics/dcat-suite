package org.aksw.dcat_suite.clients;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import org.apache.commons.lang.StringUtils;
import org.apache.http.client.utils.URIBuilder;

import org.json.simple.parser.ParseException;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.io.CharStreams;

import eu.trentorise.opendata.jackan.internal.org.apache.http.client.ClientProtocolException;
import eu.trentorise.opendata.jackan.internal.org.apache.http.client.fluent.Request;
import eu.trentorise.opendata.jackan.internal.org.apache.http.client.fluent.Response;
import eu.trentorise.opendata.jackan.model.CkanDataset;
import eu.trentorise.opendata.jackan.model.CkanResponse;
import eu.trentorise.opendata.jackan.model.CkanResource;


public class DkanClient {

	public static final ImmutableList<Integer> SUPPORTED_API_VERSIONS = ImmutableList.of(3); 
	private String portalUrl;
	private String packagesUrl;
	
    public static final String NONE = "None";
    public static final String TIMEZONE_TOKEN = "+";
	public static final String PACKAGES_PATH = "/api/3/action/package_list";
	public static final String PACKAGE_SHOW = "/api/3/action/package_show";
	public static final String ALT_NAME_ATTRIBUTE = "title";
	
	
	@Nullable
	private static ObjectMapper objectMapper;
	
	public DkanClient (String portalUrl) {
		this.portalUrl = portalUrl;
		try {
			this.packagesUrl = concatURI(PACKAGES_PATH);
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public synchronized <T> List<CkanDataset> getDataset(String idOrName) throws URISyntaxException, ClientProtocolException, IOException, ParseException {
			String showURI = concatURI(PACKAGE_SHOW, idOrName);
			List<CkanDataset> dkanDatasets = callPortal(showURI, DatasetsResponse.class).result;
			for (CkanDataset dkanDataset : dkanDatasets) {
					for (CkanResource cr : dkanDataset.getResources()) {
			            cr.setPackageId(dkanDataset.getId());
			            Set<String> othersKeySet = cr.getOthers().keySet();
			            for (String key : othersKeySet) {
			            	if (key.equals(ALT_NAME_ATTRIBUTE)) { 
			            		String altName = cr.getOthers().get(key).toString();
			            		cr.setName(altName);
			            	}
			            }
			        }
			}
			return dkanDatasets;
	 }
	
	public synchronized List<String> getDatasetList() throws ClientProtocolException, IOException, ParseException {
		return callPortal(this.packagesUrl, DatasetListResponse.class).result;
	}
	
	private  <T extends CkanResponse> T callPortal(String requestURI, Class<T> responseType) throws ClientProtocolException, IOException, ParseException {
        T ckanResponse;
        String returnedText;

        try {
            Request request = Request.Get(requestURI);
            Response response = request.execute();
            InputStream stream = response.returnResponse()
                                         .getEntity()
                                         .getContent();

            try (InputStreamReader reader = new InputStreamReader(stream, Charsets.UTF_8)) {
                returnedText = CharStreams.toString(reader);
            }
        } catch (Exception ex) {
            throw new RuntimeException("Error while performing GET. Request url was: " + requestURI);
        }
        try {
            ckanResponse = getObjectMapper().readValue(returnedText, responseType);
        } catch (Exception ex) {
        	throw new DkanException(
                    "Couldn't interpret json returned by the server! Returned text was: " + returnedText,this,  ex);
        	//throw new RuntimeException("Couldn't interpret json returned by the server! Returned text was: " + returnedText);
        }

        if (!ckanResponse.isSuccess()) {
        	throw new RuntimeException("The request did not return any data for: " + requestURI);
        }
        return ckanResponse;
        
	}
	
	private String concatURI(String relPath, String... params) throws URISyntaxException {
		URIBuilder uriBuilder = new URIBuilder(this.portalUrl);
		//uriBuilder.removeQuery();
		if (params.length > 0) {
			uriBuilder.addParameter("id", params[0]);
		}
		return uriBuilder.setPath(relPath)
				.build()		        
				.normalize()
				.toString();
	}
	
	static ObjectMapper getObjectMapper() {
        if (objectMapper == null) {
            objectMapper = new ObjectMapper();
            configureObjectMapper(objectMapper);
        }
        return objectMapper;
	} 
	
    public static Timestamp parseTimestamp(String timestamp) throws java.text.ParseException {
        
    	if (timestamp == null) {
            throw new IllegalArgumentException("Found null timestamp!");
        }

        if (NONE.equals(timestamp)) {
            throw new IllegalArgumentException("Found timestamp with 'None' inside!");
        }
       
        String result = timestamp.contains(TIMEZONE_TOKEN)
        		  ? StringUtils.substringBefore(timestamp, TIMEZONE_TOKEN)
        		  : timestamp;
        return Timestamp.valueOf(result.replace("T", " "));
    }
	
	public static void configureObjectMapper(ObjectMapper om) {
        om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        om.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS);
        om.setSerializationInclusion(Include.NON_NULL);
        om.registerModule(new DkanMapper());
    }
	
}

class DatasetsResponse extends CkanResponse {

	public List<CkanDataset> result;
}

class DatasetListResponse extends CkanResponse {

	public List<String> result;
}