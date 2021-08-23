package org.aksw.dcat_suite.app;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.github.openjson.JSONObject;

public class QACProvider {
	private final String QAC_SERVICE = "http://srv-dmz-04.ciss.de/QAC_Service/MClientQuACServlet"; 
    private final CloseableHttpClient httpClient = HttpClients.createDefault();
    private String currentJobID; 
	
    public void startJob (String serverPath) throws ClientProtocolException, URISyntaxException, IOException {
    	String jobID = registerJob(); 
    	if (!jobID.isEmpty()) {
    		currentJobID=jobID; 
    		System.out.println(this.currentJobID); 
    		CloseableHttpResponse response = httpClient.execute(postRequest(jobID, serverPath));
    		String json = EntityUtils.toString(response.getEntity());
        	JSONObject obj = new JSONObject(json);
        	String msg = obj.getString("msg");
        	System.out.println(msg); 
    	}
    	
    }
    
    public String getStatus () throws ClientProtocolException, URISyntaxException, IOException {
	    HashMap<String,String> statusMap = new HashMap<String,String>(); 
	    statusMap.put("action","get_status"); 
	    statusMap.put("id",this.currentJobID); 
	    return getRequest(statusMap, "result");
    }
    
    public String getResult () throws ClientProtocolException, URISyntaxException, IOException {
    	HashMap<String,String> resultMap = new HashMap<String,String>(); 
	    resultMap.put("action","get_result_json"); 
	    resultMap.put("id",this.currentJobID); 
	    return getRequest(resultMap, "result");
	}
    
	private String registerJob() throws URISyntaxException, ClientProtocolException, IOException {
	    HashMap<String,String> registerMap = new HashMap<String,String>(); 
	    registerMap.put("action","new_job"); 
	    this.currentJobID = getRequest(registerMap, "id");
        return this.currentJobID;
	}
	
	private String getRequest (HashMap<String,String> params, String returnParam) throws URISyntaxException, ClientProtocolException, IOException {
		String result = ""; 
	    URIBuilder builder = new URIBuilder(QAC_SERVICE); 
	    Iterator<Entry<String, String>> it = params.entrySet().iterator();
	    while (it.hasNext()) {
	        @SuppressWarnings("rawtypes")
			HashMap.Entry pair = (HashMap.Entry)it.next();
	        builder.setParameter((String)pair.getKey(), (String)pair.getValue());
	    }
	    
	    HttpGet request = new HttpGet(builder.build());
	    CloseableHttpResponse response = httpClient.execute(request);
	    if (response.getStatusLine().getStatusCode() == 200) {
        // Get HttpResponse Status
	    	String json = EntityUtils.toString(response.getEntity());
	    	JSONObject obj = new JSONObject(json);
	    	result = obj.getString(returnParam);
	    }
	    
	    return result; 
	}
	
	private HttpPost postRequest (String jobid, String serverPath) throws FileNotFoundException {
		HttpPost uploadFile = new HttpPost(QAC_SERVICE);
		MultipartEntityBuilder builder = MultipartEntityBuilder.create();
		builder.addTextBody("id", jobid, ContentType.TEXT_PLAIN);

		// This attaches the file to the POST:
		File f = new File(serverPath);
		builder.addBinaryBody(
		    "file",
		    new FileInputStream(f),
		    ContentType.APPLICATION_OCTET_STREAM,
		    f.getName()
		);

		HttpEntity multipart = builder.build();
		uploadFile.setEntity(multipart);
		return uploadFile;
	
	}
	
}
