package org.aksw.dcat_suite.cli.cmd.file;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.commons.io.util.StdIo;
import org.aksw.commons.txn.api.Txn;
import org.aksw.commons.util.string.FileName;
import org.aksw.commons.util.string.FileNameUtils;
import org.aksw.dcat.jena.conf.api.DcatRepoConfig;
import org.aksw.dcat.jena.domain.api.DcatDataset;
import org.aksw.dcat.jena.domain.api.DcatDistribution;
import org.aksw.dcat.jena.domain.api.MavenEntity;
import org.aksw.jena_sparql_api.common.DefaultPrefixes;
import org.aksw.jena_sparql_api.conjure.utils.ContentTypeUtils;
import org.aksw.jena_sparql_api.http.domain.api.RdfEntityInfo;
import org.aksw.jenax.arq.util.streamrdf.StreamRDFDeferred;
import org.aksw.jenax.arq.util.streamrdf.StreamRDFWriterEx;
import org.aksw.jenax.reprogen.core.MapperProxyUtils;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.vocabulary.DCAT;
import org.apache.jena.vocabulary.RDF;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

/** Add a file to the current dcat catalog */
@Command(name = "add", separator = "=", description="Add a file to the local dcat repository", mixinStandardHelpOptions = true)
public class CmdDcatFileAdd
    implements Callable<Integer>
{
    @Parameters(description = "Files to add as datasets to the current dcat repository")
    public List<String> files = new ArrayList<>();

    /** Default group id can be read from the dcat repo conf file */
    @Option(names= {"-g", "--groupId"}, arity = "1")
    public String groupId = null;

    @Option(names= {"-a", "--artifactId"}, arity = "1", description = "Artifact ID. Defaults to base file name (known file extensions are removed)", required = false)
    public String artifactId = null;

    /** Version for the file being added - if absent use its modified date */
    @Option(names= {"-v", "--version"}, arity = "1", description = "Version to assign to a dataset derived from a file. Defaults to the file's local date, e.g. 2020-12-31", required = false)
    public String version = null;


    @Option(names= {"--dataset" }, description = "Generate the dataset part")
    public boolean dataset = false;

    @Option(names= {"--content" }, description = "Generate the content part")
    public boolean content = false;


    @Override
    public Integer call() throws Exception {

        // If neither dataset nor content was explicitly requested then activate both
        if (!dataset && !content) {
            dataset = content = true;
        }

        if (artifactId != null && files.size() > 1) {
            throw new IllegalArgumentException("Cannot add multiple files if an artifactId is specified");
        }

        DcatRepoLocal repo = DcatRepoLocalUtils.findLocalRepo(Path.of(""));

        // Try to get a group from the properties if it is null
        if (groupId == null) {
            groupId = repo.getMemConfig().as(DcatRepoConfig.class).getProperties().get("groupId");
        }

        Dataset repoDs = repo.getDataset();
        org.apache.jena.system.Txn.executeWrite(repoDs, () -> {
            for (String file : files) {
                try {
                    addFile(repo, repoDs, file);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        return 0;
    }

    public void addFile(DcatRepoLocal repo, Dataset repoDs, String file) throws IOException {
        Path filePath = Path.of(file).normalize();
        RdfEntityInfo entityInfo = DcatRepoLocalUtils.probeFile(repo.getBasePath(), filePath);

        if (version == null) {
            FileTime fileTime = Files.getLastModifiedTime(filePath);
            if (fileTime == null) {
                throw new RuntimeException("Could not derive a version from file's last modified date: " + filePath);
            }
            LocalDate localDate = LocalDate.ofInstant(fileTime.toInstant(), ZoneId.systemDefault());
            version = localDate.toString() + ".1-SNAPSHOT";
        }

        // TODO Convert entity info to file extension
        // Fallback: Use the file extension of the supplied file
        FileName fileName = FileNameUtils.deriveFileName(file, entityInfo);
        String type = Stream.concat(Stream.of(fileName.getContentPart()), fileName.getEncodingParts().stream())
                .collect(Collectors.joining("."));

        // GraphEntityUtils.getOrCreateModel(repoDs, null)

        // Derive the base name; remove file extensions
        String baseName = artifactId != null
                ? artifactId
                : fileName.getBaseName().replace('.', '-');

        // String prefix = "#";
        String prefix = "urn:mvn:";

        // String datasetId = "#" + groupId.replace('.', '/') + "/" + baseName + "/" + version;
        String datesetArtifactId = baseName + "-dataset";

        String datasetId = groupId + ":" + datesetArtifactId + ":" + version;
        String datasetIri = prefix + datasetId;

        Dataset ds = DatasetFactory.create();
        //DcatDataset dcatDataset = GraphEntityUtils.newModelByTimestamp(ds).getSelfResource().as(DcatDataset.class);


        DcatDataset dcatDataset = ds.getNamedModel(datasetIri).createResource(datasetIri).as(DcatDataset.class);
//            DatasetOneNg dong = GraphEntityUtils.getOrCreateModel(ds,
//                    NodeFactory.createLiteral(mvn.getGroupId()),
//                    NodeFactory.createLiteral(mvn.getArtifactId()),
//                    NodeFactory.createLiteral(mvn.getVersion())
//            );
//            dong.getSelfResource().as(DcatDistribution.class)
//                .setDownloadUrl(filePath.toString());



        dcatDataset.addProperty(RDF.type, DCAT.Dataset);

        MavenEntity mvn = dcatDataset.as(MavenEntity.class);

        mvn.setGroupId(groupId);
        mvn.setVersion(version);
        mvn.setArtifactId(datesetArtifactId);
        // mvn.addProperty(DCTerms.identifier, datasetId);



        String fn = filePath.toString();
        Resource fileContent = ds.getNamedModel(fn).createResource(fn);
        RdfEntityInfo tgt = fileContent.as(RdfEntityInfo.class);
        RdfEntityInfo.copy(tgt, entityInfo);

        // __ is the marker for annotations about whatever the identifier before the __ refers to
        MapperProxyUtils.skolemize(fn + "__", tgt, map -> map.remove(RDF.nil));



        String distributionType = ContentTypeUtils.toFileExtension(entityInfo);
        // Cut off a preceding dot
        if (distributionType.startsWith(".")) {
            distributionType = distributionType.substring(1);
        }


        String distributionArtifactId = baseName;
        String distributionId = groupId + ":" + distributionArtifactId + ":" + version;
        String distributionIri = prefix + distributionId + ":" + distributionType;
        String distributionGraphIri = distributionIri; // filePath.toString()
        DcatDistribution dcatDistribution = ds.getNamedModel(distributionGraphIri).createResource(distributionIri)
                .as(DcatDistribution.class);

        //DcatDistribution dcatDistribution = GraphEntityUtils.newModelByTimestamp(ds).getSelfResource().as(DcatDistribution.class);


        dcatDistribution.addProperty(RDF.type, DCAT.Distribution);
        dcatDistribution.setDownloadUrl(filePath.toString());

        MavenEntity mvnDistribution = dcatDistribution.as(MavenEntity.class);

        mvnDistribution.setGroupId(groupId);
        mvnDistribution.setVersion(version);
        mvnDistribution.setArtifactId(baseName);
        mvnDistribution.setType(type);


//        MapperProxyUtils.skolemize(distributionIri + "/", dcatDistribution.as(RdfEntityInfo.class),
//                map -> map.remove(RDF.nil));

        dcatDataset.inModel(dcatDistribution.getModel()).as(DcatDataset.class)
            .getDistributionsAs(DcatDistribution.class).add(dcatDistribution);


        ds.asDatasetGraph().find().forEachRemaining(repoDs.asDatasetGraph()::add);

        if (false) {
            try (OutputStream out = StdIo.openStdOutWithCloseShield()) {
                StreamRDFWriterEx.writeAsGiven(ds.asDatasetGraph(), out, RDFFormat.TRIG_BLOCKS, null,
                        sr -> new StreamRDFDeferred(sr, true, DefaultPrefixes.get(), 1000, 1000, null));
            }
        }
    }
}
