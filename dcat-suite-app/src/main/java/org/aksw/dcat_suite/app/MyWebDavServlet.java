package org.aksw.dcat_suite.app;

import java.nio.file.Path;

import javax.servlet.annotation.WebServlet;

import org.aksw.dcat_suite.app.model.api.GroupMgrFactory;
import org.cryptomator.webdav.core.servlet.AbstractNioWebDavServlet;
import org.springframework.beans.factory.annotation.Autowired;

// Note that the url path is by default appended - example:
// without further config http://localhost/webdav/test.txt maps to /tmp/webdav/test.txt
@WebServlet(value = "/webdav/*", asyncSupported = true, initParams = {
//		  @WebInitParam(name = "ui", value = "com.example.MyUI"),
//		  @WebInitParam(name = "productionMode", value = "false")
})
public class MyWebDavServlet
	extends AbstractNioWebDavServlet
{
	@Autowired
	protected GroupMgrFactory gmf;

//	@Autowired
//	protected HttpServletRequest req;
	
	@Override
	protected Path resolveUrl(String relativeUrl) throws IllegalArgumentException {
		// Remove the servlet path
		String actualPath = relativeUrl.substring("webdav/".length());

		String[] groupAndPath = actualPath.split("/", 2);
		String groupId = groupAndPath[0];
		String pathStr = groupAndPath.length > 1 ? groupAndPath[1] : "";

		System.out.println("Webdav paths: " + relativeUrl + " - " + groupId + " - " + pathStr);

		
		Path result = gmf.create(groupId).getBasePath().resolve(pathStr);
		return result;
	}

}
