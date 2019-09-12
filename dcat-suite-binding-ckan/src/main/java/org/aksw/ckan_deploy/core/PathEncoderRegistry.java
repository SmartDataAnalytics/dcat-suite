package org.aksw.ckan_deploy.core;

import java.util.LinkedHashMap;
import java.util.Map;

public class PathEncoderRegistry {
	protected static PathEncoderRegistry INSTANCE = null;

	protected Map<String, PathCoder> coders = new LinkedHashMap<>();
	
	public static PathEncoderRegistry get() {
		if(INSTANCE == null) {
			INSTANCE = new PathEncoderRegistry();

			INSTANCE.coders.put("bzip", new PathCoderLbZip());
			INSTANCE.coders.put("gzip", new PathCoderGzip());
		}
		
		return INSTANCE;
	}
	
	
	public PathCoder getCoder(String name) {
		return coders.get(name);
	}
}
