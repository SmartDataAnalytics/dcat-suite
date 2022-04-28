package org.aksw.dcat_suite.cli.cmd.file;

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import org.aksw.dcat.jena.domain.api.DcatDistribution;
import org.aksw.dcat.jena.domain.api.MavenEntity;
import org.aksw.jenax.arq.dataset.impl.ResourceInDatasetImpl;
import org.aksw.jenax.arq.util.execution.QueryExecutionUtils;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.maven.model.Build;
import org.apache.maven.model.Parent;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.Resource;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;

import picocli.CommandLine.Command;

@Command(name = "content", separator = "=", description="Prepare a maven project from the dataset description", mixinStandardHelpOptions = true)
public class CmdDcatMvnizeContent
    implements Callable<Integer>
{
    protected String buildDirName = "target";



    @Override
    public Integer call() throws Exception {

        MavenXpp3Writer pomWriter = new MavenXpp3Writer();

        org.apache.maven.model.Model parentPom = DcatRepoLocalMvnUtils.loadDefaultPomTemplateModel();

        parentPom.setGroupId("myGroupId");
        parentPom.setArtifactId("myArtifactId");
        parentPom.setVersion("1.0.0-SNAPSHOT");

        Parent parent = new Parent();
        parent.setGroupId(parentPom.getGroupId());
        parent.setArtifactId(parentPom.getArtifactId());
        parent.setVersion(parentPom.getVersion());


        DcatRepoLocal repo = DcatRepoLocalUtils.findLocalRepo();

        Path basePath = repo.getBasePath();
        Dataset fileCentricDataset = repo.getDataset();




        // Model model = fileCentricDataset.getUnionModel();

        // Collection<DcatDistribution> distributions = DcatUtils.listDcatDistributions(model);

        Query distQuery = QueryFactory.create("PREFIX dcat: <http://www.w3.org/ns/dcat#> SELECT DISTINCT ?x { GRAPH ?x { { ?x a dcat:Distribution } UNION { ?x dcat:downloadURL [] } UNION { [] dcat:distribution ?x } } }");
        List<DcatDistribution> distributions = QueryExecutionUtils.executeList(
                q -> QueryExecutionFactory.create(q, fileCentricDataset), distQuery).stream()
                .map(x -> new ResourceInDatasetImpl(fileCentricDataset, x.getURI(), x).as(DcatDistribution.class))
                .collect(Collectors.toList());

        Path buildDir = Path.of("target").toAbsolutePath();;
        Files.createDirectories(buildDir);
        // Group datasets by their group id

        for (DcatDistribution d : distributions) {
            MavenEntity mvnEntity = d.as(MavenEntity.class);
            String artifactId = mvnEntity.getArtifactId();

            org.apache.maven.model.Model childPom = new org.apache.maven.model.Model();
            childPom.setModelVersion("4.0.0");
            childPom.setParent(parent);

            childPom.setArtifactId(artifactId);
            childPom.setGroupId(mvnEntity.getGroupId());
            childPom.setVersion(mvnEntity.getVersion());
            childPom.setPackaging("pom");


            Build build = new Build();

            if (false) {

                Resource resource = new Resource();
                resource.setDirectory("../../");
//
//                for (DcatDistribution dist : d.getBasicDistributions()) {
//                    String downloadUrl = dist.getDownloadUrl();
//                    if (downloadUrl != null) {
//                        resource.addInclude(downloadUrl);
//                    }
//                }

                build.addResource(resource);
            } else {

                Plugin plugin = BuildHelperUtils.createPlugin();

//                for (DcatDistribution dist : d.getBasicDistributions()) {
                MavenEntity distMvnEntity = d.as(MavenEntity.class);

                String downloadUrl = d.getDownloadUrl();
                if (downloadUrl != null) {
                    BuildHelperUtils.attachArtifact(plugin,
                            Path.of("../../").resolve(downloadUrl).toString(),
                            distMvnEntity.getType(), distMvnEntity.getClassifier());

                }
                build.addPlugin(plugin);
            }
            childPom.setBuild(build);


            // Get the graph having the same name as the dataset
            // copy it to the resources folder


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


/*
<plugin>
<groupId>org.codehaus.mojo</groupId>
<artifactId>build-helper-maven-plugin</artifactId>
<version>1.8</version>
<executions>
  <execution>
    <id>attach-artifacts</id>
    <phase>package</phase>
    <goals>
      <goal>attach-artifact</goal>
    </goals>
    <configuration>
      <artifacts>
        <artifact>
          <file>some file</file>
          <type>extension of your file </type>
          <classifier>optional</classifier>
        </artifact>
        ...
      </artifacts>
    </configuration>
  </execution>
</executions>
</plugin>
*/