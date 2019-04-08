package org.aksw.ckan_deploy.core;

import java.util.LinkedHashMap;
import java.util.Map;

public class PackerRegistry {
	protected Map<String, PathTransform> mimeTypeToTransform;
	
	public PackerRegistry(Map<String, PathTransform> mimeTypeToTransform) {
		this.mimeTypeToTransform = mimeTypeToTransform;
	}
	
	public Map<String, PathTransform> getMap() {
		return mimeTypeToTransform;
	}
	
	public static PackerRegistry createDefault() {
		Map<String, PathTransform> mimeTypeToTransform = new LinkedHashMap<>();

		String mimeType = "application/x-bzip";
		
		PathTransform lbzip = new PathTransformLbZip();
		if(lbzip.cmdExists()) {
			mimeTypeToTransform.put(mimeType, lbzip);
		} else {
			lbzip = new PathTransformNativBzip();
			mimeTypeToTransform.put(mimeType, lbzip);
		}
		
		return new PackerRegistry(mimeTypeToTransform);
	}
}
