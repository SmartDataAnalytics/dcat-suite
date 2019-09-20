package org.aksw.ckan_deploy.core;

import java.util.LinkedHashMap;
import java.util.Map;

public class PathCoderRegistry {
	protected static PathCoderRegistry INSTANCE = null;

	protected Map<String, PathCoder> coders = new LinkedHashMap<>();
	
	public static PathCoderRegistry get() {
		if(INSTANCE == null) {
			INSTANCE = new PathCoderRegistry();

			INSTANCE.coders.put("bzip2", new PathCoderLbZip());
			INSTANCE.coders.put("gzip", new PathCoderGzip());
		}
		
		return INSTANCE;
	}
	
	
	public PathCoder getCoder(String name) {
		return coders.get(name);
	}
}