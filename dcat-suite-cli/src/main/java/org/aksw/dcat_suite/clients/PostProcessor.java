package org.aksw.dcat_suite.clients;

import java.net.URL;
import java.nio.charset.StandardCharsets;

import eu.trentorise.opendata.jackan.model.CkanResource;

import java.util.List;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.util.UriUtils;

import eu.trentorise.opendata.jackan.model.CkanDataset; 

public class PostProcessor {
	
	private static final Pattern KB_PATTERN = Pattern.compile("[Kk][Bb]");
	private static final Pattern MB_PATTERN = Pattern.compile("[Mm][Bb]");
	private static final Pattern GB_PATTERN = Pattern.compile("[Gg][Bb]");
	private static final long KB_VALUE = 1024; 
	private static final long MB_VALUE = 1048576; 
	private static final long GB_VALUE = 1073741824;  
	
	private static final Logger logger = LoggerFactory.getLogger(PostProcessor.class);
	
	public static final String ALT_NAME_ATTRIBUTE = "title";
	public static final String CHAR_SEQUENCE = "[a-zA-Z]";
	
	public synchronized static CkanDataset process(CkanDataset dataset) {
		if (dataset.getUrl() != null) {
			dataset.setUrl(normalizeURL(dataset.getUrl())); 
		}
			if (dataset.getResources() != null) {
				//dataset.setResources(processResources(dataset.getResources(), dataset)); 
				processResources(dataset.getResources(), dataset);
		}
		return dataset; 
	}
	
	private synchronized static void processResources (List<CkanResource> resources, CkanDataset dataset) {
		//List<CkanResource> processedResources = new ArrayList<CkanResource>(); 
		for (CkanResource cr : resources) {
			if (dataset.getId() != null ) {
				cr.setPackageId(dataset
						.getId());
			}
            if (cr.getSize() != null ) {
            	cr.setSize(convertBytes(cr.getSize()
            			.replaceAll(" ", "")));
            }
            if (cr.getUrl() != null ) {
            	cr.setUrl(normalizeURL(cr.getUrl())); 
            }
            if (cr.getOthers() != null) {
           	for (String key : cr.getOthers().keySet()) {
           		if (key.equals(ALT_NAME_ATTRIBUTE)) { 
            			String altName = cr
            					.getOthers()
            					.get(key)
            					.toString();
            			cr.setName(altName);
              }
            }
        }

            //processedResources.add(cr);
     }
		//return processedResources;
   }    
	
	private synchronized static String normalizeURL(String rawUrl) {
    	String processedUrl = null;	
    	String tobeReplaced; 
		String subStr = rawUrl.substring(rawUrl.lastIndexOf("/") + 1);
		String [] parts = subStr.split("\\?(?!\\?)");
		try {
	  			
				if (parts.length > 1)
					tobeReplaced = UriUtils
						.encode(parts[0],StandardCharsets.UTF_8.toString())+"?"+UriUtils
						.encodeQuery(parts[1],StandardCharsets.UTF_8.toString());
				else {
					tobeReplaced = UriUtils
							.encode(subStr,StandardCharsets.UTF_8.toString());
				}
				processedUrl = new URL (rawUrl.replace(subStr, tobeReplaced))
						.toString()
						.replace(" ","%20"); 
    		}
    	catch (Exception ex) {
    		logger.warn("Cannot parse access URL. Dropping it!");
    	}
		return processedUrl; 
	}
	
	private static String convertBytes (String bytes) {
		if (bytes.isEmpty()) {
			return null;
		}
		else if (bytes.matches(CHAR_SEQUENCE)) {
			return bytes.replaceAll(CHAR_SEQUENCE,"");
		}
		else {
			
			String strippedBytes =  bytes.replaceAll(CHAR_SEQUENCE,"");
			Double bytesDouble = Double.parseDouble(strippedBytes);
			if (KB_PATTERN.matcher(bytes).find()) {
				bytesDouble = bytesDouble*KB_VALUE; 
			} 
			else if (MB_PATTERN.matcher(bytes).find()) {
				bytesDouble = bytesDouble*MB_VALUE;
			}
			else if (GB_PATTERN.matcher(bytes).find()) {
				bytesDouble = bytesDouble*GB_VALUE;
			}
			return String.valueOf(bytesDouble);
		}
		
	}

//}
}