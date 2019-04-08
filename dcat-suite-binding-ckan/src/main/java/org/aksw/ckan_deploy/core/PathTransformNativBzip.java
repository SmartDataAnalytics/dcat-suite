package org.aksw.ckan_deploy.core;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.aksw.commons.util.compress.MetaBZip2CompressorInputStream;

public class PathTransformNativBzip
	implements PathTransform
{
	@Override
	public boolean cmdExists() {
		return true;
	}
	
	@Override
	public void transform(Path input, Path output) throws Exception {
		try(InputStream in = new MetaBZip2CompressorInputStream(Files.newInputStream(input))) {
			Files.copy(in, output);
		}
	}
}
