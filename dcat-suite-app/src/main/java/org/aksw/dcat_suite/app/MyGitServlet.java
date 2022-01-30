package org.aksw.dcat_suite.app;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;

import org.aksw.dcat_suite.app.model.api.GroupMgrFactory;
import org.eclipse.jgit.http.server.GitServlet;
import org.eclipse.jgit.lib.Repository;
import org.springframework.beans.factory.annotation.Autowired;

@WebServlet(value = "/git/*", asyncSupported = true, initParams = {
})
public class MyGitServlet
	extends GitServlet
{
	@Autowired
	protected GroupMgrFactory gmf;

	@Override
	public void init() throws ServletException {
		System.out.println("Here");
		super.init();
	}
	
	@Override
	public void init(ServletConfig config) throws ServletException {

		// TODO Where to handle destroy?
		this.setRepositoryResolver((req, name) -> {

			String path = getRelativePath(req);

			String[] parts = path.split("/");
			String groupId = parts[0];
			Repository repository = gmf.create(groupId).get().getGitRepository();
			repository.incrementOpen();
			return repository;
		});

		super.init(config);
	}
	
	public static String getRelativePath(HttpServletRequest req) {
		String servletPath = req.getServletPath();
		String pathInfo = req.getPathInfo();

		int pathInfoInServletPath = servletPath.indexOf(pathInfo);
		if (pathInfoInServletPath < 0) {
			throw new RuntimeException("Heuristic to determine path failed.");
		}
		
		// Remove optional leading slash
		String result = servletPath.substring(pathInfoInServletPath).replaceAll("^/", "");		

		return result;
	}
}
