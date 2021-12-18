package org.aksw.dcat_suite.app;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class SpotlightProvider {
    private final String SPOTLIGHT_URI = "https://api.dbpedia-spotlight.org/de/annotate";
    //private final String SPOTLIGHT_URI = "http://spotlight-de.aksw.org/rest/annotate";
    private final CloseableHttpClient httpClient = HttpClients.createDefault();
    private final String SPOTLIGHT_CONFIDENCE = "0.8";
    private final String DBPEDIA = "DBpedia";

    public Map<String,String> getAnnotations (String text) throws ClientProtocolException, URISyntaxException, IOException {
        HashMap<String,String> requestMap = new HashMap<String,String>();
        requestMap.put("text",text);
        requestMap.put("confidence",SPOTLIGHT_CONFIDENCE);
        return getRequest(requestMap, "Resources");
    }

    private Map<String,String> getRequest (HashMap<String,String> params, String returnParam) throws URISyntaxException, ClientProtocolException, IOException {
        URIBuilder builder = new URIBuilder(SPOTLIGHT_URI);
        Iterator<Entry<String, String>> it = params.entrySet().iterator();
        while (it.hasNext()) {
            @SuppressWarnings("rawtypes")
            HashMap.Entry pair = (HashMap.Entry)it.next();
            builder.setParameter((String)pair.getKey(), (String)pair.getValue());
        }

        HttpGet request = new HttpGet(builder.build());
        request.setHeader("accept",String.valueOf(ContentType.APPLICATION_JSON));
        CloseableHttpResponse response = httpClient.execute(request);
        Map<String,String> returnMap = new HashMap<String,String>();
        if (response.getStatusLine().getStatusCode() == 200) {
            String jsonStr = EntityUtils.toString(response.getEntity());
            Gson gson = new Gson();
            JsonObject jsonObj = gson.fromJson(jsonStr, JsonObject.class);
            JsonArray jsonArr = jsonObj.get(returnParam).getAsJsonArray();
            for (int i = 0; i < jsonArr.size(); i++) {
                 String uri = jsonArr.get(i).getAsJsonObject().get("@URI").getAsString();
                 String classList =  jsonArr.get(i).getAsJsonObject().get("@types").getAsString();
                 String [] uriClasses = classList.split(",");
                 for (String uriClass : uriClasses) {
                     if (uriClass.startsWith(DBPEDIA)) {
                         returnMap.put(uri, uriClass);
                     }
                 }
            }

        }

        return returnMap;
    }

}