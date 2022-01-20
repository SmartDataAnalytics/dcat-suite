package org.aksw.dcat_suite.cli.cmd.file;

import java.io.OutputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.aksw.commons.io.util.StdIo;
import org.aksw.dcat.jena.domain.api.DcatDataset;
import org.aksw.dcat.jena.domain.api.DcatDistribution;
import org.aksw.dcat.jena.domain.api.MavenEntity;
import org.aksw.jena_sparql_api.common.DefaultPrefixes;
import org.aksw.jena_sparql_api.conjure.utils.ContentTypeUtils;
import org.aksw.jena_sparql_api.http.domain.api.RdfEntityInfo;
import org.aksw.jena_sparql_api.http.domain.api.RdfEntityInfoDefault;
import org.aksw.jenax.arq.util.streamrdf.StreamRDFDeferred;
import org.aksw.jenax.arq.util.streamrdf.StreamRDFWriterEx;
import org.aksw.jenax.reprogen.core.MapperProxyUtils;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.vocabulary.DCAT;
import org.apache.jena.vocabulary.DCTerms;
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

    @Option(names= {"-g", "--groupId"}, arity = "1", required = true)
    public String groupId;

    /** Version for the transform to be created */
    @Option(names= {"-v", "--version"}, arity = "1", required = true)
    public String version;

    @Override
    public Integer call() throws Exception {
        DcatRepoLocal repo = DcatRepoLocalUtils.findLocalRepo(Path.of(""));

        Dataset repoDs = repo.getDataset();
        repoDs.begin(ReadWrite.WRITE);

        for (String file : files) {
            Path filePath = Path.of(file).normalize();
            RdfEntityInfo entityInfo = DcatRepoLocalUtils.probeFile(repo.getBasePath(), filePath);

            // Derive the base name; remove file extensions
            String baseName = DcatRepoLocalUtils.deriveBaseName(file, entityInfo, true);

            // String datasetId = "#" + groupId.replace('.', '/') + "/" + baseName + "/" + version;
            String datasetId = groupId + ":" + baseName + ":" + version;
            String datasetIri = "#" + datasetId;

            Dataset ds = DatasetFactory.create();
            DcatDataset dcatDataset = ds.getNamedModel(datasetIri).createResource(datasetIri).as(DcatDataset.class);
            dcatDataset.addProperty(RDF.type, DCAT.Dataset);

            MavenEntity mvn = dcatDataset.as(MavenEntity.class);

            mvn.setGroupId(groupId);
            mvn.setVersion(version);
            mvn.setArtifactId(baseName);
            mvn.addProperty(DCTerms.identifier, datasetId);



            String distributionType = ContentTypeUtils.toFileExtension(entityInfo);
            // Cut off a preceding dot
            if (distributionType.startsWith(".")) {
                distributionType = distributionType.substring(1);
            }

            String distributionIri = "#" + datasetId + ":" + distributionType;
            DcatDistribution dcatDistribution = ds.getNamedModel(filePath.toString()).createResource(distributionIri)
                    .as(DcatDistribution.class);

            dcatDistribution.addProperty(RDF.type, DCAT.Distribution);
            dcatDistribution.setDownloadUrl(filePath.toString());

            RdfEntityInfo tgt = dcatDistribution.as(RdfEntityInfo.class);
            copyEntityInfo(tgt, entityInfo);

            //MapperProxyUtils.s
            MapperProxyUtils.skolemize("", dcatDistribution.as(RdfEntityInfoDefault.class),
                    map -> map.remove(RDF.nil));
            // MapperProxyUtils.

            dcatDataset.inModel(dcatDistribution.getModel()).as(DcatDataset.class)
                .getDistributions(DcatDistribution.class).add(dcatDistribution);


            ds.asDatasetGraph().find().forEachRemaining(repoDs.asDatasetGraph()::add);

            if (false) {
                try (OutputStream out = StdIo.openStdOutWithCloseShield()) {
                    StreamRDFWriterEx.writeAsGiven(ds.asDatasetGraph(), out, RDFFormat.TRIG_BLOCKS, null,
                            sr -> new StreamRDFDeferred(sr, true, DefaultPrefixes.get(), 1000, 1000, null));
                }
            }
        }

        repoDs.commit();

        return 0;
    }


    public static void copyEntityInfo(RdfEntityInfo tgt, RdfEntityInfo src) {
        tgt
            .setCharset(src.getCharset())
            .setContentEncodings(src.getContentEncodings())
            .setContentType(src.getContentType())
            .setByteSize(src.getByteSize())
            .setUncompressedByteSize(src.getUncompressedByteSize())
            ;
    }

}
