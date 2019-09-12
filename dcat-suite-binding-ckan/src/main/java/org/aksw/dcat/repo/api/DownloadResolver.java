package org.aksw.dcat.repo.api;

import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;

public interface DownloadResolver {
	DistributionResolver getDistributionResolver();


	/**
	 * Get the URL that backs this download
	 * 
	 * @return
	 */
	URL getURL();

	/**
	 * Return the file path for the download, if it exists
	 * 
	 * @return
	 */
	Path getPath();
	
	InputStream open() throws Exception;

}
