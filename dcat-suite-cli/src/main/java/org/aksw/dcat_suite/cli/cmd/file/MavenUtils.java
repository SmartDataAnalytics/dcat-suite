package org.aksw.dcat_suite.cli.cmd.file;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.function.Supplier;

import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.codehaus.plexus.util.cli.StreamConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.StandardSystemProperty;

public class MavenUtils {

    private static final Logger logger = LoggerFactory.getLogger(MavenUtils.class);


    public static class StringStreamConsumer
        implements StreamConsumer
    {
        protected StringBuffer sb = new StringBuffer();
        protected String ls = StandardSystemProperty.LINE_SEPARATOR.value();

        @Override
        public void consumeLine( String line )
        {
            if (sb.length() != 0) {
                sb.append(ls);
            }
            sb.append(line);
        }

        public String getOutput()
        {
            return sb.toString();
        }
    }


    public static ArtifactResolver createDefaultArtifactResolver(
            Invoker invoker,
            Path pomFile,
            List<String> profiles,
            List<String> remoteRepositories) throws MavenInvocationException {
        Path localRepositoryPath = getLocalRepository(invoker);

        Supplier<InvocationRequest> invocationRequestFactory = () -> {
            DefaultInvocationRequest r = new DefaultInvocationRequest();
            r.setPomFile(pomFile.toFile());
            r.setProfiles(profiles);
            InvocationRequestUtils.setProperty(r, "remoteRepositories", ",", remoteRepositories);
            return r;
        };

        return new ArtifactResolverMavenRemote(
                new ArtifactResolverMavenLocal(localRepositoryPath),
                invoker,
                invocationRequestFactory);
    }


//    public static void dependencyGet(Invoker invoker, String artifact, List<String> remoteRepositories) throws MavenInvocationException {
//    }

    public static void dependencyGet(Invoker invoker, String artifact, List<String> remoteRepositories, InvocationRequest request) throws MavenInvocationException {
        request = request == null ? new DefaultInvocationRequest() : request;

        request.setInputStream(InputStream.nullInputStream());

        request.setGoals(Collections.singletonList("dependency:get"));
        request.setQuiet(true);
        InvocationRequestUtils.setProperty(request, "artifact", artifact);

        if (remoteRepositories != null && ! remoteRepositories.isEmpty()) {
            InvocationRequestUtils.setProperty(request, "remoteRepositories", ",", remoteRepositories);
        }

        invoker.execute(request);
    }

    public static Path getLocalRepository(Invoker invoker) throws MavenInvocationException {
        InvocationRequest request = new DefaultInvocationRequest();
        request.setInputStream(InputStream.nullInputStream());
        request.setErrorHandler(logger::warn);

        StringStreamConsumer sc = new StringStreamConsumer();
        request.setOutputHandler(sc::consumeLine);

        request.setGoals(Collections.singletonList("help:evaluate"));
        request.setQuiet(true);
        Properties props = new Properties();
        props.put("expression", "settings.localRepository");
        props.put("forceStdout", "true");

        request.setProperties(props);;

        invoker.execute(request);
        String str = sc.getOutput();
        Path result = Path.of(str);
        return result;
    }
}
