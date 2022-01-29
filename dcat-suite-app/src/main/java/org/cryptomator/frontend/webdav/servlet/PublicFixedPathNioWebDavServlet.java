package org.cryptomator.frontend.webdav.servlet;

import java.nio.file.Path;

/** Package hack to make package-visible class {@link FixedPathNioWebDavServlet} public */
public class PublicFixedPathNioWebDavServlet
	extends FixedPathNioWebDavServlet
{

	public PublicFixedPathNioWebDavServlet(Path rootPath) {
		super(rootPath);
	}
}
