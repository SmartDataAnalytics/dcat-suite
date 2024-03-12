package org.aksw.dcat_suite.cli.cmd.file;

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import org.aksw.commons.model.maven.domain.api.MavenEntityCore;
import org.aksw.commons.model.maven.domain.impl.MavenEntityCoreImpl;
import org.aksw.dcat.jena.domain.api.DcatDataset;
import org.aksw.dcat.jena.domain.api.DcatDistribution;
import org.aksw.dcat.jena.domain.api.MavenEntity;
import org.aksw.dcat.utils.DcatUtils;
import org.aksw.jenax.arq.dataset.api.DatasetOneNg;
import org.aksw.jenax.arq.dataset.impl.DatasetOneNgImpl;
import org.aksw.jenax.sparql.query.rx.RDFDataMgrEx;
import org.aksw.jenax.stmt.core.SparqlStmtMgr;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.update.UpdateExecutionFactory;
import org.apache.jena.update.UpdateRequest;
import org.apache.maven.model.Build;
import org.apache.maven.model.Parent;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;

import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

/**
 * Replace all downloadUrls pointing to files with corresponding <urn:mvn:> ids.
 *
 *
 *
 * @author raven
 *
 */
@Command(name = "metadata", separator = "=", description="Generate the maven project to publish dcat metadata", mixinStandardHelpOptions = true)
public class CmdDcatMvnizeMetadata
    implements Callable<Integer>
{
    @Parameters
    public List<String> dcatFiles = new ArrayList<>();

    protected String buildDirName = "target/metadata";


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
        Path baseDir = repo.getBasePath();

        Path buildDir = baseDir.resolve(buildDirName).toAbsolutePath();;
        Files.createDirectories(buildDir);
        // Group datasets by their group id


//        DcatRepoLocal repo = DcatRepoLocalUtils.findLocalRepo();
//
//        Path basePath = repo.getBasePath();
//        Dataset fileCentricDataset = repo.getDataset();

        for (String fileName : dcatFiles) {
            Dataset xds = RDFDataMgrEx.readAsGiven(DatasetFactory.create(), fileName);

            xds = CmdDcatFileFinalize.makeDatasetCentric(xds);

            RDFDataMgrEx.writeAsGiven(System.out, xds, RDFFormat.TRIG_BLOCKS, null);

            UpdateRequest ur = SparqlStmtMgr.loadSparqlStmts("dcat-download-to-mvn.rq").get(0).getAsUpdateStmt().getUpdateRequest();

            UpdateExecutionFactory.create(ur, xds).execute();

            // Model model = xds.getUnionModel();

            // Create the distribution graphs: For every distribution create a copy of all referenced file graphs
            // thereby replacing the file name with the distribution id.


            // Collect all graphs relevant to the datasets. These are all graphs
            // whose IRI starts with the dataset id or the distribution ids.

            Collection<DatasetOneNg> datasetGraphs = DcatUtils.listDatasetGraphs(xds);

            for (DatasetOneNg datasetGraph : datasetGraphs) {
                DcatDataset d = datasetGraph.getSelfResource().as(DcatDataset.class);
                Node datasetGraphNode = d.asNode();
                String datasetGraphIri = datasetGraphNode.getURI();

                Map<Node, Node> map = CmdDcatFileFinalize.listRelatedGraphs(xds, datasetGraphIri, datasetGraphIri);
                Set<Node> graphSet = new LinkedHashSet<>();
                graphSet.add(datasetGraphNode);
                graphSet.addAll(map.keySet());

                Dataset finalDataset = DatasetFactory.create();
                for (Node graph : graphSet) {
                    DatasetOneNg dong = DatasetOneNgImpl.create(xds, graph);
                    finalDataset.asDatasetGraph().addAll(dong.asDatasetGraph());
                }

                // Dataset ds = DatasetUtils.createFromResource(d);
                // DatasetOneNg ds = DatasetOneNgImpl.create(xds, d.getURI());
                // xds.getNamedModel(d.getURI()).as(DcatDataset.class);

                MavenEntity dsMvnId = d.as(MavenEntity.class);

                String artifactId = dsMvnId.getArtifactId();
                Path datasetFolder = buildDir.resolve(artifactId);
                Files.createDirectories(datasetFolder);

                MavenEntityCoreImpl metadataArtifact = new MavenEntityCoreImpl(dsMvnId);

                metadataArtifact.setArtifactId(metadataArtifact.getArtifactId()); // + "-dcat-metadata");
                metadataArtifact.setType("trig");

                String datasetFilename = MavenEntityCore.toFileName(dsMvnId);

                Path datasetFile = datasetFolder.resolve("dcat.trig");

                try (OutputStream out = Files.newOutputStream(datasetFile)) {
                    RDFDataMgrEx.writeAsGiven(out, finalDataset, RDFFormat.TRIG_BLOCKS, null);
                    out.flush();
                }


                org.apache.maven.model.Model childPom = new org.apache.maven.model.Model();
                childPom.setModelVersion("4.0.0");
                childPom.setParent(parent);

                childPom.setArtifactId(metadataArtifact.getArtifactId());
                childPom.setGroupId(metadataArtifact.getGroupId());
                childPom.setVersion(metadataArtifact.getVersion());
                childPom.setPackaging("pom");


                Build build = new Build();

                Plugin plugin = BuildHelperUtils.createPlugin();

                Model unionModel = xds.getUnionModel();
                DcatDataset dd = d.inModel(unionModel).as(DcatDataset.class);


//                for (DcatDistribution dist : dd.getBasicDistributions()) {
//                    MavenEntity mvnId = dist.as(MavenEntity.class);
//                    // TODO Generate dependencies?
//
//                    BuildHelperUtils.attachArtifact(plugin,
//                            datasetFile.getFileName().toString(), mvnId.getType(), mvnId.getClassifier());
//
//                }
                BuildHelperUtils.attachArtifact(plugin,
                        datasetFile.getFileName().toString(), "trig", null);
                build.addPlugin(plugin);
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
