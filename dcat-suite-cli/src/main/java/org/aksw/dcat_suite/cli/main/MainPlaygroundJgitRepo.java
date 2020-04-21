package org.aksw.dcat_suite.cli.main;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.aksw.jena_sparql_api.http.repository.impl.UriToPathUtils;
import org.apache.commons.compress.utils.Lists;
import org.apache.jena.ext.com.google.common.base.StandardSystemProperty;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.OpenSshConfig;
import org.eclipse.jgit.transport.SshSessionFactory;
import org.eclipse.jgit.transport.SshTransport;
import org.eclipse.jgit.util.FS;

import com.jcraft.jsch.IdentityRepository;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;
import com.jcraft.jsch.agentproxy.AgentProxyException;
import com.jcraft.jsch.agentproxy.Connector;
import com.jcraft.jsch.agentproxy.RemoteIdentityRepository;
import com.jcraft.jsch.agentproxy.USocketFactory;
import com.jcraft.jsch.agentproxy.connector.SSHAgentConnector;
import com.jcraft.jsch.agentproxy.usocket.JNAUSocketFactory;

public class MainPlaygroundJgitRepo {
	public static List<Path> listPrivateKeys(Path path) throws IOException {
		List<Path> result = Files.list(path)
				// .peek(p -> System.out.println(p))
				// .filter(p -> p.getFileName().toString().endsWith(".pub"))
				.filter(p -> !p.getFileName().toAbsolutePath().endsWith(".pub"))
				.filter(p -> Files.exists(p.getParent().resolve(p.getFileName().toString() + ".pub")))
				.collect(Collectors.toList());
		System.out.println(result);

		return result;
	}

	public static List<Path> match(Path basePath, String glob) throws IOException {
//		String tmp = "glob:" + glob;
//		PathMatcher pathMatcher = basePath.getFileSystem().getPathMatcher(tmp);
		List<Path> result = null; // new ArrayList<>();
	
		try(DirectoryStream<Path> stream = Files.newDirectoryStream(basePath, glob)) {
			result = Lists.newArrayList(stream.iterator());
		}
		
		return result;
//		Files.walkFileTree(basePath, new SimpleFileVisitor<Path>() {
//			@Override
//			public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
//				System.out.println(path);
//				if (pathMatcher.matches(path)) {
//					result.add(path);
//				}
//				return FileVisitResult.CONTINUE;
//			}
//
//			@Override
//			public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
//				return FileVisitResult.CONTINUE;
//			}
//		});
//		
//		return result;
	}

	public static SshSessionFactory createSshSessionFactory() {
		// Path gitRoot = Paths.get("/home/raven/.dcat/git");
		// CatalogResolver cr = CatalogResolverUtils.createCatalogResolverDefault();

		SshSessionFactory sshSessionFactory = new JschConfigSessionFactory() {

//			@Override
//			protected JSch createDefaultJSch(FS fs) throws JSchException {
//				JSch defaultJSch = super.createDefaultJSch(fs);
//				//defaultJSch.removeAllIdentity();
//
//				Path path = Paths.get(StandardSystemProperty.USER_HOME.value()).resolve(".ssh");
//
//				List<Path> privateKeyFiles;
//				try {
//					privateKeyFiles = listPrivateKeys(path);
//				} catch (IOException e) {
//					throw new RuntimeException(e);
//				}
//
//				for (Path p : privateKeyFiles) {
//					defaultJSch.addIdentity(p.toString());
//				}
//				return defaultJSch;
//			}

			@Override
			protected JSch createDefaultJSch(FS fs) throws JSchException {
				Connector con = null;
				try {
					if (SSHAgentConnector.isConnectorAvailable()) {
						// USocketFactory usf = new JUnixDomainSocketFactory();
						USocketFactory usf = new JNAUSocketFactory();
						con = new SSHAgentConnector(usf);
					}
				} catch (AgentProxyException e) {
					System.out.println(e);
				}

				JSch jsch = super.createDefaultJSch(fs);
				if (con != null) {
					JSch.setConfig("PreferredAuthentications", "publickey");

					IdentityRepository identityRepository = new RemoteIdentityRepository(con);
					jsch.setIdentityRepository(identityRepository);
				}

				return jsch;
//		        if (con == null) {
//		            return 
//		        } else {
//		            final JSch jsch = new JSch();
//		            jsch.setConfig("PreferredAuthentications", "publickey");
//		            IdentityRepository irepo = new RemoteIdentityRepository(con);
//		            jsch.setIdentityRepository(irepo);
//		            knownHosts(jsch, fs); // private method from parent class, yeah for Groovy!
//		            return jsch;
//		        }
			}

			@Override
			protected void configure(OpenSshConfig.Host host, Session session) {

				session.setConfig("StrictHostKeyChecking", "no");

				session.setUserInfo(new UserInfo() {
					@Override
					public String getPassphrase() {
						System.err.println("Passphrase requested");
						return null;
						// return "passphrase";
					}

					@Override
					public String getPassword() {
						return null;
					}

					@Override
					public boolean promptPassword(String message) {
						System.err.println(message);
						return true;
					}

					@Override
					public boolean promptPassphrase(String message) {
						System.err.println(message);
						return true;
					}

					@Override
					public boolean promptYesNo(String message) {
						System.err.println(message);
						return true;
					}

					@Override
					public void showMessage(String message) {
						System.err.println(message);
					}
				});

			}

		};
		
		return sshSessionFactory;
	}
	
	public static void main(String[] args) throws Exception {

		Path gitCacheBase = Paths.get(StandardSystemProperty.USER_HOME.value()).resolve(".dcat").resolve("git");
		Files.createDirectories(gitCacheBase);

		String gitUrl = "https://github.com/SmartDataAnalytics/Meta-LOD.git";
		List<String> filenamePatterns = Arrays.asList("*.ttl");

		Path relPath = UriToPathUtils.resolvePath(gitUrl); //.resolve("_content");

		Path fullPath = gitCacheBase.resolve(relPath).toAbsolutePath();
		Files.createDirectories(fullPath.getParent());
		
		SshSessionFactory sshSessionFactory = createSshSessionFactory();

		TransportConfigCallback transportCallback = transport -> {
			if(transport instanceof SshTransport) {
				SshTransport sshTransport = (SshTransport) transport;
				sshTransport.setSshSessionFactory(sshSessionFactory);
			}
		};
		
		if(!Files.exists(fullPath)) {
			Git.cloneRepository()
				.setURI(gitUrl)
				.setDirectory(fullPath.toFile())
				.setTransportConfigCallback(transportCallback)
				.call();
		}
		
		
		Git gitRepo = Git.open(fullPath.toFile());

//		Git gitRepo = Git.open(new File("/home/raven/Projects/limbo/git/metadata-catalog"));

		PullResult res = gitRepo
				.pull()
				.setTransportConfigCallback(transportCallback)
				.call();
		
		List<Path> matches = filenamePatterns.stream()
				.flatMap(pattern -> {
					try {
						return match(fullPath, pattern).stream();
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				})
				.collect(Collectors.toList());
		
		System.out.println("Matched files: " + matches);
	}
}
