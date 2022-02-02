package org.aksw.dcat_suite.app;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;

import org.aksw.dcat_suite.app.model.api.GroupMgrFactory;
import org.eclipse.jgit.http.server.GitServlet;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.StoredConfig;
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
			enableInsecureReceiving(repository);
			
			repository.incrementOpen();
			return repository;
		});

		super.init(config);
	}
	
    /**
     * Source: https://stackoverflow.com/questions/58362941/gitservlet-password-query-after-push-command
     *   https://www.programcreek.com/java-api-examples/?code=arquillian/smart-testing/smart-testing-master/functional-tests/git-rules/src/main/java/org/arquillian/smart/testing/rules/git/server/EmbeddedHttpGitServer.java
     * 
     * To allow performing push operations from the cloned repository to remote (served by this server) let's
     * skip authorization for HTTP.
     */
    private void enableInsecureReceiving(Repository repository) {
        final StoredConfig config = repository.getConfig();
        config.setBoolean("http", null, "receivepack", true);
        try {
            config.save();
        } catch (IOException e) {
            throw new RuntimeException("Unable to save http.receivepack=true config", e);
        }
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
