package org.aksw.dcat_suite.cli.cmd.file;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

import org.aksw.dcat.ap.domain.api.Checksum;
import org.aksw.dcat.jena.domain.api.DcatDataset;
import org.aksw.dcat.jena.domain.api.DcatDistribution;
import org.aksw.dcat.jena.domain.api.MavenEntityCore;
import org.aksw.dcat.jena.domain.api.MavenEntityCoreImpl;
import org.aksw.jena_sparql_api.conjure.entity.engine.OpExecutor;
import org.aksw.jena_sparql_api.http.domain.api.RdfEntityInfo;
import org.aksw.jena_sparql_api.http.repository.api.HttpResourceRepositoryFromFileSystem;
import org.aksw.jena_sparql_api.http.repository.api.RdfHttpEntityFile;
import org.aksw.jena_sparql_api.http.repository.api.RdfHttpEntityFileSimple;
import org.aksw.jena_sparql_api.http.repository.impl.HttpResourceRepositoryFromFileSystemImpl;
import org.aksw.jena_sparql_api.http.repository.impl.HttpResourceRepositoryFromFileSystemImpl.Plan;
import org.aksw.jena_sparql_api.http.repository.impl.ResourceStoreImpl;
import org.aksw.jenax.sparql.query.rx.RDFDataMgrEx;
import org.apache.http.message.BasicHttpRequest;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.RDFLanguages;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.Invoker;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "get", separator = "=", description="Retrieve artifacts(s) from a mvn repository", mixinStandardHelpOptions = true)
public class CmdDcatMvnGet
    implements Callable<Integer>
{

    @Option(names= { "-r", "--remoteRepositories" })
    public List<String> remoteRepositories = new ArrayList<>();

    @Option(names= { "-P" }, description = "Maven profiles to activate")
    public List<String> profiles = new ArrayList<>();

    @Parameters
    protected List<String> artifacts;

    public Integer call() throws Exception {

        Invoker invoker = new DefaultInvoker();
        Path templatePomXml = DcatRepoLocalMvnUtils.getDefaultPomTemplate();
        ArtifactResolver artifactResolver = MavenUtils.createDefaultArtifactResolver(invoker, templatePomXml, profiles, remoteRepositories);


        for (String artifact : artifacts) {

            // Check for a '-dcat-metadata' artifact
            MavenEntityCore mvnId = MavenEntityCore.parse(artifact);
            MavenEntityCore raw = new MavenEntityCoreImpl(mvnId);

            String rawId = "urn:mvn:" + MavenEntityCore.toString(raw);

            mvnId.setArtifactId(mvnId.getArtifactId() + "-dcat-metadata");

            String fileExt = "trig";
            mvnId.setType(fileExt);

            System.out.println(MavenEntityCore.toString(mvnId));

            String physicalMvnId = MavenEntityCore.toString(mvnId);

            ArtifactResolutionRequest req = new ArtifactResolutionRequestImpl();
            req.setArtifactId(physicalMvnId);

            Path path = artifactResolver.resolve(req);
//            if (!Files.exists(path)) {
//                throw new NoSuchFileException(path.toString());
//            }

            Dataset dataset = DatasetFactory.create();
            Lang lang = RDFLanguages.fileExtToLang(fileExt);
            try (InputStream in = new BufferedInputStream(Files.newInputStream(path))) {
                // TypedInputStream tin = RDFDataMgrEx.probeLang(in, RDFDataMgrEx.DEFAULT_PROBE_LANGS);
                // Lang lang = RDFLanguages.contentTypeToLang(tin.getMediaType());

                System.out.println("Lang is " + lang);

                RDFDataMgrEx.readAsGiven(dataset, in, lang);
            }


            System.out.println(path);
            HttpResourceRepositoryFromFileSystem repo = null;
            // repo.get
            BasicHttpRequest request = HttpResourceRepositoryFromFileSystemImpl.createRequest(artifact, "application/turtle", Arrays.asList("bzip2"));

            DcatDataset dcatDataset = dataset.getNamedModel(rawId).getResource(rawId).as(DcatDataset.class);
            List<RdfHttpEntityFile> entities = new ArrayList<>();

            for (DcatDistribution dist : dcatDataset.getBasicDistributions()) {
                RdfEntityInfo httpEntity = dist.as(RdfEntityInfo.class);
                Checksum c = httpEntity.getModel().createResource().as(Checksum.class);
                c.setAlgorithm("sha256");
                c.setChecksum(Hashing.sha256().hashString("test", StandardCharsets.UTF_8).toString());

                httpEntity.getHashes().add(c);

                RdfHttpEntityFile entity = new RdfHttpEntityFileSimple(Path.of(dist.getDownloadUrl()), httpEntity);
                entities.add(entity);
            }

            HttpResourceRepositoryFromFileSystemImpl httpRepo = new HttpResourceRepositoryFromFileSystemImpl();
            Path ppath = HttpResourceRepositoryFromFileSystemImpl.getDefaultPath();
            ResourceStoreImpl hashStore = new ResourceStoreImpl(ppath.resolve("hash"), HttpResourceRepositoryFromFileSystemImpl::hashToRelPath);

            OpExecutor opExector = new OpExecutor(httpRepo, hashStore);
            // OpUtils.optimize(op, hasher, hashStore);

            Plan plan = HttpResourceRepositoryFromFileSystemImpl.findBestPlanToServeRequest(
                request,
                entities,
                opExector
            );

            RDFDataMgrEx.writeAsGiven(System.out, plan.getOp().getModel(), RDFFormat.TURTLE_BLOCKS, null);


            RDFDataMgrEx.writeAsGiven(System.out, dataset, RDFFormat.TRIG_BLOCKS, null);
        }

        return 0;
    }
}
