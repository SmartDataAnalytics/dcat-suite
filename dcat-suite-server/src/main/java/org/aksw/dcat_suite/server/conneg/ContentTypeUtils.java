package org.aksw.dcat_suite.server.conneg;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.beam.repackaged.beam_sdks_java_core.com.google.common.collect.Iterables;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.entity.ContentType;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;

public class ContentTypeUtils {

	// TODO Move to registry, use the RDF model for init
	protected static MapPair<String, String> ctExtensions = new MapPair<>();
	protected static MapPair<String, String> codingExtensions = new MapPair<>();

	static {
		for(Lang lang : RDFLanguages.getRegisteredLanguages()) {
			String contentType = lang.getContentType().getContentType();
			String primaryFileExtension = Iterables.getFirst(lang.getFileExtensions(), null);
			if(primaryFileExtension != null) {
				ctExtensions.getPrimary().put(contentType, primaryFileExtension);
			}
			
			for(String fileExtension : lang.getFileExtensions()) {
				ctExtensions.getAlternatives().put(fileExtension, contentType);
			}
		}
	
		ctExtensions.putPrimary(ContentType.APPLICATION_OCTET_STREAM.toString(), "bin");
		
		// TODO We need a registry for coders similar to RDFLanguages
		codingExtensions.putPrimary("gzip", "gz");
		codingExtensions.putPrimary("bzip2", "bz2");
	}
	
	
	// TODO Move 
	
	public static String toFileExtension(Header[] headers) {
		String result = toFileExtensionCt(headers) +
				toFileExtension(HttpHeaderUtils.getValues(headers, HttpHeaders.CONTENT_ENCODING));
		return result;
	}
		
	
	public static String toFileExtensionCt(Header[] headers) {
		String ct = HttpHeaderUtils.getValue(headers, HttpHeaders.CONTENT_TYPE);
		String result = ctExtensions.getPrimary().get(ct);
		Objects.requireNonNull(result, "Could not find file extension for content type: " + ct);
		result = "." + result;
		return result;		
	}
	
	public static String toFileExtension(List<String> codings) {
		List<String> parts = new ArrayList<>(codings.size());
		
		for(String coding : codings) {
			String part = Objects.requireNonNull(codingExtensions.getPrimary().get(coding));
			parts.add(part);
		}
		
		String result = parts.stream().collect(Collectors.joining("."));
		
		result = result.isEmpty() ? result : "." + result;
		return result;
	}

	
	public static String toFileExtension(RdfEntityInfo info) {
		Header[] headers = HttpHeaderUtils.toHeaders(info);
		String result = toFileExtension(headers);
		return result;
	}
	
	public static String toFileExtension(String contentType) {
		String result = Objects.requireNonNull(ctExtensions.getPrimary().get(contentType));
		result = result.isEmpty() ? result : "." + result;
		return result;
	}

	
	public  String toFileExtension(String contentType, List<String> codings) {
		List<String> parts = new ArrayList<>(1 + codings.size());
		
		String part = Objects.requireNonNull(ctExtensions.getPrimary().get(contentType));
		parts.add(part);
		
		for(String coding : codings) {
			part = Objects.requireNonNull(codingExtensions.getPrimary().get(coding));
			parts.add(part);
		}
		
		String result = parts.stream().collect(Collectors.joining("."));
		return result;
	}

	/**
	 * Attempts to get Content-type, Content-encoding from a given filename 
	 * 
	 * @param resultFile The non-null file that will be passed into the FileEntityEx result. Used for pragmatic reasons, as it seems to be the
	 * best Entity class that prevents us from having to roll our own.
	 * @param fileName
	 * @return
	 */
	public static RdfEntityInfo deriveHeadersFromFileExtension(String fileName) {
		// TODO Should we remove trailing slashes?
		
		String contentType = null;
		List<String> codings = new ArrayList<>();
		

		String current = fileName;
		
		while(true) {
			String ext = com.google.common.io.Files.getFileExtension(current);
			
			if(ext == null) {
				break;
			}
			
			// Classify the extension - once it maps to a content type, we are done
			String coding = codingExtensions.getAlternatives().get(ext);
			String ct = ctExtensions.getAlternatives().get(ext);

			// Prior validation of the maps should ensure that at no point a file extension
			// denotes both a content type and a coding
			assert !(coding != null && ct != null) :
				"File extension conflict: '" + ext + "' maps to " + coding + " and " + ct;  
			
			if(coding != null) {
				codings.add(coding);
			}
			
			if(ct != null) {
				contentType = ct;//MediaType.parse(ct);
				break;
			}
			
			// both coding and ct were null - skip
			if(coding == null && ct == null) {
				break;
			}
			
			current = com.google.common.io.Files.getNameWithoutExtension(current);//current.substring(0, current.length() - ext.length());
		}
		
		RdfEntityInfo result = null;
		if(contentType != null) {
			result = ModelFactory.createDefaultModel().createResource().as(RdfEntityInfo.class);
			result.setContentType(contentType);
			result.setContentEncodings(codings);
			
//			fileEntity = new FileEntityEx(resultFile);
//			fileEntity.setContentType(contentType);
//			if(codings.isEmpty()) {
//				// nothing to do
//			} else {
//				String str = codings.stream().collect(Collectors.joining(","));
//				fileEntity.setContentEncoding(str);
//			}
		}
		
		return result;

	}
}
