package org.aksw.dcat_suite.app.git;

/**
 * A very simple Git Server which allows to clone a Git repository. Currently
 * it will return the same repository for any name that is requested, there is no
 * logic to distinguish between different repos in this simple example.
 *
 * After starting this application, you can use something like
 *
 *      git clone http://localhost:8080/TestRepo
 *
 * to clone the repository from the running server.
 *
 *
 * Note: Visiting http://localhost:8080/&lt;reponame&gt; in the Browser
 * will not work and always return a HTTP Error 404.
 *
 * Also this is just a very simple sample and not a full-features
 * Git Server!
 *
 * Expect some work if you want to do anything useful with this!
 */
/*
public class MainTestGit {
    public static void main(String[] args) throws Exception {
        Repository repository = createNewRepository();

        populateRepository(repository);

        // Create the JGit Servlet which handles the Git protocol
        GitServlet gs = new GitServlet();
        gs.setRepositoryResolver((req, name) -> {
            repository.incrementOpen();
            return repository;
        });

        // start up the Servlet and start serving requests
        Server server = configureAndStartHttpServer(gs);

        // finally wait for the Server being stopped
        server.join();
    }

    private static Server configureAndStartHttpServer(GitServlet gs) throws Exception {
        Server server = new Server(8080);

        ServletHandler handler = new ServletHandler();
        server.setHandler(handler);

        if (true) { throw new RuntimeException("Git servlet needs to be updated for jakarta - 2023-11-29 ~ Claus"); }
        // ServletHolder holder = new ServletHolder(gs);
        // handler.addServletWithMapping(holder, "/git/*");

        server.start();
        return server;
    }

    private static void populateRepository(Repository repository) throws IOException, GitAPIException {
        // enable pushing to the sample repository via http
        repository.getConfig().setString("http", null, "receivepack", "true");

        try (Git git = new Git(repository)) {
            File myfile = new File(repository.getDirectory().getParent(), "testfile");
            if(!myfile.createNewFile()) {
                throw new IOException("Could not create file " + myfile);
            }

            git.add().addFilepattern("testfile").call();

            System.out.println("Added file " + myfile + " to repository at " + repository.getDirectory());

            git.commit().setMessage("Test-Checkin").call();
        }
    }

    private static Repository createNewRepository() throws IOException {
        // prepare a new folder
        File localPath = File.createTempFile("TestGitRepository", "");
        if(!localPath.delete()) {
            throw new IOException("Could not delete temporary file " + localPath);
        }

        if(!localPath.mkdirs()) {
            throw new IOException("Could not create directory " + localPath);
        }

        // create the directory
        Repository repository = FileRepositoryBuilder.create(new File(localPath, ".git"));
        repository.create();

        return repository;
    }
}
*/
