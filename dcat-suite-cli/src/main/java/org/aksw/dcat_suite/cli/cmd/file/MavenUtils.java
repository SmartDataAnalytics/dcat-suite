package org.aksw.dcat_suite.cli.cmd.file;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.codehaus.plexus.util.cli.StreamConsumer;

import com.google.common.base.StandardSystemProperty;

public class MavenUtils {


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


    public static ArtifactResolver createDefaultArtifactResolver(Invoker invoker, List<String> remoteRepositories) throws MavenInvocationException {
        Path localRepositoryPath = getLocalRepository(invoker);

        return new ArtifactResolverMavenRemote(
                new ArtifactResolverMavenLocal(localRepositoryPath),
                invoker,
                remoteRepositories);
    }

    public static void dependencyGet(Invoker invoker, String artifact, List<String> remoteRepositories) throws MavenInvocationException {
        InvocationRequest request = new DefaultInvocationRequest();
        request.setInputStream(InputStream.nullInputStream());

        request.setGoals(Collections.singletonList("dependency:get"));
        request.setQuiet(true);
        Properties props = new Properties();
        props.put("artifact", artifact);

        if (remoteRepositories != null && ! remoteRepositories.isEmpty()) {
            String str = remoteRepositories.stream().collect(Collectors.joining(","));
            props.put("remoteRepositories", str);
        }
        // props.put("forceStdout", "true");

        request.setProperties(props);;

        invoker.execute(request);
    }

    public static Path getLocalRepository(Invoker invoker) throws MavenInvocationException {
        InvocationRequest request = new DefaultInvocationRequest();
        request.setInputStream(InputStream.nullInputStream());

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
