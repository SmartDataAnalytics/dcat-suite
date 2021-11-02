package org.aksw.dcat_suite.clients;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.TimeZone;

import javax.annotation.Nullable;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.utils.URIBuilder;
import org.json.simple.parser.ParseException;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.io.CharStreams;

import eu.trentorise.opendata.jackan.internal.org.apache.http.client.fluent.Request;
import eu.trentorise.opendata.jackan.internal.org.apache.http.client.fluent.Response;
import eu.trentorise.opendata.jackan.model.CkanDataset;
import eu.trentorise.opendata.jackan.model.CkanResponse;


public class DkanClient {

    public static final ImmutableList<Integer> SUPPORTED_API_VERSIONS = ImmutableList.of(3);
    private String portalUrl;
    private String packagesUrl;

    public static final String NONE = "None";
    public static final String PACKAGES_PATH = "/api/3/action/package_list";
    public static final String PACKAGE_SHOW = "/api/3/action/package_show";
    public static final String CKAN_NO_MILLISECS_PATTERN = "yyyy-MM-dd'T'HH:mm:ss";

    @Nullable
    private static ObjectMapper objectMapper;

    public DkanClient (String portalUrl) {
        this.portalUrl = portalUrl;
        try {
            this.packagesUrl = concatURI(PACKAGES_PATH);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }


    public synchronized <T> List<CkanDataset> getDataset(String idOrName, Boolean callforAlt) throws URISyntaxException, ClientProtocolException, IOException, ParseException {
            String showURI = concatURI(PACKAGE_SHOW, idOrName);
            List<CkanDataset> dkanDatasets;
            if (callforAlt) {
                String returnedText = call(showURI);
                dkanDatasets = objectMapper.readValue(returnedText, new TypeReference<List<CkanDataset>>(){});
            }
            else {

                dkanDatasets = callPortal(showURI, DatasetsResponse.class).result;

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
                returnedText = call(requestURI);
            }
        } catch (Exception ex) {
            throw new RuntimeException("Error while performing GET. Request url was: " + requestURI);
        }
        try {
            ckanResponse = getObjectMapper().readValue(returnedText, responseType);
        } catch (Exception ex) {
            throw new DkanException(
                    "Couldn't interpret json returned by the server! Returned text was: " + returnedText,this,  ex);
        }

        if (!ckanResponse.isSuccess()) {
            throw new RuntimeException("The request did not return any data for: " + requestURI);
        }
        return ckanResponse;

    }

    private String call(String requestURI) throws ClientProtocolException, IOException  {
        Request request = Request.Get(requestURI);
        Response response = request.execute();
        InputStream stream = response.returnResponse()
                                     .getEntity()
                                     .getContent();

        InputStreamReader reader = new InputStreamReader(stream, Charsets.UTF_8);
        return CharStreams.toString(reader);
    }


    private String concatURI(String relPath, String... params) throws URISyntaxException {
        URIBuilder uriBuilder = new URIBuilder(this.portalUrl);
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
           objectMapper = JsonMapper.builder()
            .addHandler(new DeserializationProblemHandler() {
                @Override
                public Object handleWeirdStringValue(DeserializationContext ctxt, Class<?> targetType, String valueToConvert, String failureMsg) throws IOException {
                    if (targetType == Boolean.class) {
                       return Boolean.TRUE.toString().equalsIgnoreCase(valueToConvert);
                    }
                   return super.handleWeirdStringValue(ctxt, targetType, valueToConvert, failureMsg);
                }
            })
            .build();
            configureObjectMapper(objectMapper);
        }
        return objectMapper;
    }

    /**
      * Method taken from the Jackan CkanClient, see
      * https://github.com/opendatatrentino/jackan/blob/branch-0.4/src/main/java/eu/trentorise/opendata/jackan/CkanClient.java
    */
    public static Timestamp parseTimestamp(String timestamp) throws java.text.ParseException {
        Timestamp ret = null;
        if (timestamp == null) {
            throw new IllegalArgumentException("Found null timestamp!");
        }

        if (NONE.equals(timestamp)) {
            throw new IllegalArgumentException("Found timestamp with 'None' inside!");
        }

        String[] tokens = timestamp.split("\\.");
        String withoutFractional;

        int nanoSecs;

        if (tokens.length == 2) {
            withoutFractional = tokens[0];

            int factor;
            if (tokens[1].length() == 6){
                factor = 1000;
            } else if (tokens[1].length() == 3){
                factor = 1000000;
            } else {
                throw new IllegalArgumentException("Couldn0t parse timestamp:" + timestamp
                                            + "  ! unsupported fractional length: " + tokens[1].length());
            }
            try {
                nanoSecs = Integer.parseInt(tokens[1]) * factor;

            } catch (NumberFormatException ex){
                throw new IllegalArgumentException("Couldn0t parse timestamp:" + timestamp
                        + "  ! invalid fractional part:" + tokens[1]);
            }

        } else if (tokens.length == 1){
            withoutFractional = timestamp;
            nanoSecs = 0;
        } else {
            throw new IllegalArgumentException("Error while parsing timestamp:" + timestamp);
        }

        DateFormat formatter = new SimpleDateFormat(CKAN_NO_MILLISECS_PATTERN);
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        long time = formatter.parse(withoutFractional).getTime();
        ret = new Timestamp(time);
        ret.setNanos(nanoSecs);

        return ret;
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