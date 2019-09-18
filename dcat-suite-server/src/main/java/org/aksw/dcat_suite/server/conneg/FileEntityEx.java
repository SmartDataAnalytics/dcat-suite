package org.aksw.dcat_suite.server.conneg;

import java.nio.file.Path;

/**
 * Custom subclass in order to allow for clean access to the file.
 * (IMO FileEntity should already provide that getter)
 * 
 * @author raven
 *
 */
public class FileEntityEx
	extends RdfFileEntityImpl<RdfEntityInfo>
//	extends FileEntity
{
	public FileEntityEx(Path path, RdfEntityInfo info) {
		super(null, path, RdfEntityInfo.class, info);
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