package org.aksw.dcat_suite.app;

import java.nio.file.Path;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;

import org.cryptomator.frontend.webdav.servlet.PublicFixedPathNioWebDavServlet;

// Note that the url path is by default appended - example:
// without further config http://localhost/webdav/test.txt maps to /tmp/webdav/test.txt
@WebServlet(value = "/webdav/*", asyncSupported = true, initParams = {
//		  @WebInitParam(name = "ui", value = "com.example.MyUI"),
//		  @WebInitParam(name = "productionMode", value = "false")
})
public class MyWebDavServlet
	extends PublicFixedPathNioWebDavServlet
{
	public MyWebDavServlet() {
		super(Path.of("/tmp"));
	}
	
	@Override
	public void init() throws ServletException {
		System.out.println("Init of webdav servlet called");
		super.init();
	}
}
