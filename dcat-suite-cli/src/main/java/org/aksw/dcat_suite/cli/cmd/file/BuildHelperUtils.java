package org.aksw.dcat_suite.cli.cmd.file;

import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.codehaus.plexus.util.xml.Xpp3Dom;

public class BuildHelperUtils {
    public static Plugin createPlugin() {

        Plugin plugin = new Plugin();
        plugin.setGroupId("org.codehaus.mojo");
        plugin.setArtifactId("build-helper-maven-plugin");
        plugin.setVersion("3.3.0");

        PluginExecution execution = new PluginExecution();
        execution.setId("attach-artifacts");
        execution.setPhase("package");
        execution.addGoal("attach-artifact");

        // Artifact artifact = new Artifact();
        Xpp3Dom configuration = new Xpp3Dom("configuration");
        Xpp3Dom artifacts = new Xpp3Dom("artifacts");

        configuration.addChild(artifacts);
        execution.setConfiguration(configuration);
        plugin.addExecution(execution);

        return plugin;
    }

    public static Xpp3Dom attachArtifact(Plugin plugin, String file, String type, String classifier) {
        PluginExecution execution = plugin.getExecutionsAsMap().get("attach-artifacts");

        Xpp3Dom configuration = (Xpp3Dom)execution.getConfiguration(); // plugin.getConfiguration();
        Xpp3Dom artifacts = configuration.getChild("artifacts");

        Xpp3Dom artifact = new Xpp3Dom("artifact");
        Xpp3DomUtils.addEntryAsChild(artifact, "file", file);
        Xpp3DomUtils.addEntryAsChild(artifact, "type", type);
        Xpp3DomUtils.addEntryAsChild(artifact, "classifier", classifier);

        artifacts.addChild(artifact);

        return artifact;
    }

}
