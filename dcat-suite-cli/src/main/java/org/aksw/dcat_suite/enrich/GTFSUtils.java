package org.aksw.dcat_suite.enrich;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public abstract class GTFSUtils {

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
