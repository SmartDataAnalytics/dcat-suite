package org.aksw.dcat_suite.cli.main;

import java.io.File;
import java.io.FileReader;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Properties;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;

public class MainMavenIntegration {
    public static void main(String[] args) throws Exception {
        MavenXpp3Reader reader = new MavenXpp3Reader();
        Model model = reader.read(new FileReader("/home/raven/.dcat/pom.xml"));
        model.setArtifactId("wee");
        model.setGroupId("foo");
        MavenXpp3Writer writer = new MavenXpp3Writer();
        try (OutputStream out = Files.newOutputStream(Path.of("/tmp/test.xml"))) {
            writer.write(out, model);
            out.flush();
        }

        if (true) return;

        InvocationRequest request = new DefaultInvocationRequest();
        request.setPomFile(new File("/home/raven/.dcat/pom.xml"));
        request.setGoals(Collections.singletonList("dependency:copy"));
        Properties props = new Properties();
        props.put("artifact", "org.dllearner:scripts:1.2-SNAPSHOT");
        props.put("outputDirectory", "/tmp/target");
        request.setProperties(props);

        Invoker invoker = new DefaultInvoker();
        // invoker.setMavenHome(new File("/tmp"));

        try {
            invoker.execute(request);
        } catch (MavenInvocationException e) {
            e.printStackTrace();
        }
    }


}
