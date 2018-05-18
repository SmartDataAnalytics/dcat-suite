package org.aksw.ckan_deploy.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.trentorise.opendata.jackan.CkanClient;
import eu.trentorise.opendata.jackan.exceptions.CkanException;
import eu.trentorise.opendata.jackan.internal.org.apache.http.HttpEntity;
import eu.trentorise.opendata.jackan.internal.org.apache.http.HttpResponse;
import eu.trentorise.opendata.jackan.internal.org.apache.http.client.methods.HttpPost;
import eu.trentorise.opendata.jackan.internal.org.apache.http.entity.ContentType;
import eu.trentorise.opendata.jackan.internal.org.apache.http.entity.mime.MultipartEntityBuilder;
import eu.trentorise.opendata.jackan.internal.org.apache.http.entity.mime.content.FileBody;
import eu.trentorise.opendata.jackan.internal.org.apache.http.entity.mime.content.StringBody;
import eu.trentorise.opendata.jackan.internal.org.apache.http.impl.client.CloseableHttpClient;
import eu.trentorise.opendata.jackan.internal.org.apache.http.impl.client.HttpClientBuilder;
import eu.trentorise.opendata.jackan.model.CkanResource;

public class CkanClientUtils {
	private static final Logger logger = LoggerFactory.getLogger(CkanClientUtils.class);

	/**
	 * Upload a file to an *existing* record
	 * 
	 * @param ckanClient
	 * @param datasetName
	 * @param resourceId
	 * @param isResourceCreationRequired
	 * @param srcFilename
	 * @return
	 */
	public static CkanResource uploadFile(
			CkanClient ckanClient,
			String datasetName,
			String resourceId,
			//String resourceName,
			//boolean isResourceCreationRequired,
			String srcFilename,
			ContentType contentType,
			String downloadFilename)
	{
		Path path = Paths.get(srcFilename);
		logger.info("Updating ckan resource " + resourceId + " with content from " + path.toAbsolutePath());
	
		contentType = contentType == null ? ContentType.DEFAULT_TEXT : contentType;
		downloadFilename = downloadFilename == null ? path.getFileName().toString() : downloadFilename;
		
		String apiKey = ckanClient.getCkanToken();
		String HOST = ckanClient.getCatalogUrl();// "http://ckan.host.com";
	
		try (CloseableHttpClient httpclient = HttpClientBuilder.create().build()) {
	
			// Ideally I'd like to use nio.Path instead of File but apparently the http
			// client library does not support it(?)
			File file = path.toFile();
	
			// SimpleDateFormat dateFormatGmt = new SimpleDateFormat("yyyyMMdd_HHmmss");
			// String date=dateFormatGmt.format(new Date());
	
			HttpPost postRequest;
			HttpEntity reqEntity = MultipartEntityBuilder.create()
					.addPart("id", new StringBody(resourceId, ContentType.TEXT_PLAIN))
					//.addPart("name", new StringBody(resourceName, ContentType.TEXT_PLAIN))
					.addPart("package_id", new StringBody(datasetName, ContentType.TEXT_PLAIN))
					.addPart("upload", new FileBody(file, contentType, downloadFilename)) // , ContentType.APPLICATION_OCTET_STREAM))
					// .addPart("file", cbFile)
					// .addPart("url",new StringBody("path/to/save/dir", ContentType.TEXT_PLAIN))
					// .addPart("comment",new StringBody("comments",ContentType.TEXT_PLAIN))
					// .addPart("notes", new StringBody("notes",ContentType.TEXT_PLAIN))
					// .addPart("author",new StringBody("AuthorName",ContentType.TEXT_PLAIN))
					// .addPart("author_email",new StringBody("AuthorEmail",ContentType.TEXT_PLAIN))
					// .addPart("title",new StringBody("title",ContentType.TEXT_PLAIN))
					// .addPart("description",new StringBody("file
					// Desc"+date,ContentType.TEXT_PLAIN))
					.build();
	
			String url = false//isResourceCreationRequired
					? HOST + "/api/action/resource_create"
					: HOST + "/api/action/resource_update";
			
			postRequest = new HttpPost(url);
			
			postRequest.setEntity(reqEntity);
			postRequest.setHeader("Authorization", apiKey);
			// postRequest.setHeader("X-CKAN-API-Key", myApiKey);
	
			HttpResponse response = httpclient.execute(postRequest);
			int statusCode = response.getStatusLine().getStatusCode();			
			String status =  new BufferedReader(new InputStreamReader(response.getEntity().getContent()))
					.lines().collect(Collectors.joining("\n"));
	
			logger.info("Upload status: " + statusCode + "\n" + status);
	
			// TODO We could get rid of this extra request by processing the reply of the upload
			CkanResource result = ckanClient.getResource(resourceId);
			
			return result;
		} catch (IOException e) {
			throw new CkanException(e.getMessage(), ckanClient, e);
		}
	}

}
