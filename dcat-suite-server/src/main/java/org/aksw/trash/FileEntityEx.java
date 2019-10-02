package org.aksw.trash;

import java.nio.file.Path;

import org.aksw.jena_sparql_api.http.domain.api.RdfEntityInfo;

/**
 * Custom subclass in order to allow for clean access to the file.
 * (IMO FileEntity should already provide that getter)
 * 
 * @author raven
 *
 */
public class FileEntityEx
	extends RdfFileEntityImpl
//	extends FileEntity
{
	public FileEntityEx(Path path, RdfEntityInfo info) {
		super(null, path, info);
	}

	
//	public FileEntityEx(File file, ContentType contentType) {
//		super(file, contentType);
//	}
//
//	public FileEntityEx(File file) {
//		super(file);
//	}
//
//	public File getFile() {
//		return file;
//	}
}