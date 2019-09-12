package org.aksw.ckan_deploy.core;

import java.io.InputStream;
import java.io.OutputStream;

public interface StreamTransform {
	InputStream transform(OutputStream out) throws Exception;
}
