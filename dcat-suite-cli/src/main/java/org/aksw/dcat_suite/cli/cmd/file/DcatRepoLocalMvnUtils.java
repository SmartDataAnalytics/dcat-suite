package org.aksw.dcat_suite.cli.cmd.file;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.aksw.dcat.repo.impl.fs.CatalogResolverFilesystem;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

public class DcatRepoLocalMvnUtils {

    public static Path getDefaultPomTemplate() {
        Path repoRoot = CatalogResolverFilesystem.getDefaultRepoDir();
        // Path repoRoot = catalogResolver.getDcatRepoRoot();
        Path pomXmlFile = repoRoot.resolve("template.pom.xml");
        if (!Files.exists(pomXmlFile)) {
            throw new RuntimeException("No default pom.xml found at " + pomXmlFile);
        }

        return pomXmlFile;
    }

    public static Model loadDefaultPomTemplateModel() throws IOException, XmlPullParserException {
        MavenXpp3Reader pomReader = new MavenXpp3Reader();
        Path path = getDefaultPomTemplate();

        org.apache.maven.model.Model parentPom;
        try (InputStream in = Files.newInputStream(path)) {
            parentPom = pomReader.read(in);
        }
        return parentPom;
    }

}
