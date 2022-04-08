package org.aksw.dcat_suite.cli.cmd.file;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.concurrent.Callable;

import org.aksw.dcat.jena.domain.api.DcatDataset;
import org.aksw.dcat.jena.domain.api.DcatDistribution;
import org.aksw.dcat.jena.domain.api.MavenEntity;
import org.aksw.dcat.utils.DcatUtils;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.maven.model.Build;
import org.apache.maven.model.Parent;
import org.apache.maven.model.Resource;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;

import picocli.CommandLine.Command;

@Command(name = "pom", separator = "=", description="Prepare a maven project from the dataset description", mixinStandardHelpOptions = true)
public class CmdDcatFilePom
    implements Callable<Integer>
{
    protected String buildDirName = "target";


    @Override
    public Integer call() throws Exception {
        MavenXpp3Reader pomReader = new MavenXpp3Reader();
        MavenXpp3Writer pomWriter = new MavenXpp3Writer();

        org.apache.maven.model.Model parentPom;
        try (InputStream in = this.getClass().getClassLoader().getResourceAsStream("dcat.template.pom.xml")) {
            parentPom = pomReader.read(in);
        }

        parentPom.setGroupId("myGroupId");
        parentPom.setArtifactId("myArtifactId");
        parentPom.setVersion("1.0.0-SNAPSHOT");

        Parent parent = new Parent();
        parent.setGroupId(parentPom.getGroupId());
        parent.setArtifactId(parentPom.getArtifactId());
        parent.setVersion(parentPom.getVersion());


        DcatRepoLocal repo = DcatRepoLocalUtils.findLocalRepo();

        Path basePath = repo.getBasePath();
        Dataset dataset = repo.getDataset();

        Model model = dataset.getUnionModel();

        Collection<DcatDataset> datasets = DcatUtils.listDcatDatasets(model);

        Path buildDir = Path.of("target").toAbsolutePath();;
        Files.createDirectories(buildDir);
        // Group datasets by their group id

        for (DcatDataset d : datasets) {
            MavenEntity mvnEntity = d.as(MavenEntity.class);
            String artifactId = mvnEntity.getArtifactId();

            org.apache.maven.model.Model childPom = new org.apache.maven.model.Model();
            childPom.setModelVersion("4.0.0");
            childPom.setParent(parent);

            childPom.setArtifactId(artifactId);
            childPom.setGroupId(mvnEntity.getGroupId());
            childPom.setVersion(mvnEntity.getVersion());


            Build build = new Build();
            Resource resource = new Resource();
            resource.setDirectory("../../");

            build.addResource(resource);
            childPom.setBuild(build);


            // Get the graph having the same name as the dataset
            // copy it to the resources folder


            for (DcatDistribution dist : d.getBasicDistributions()) {
                String downloadUrl = dist.getDownloadUrl();
                if (downloadUrl != null) {
                    resource.addInclude(downloadUrl);
                }
            }

            parentPom.addModule(artifactId);

            Path moduleDir = buildDir.resolve(artifactId);
            Files.createDirectories(moduleDir);

            try (OutputStream out = Files.newOutputStream(moduleDir.resolve("pom.xml"))) {
                pomWriter.write(out, childPom);
            }
        }

        try (OutputStream out = Files.newOutputStream(buildDir.resolve("pom.xml"))) {
            pomWriter.write(out, parentPom);
        }


//        Path pwd = Path.of("").toAbsolutePath();
//
//
//        Path buildAbsPath = pwd.resolve(buildDirName);
//
//        String str;
//        try (InputStream in = CmdDcatFilePom.class.getClassLoader().getResourceAsStream("dcat.pom.xml.template")) {
//            Objects.requireNonNull(in);
//            str = IOUtils.toString(in, StandardCharsets.UTF_8);
//        }

        // Envsubst.envsubst(str, );

        return 0;
    }

}
