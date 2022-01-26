package org.aksw.dcat_suite.cli.main;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.aksw.dcat.jena.domain.api.DcatDataset;
import org.aksw.dcat.jena.domain.api.DcatDistribution;
import org.aksw.dcat.jena.domain.api.MavenEntity;
import org.aksw.dcat.utils.DcatUtils;
import org.aksw.jena_sparql_api.conjure.datapod.api.RdfDataPod;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.Op;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpJobInstance;
import org.aksw.jena_sparql_api.conjure.dataset.engine.ExecutionUtils;
import org.aksw.jena_sparql_api.conjure.fluent.ConjureBuilderImpl;
import org.aksw.jena_sparql_api.conjure.fluent.JobUtils;
import org.aksw.jena_sparql_api.conjure.job.api.Job;
import org.aksw.jena_sparql_api.conjure.job.api.JobInstance;
import org.aksw.jena_sparql_api.conjure.resourcespec.RPIF;
import org.aksw.jena_sparql_api.conjure.utils.ContentTypeUtils;
import org.aksw.jena_sparql_api.http.domain.api.RdfEntityInfo;
import org.aksw.jena_sparql_api.rdf.collections.ResourceUtils;
import org.aksw.jenax.arq.util.node.NodeTransformLib2;
import org.aksw.jenax.arq.util.node.NodeTransformRenameMap;
import org.aksw.jenax.reprogen.core.JenaPluginUtils;
import org.apache.jena.atlas.lib.IRILib;
import org.apache.jena.ext.com.google.common.collect.Iterables;
import org.apache.jena.ext.com.google.common.collect.Iterators;
import org.apache.jena.ext.com.google.common.collect.Maps;
import org.apache.jena.ext.com.google.common.collect.Streams;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.DCAT;
import org.apache.jena.vocabulary.RDF;



public class DcatOps {

    public static Entry<String, String> parseEntry(String str) {
        String[] kv = str.split("=", 2);
        String k = kv[0];
        String v = kv[1];
        Entry<String, String> result = Maps.immutableEntry(k, v);

        return result;
    }

    public static void transformAllDists(Model model, Consumer<Resource> consumer) {
        Collection<DcatDataset> dcatDatasets = DcatUtils.listDcatDatasets(model);

        for(DcatDataset ds : dcatDatasets) {
            transformAllDists(ds, consumer);
        }
    }

    public static DcatDataset transformAllDists(DcatDataset source, Function<DcatDistribution, DcatDistribution> transform) {
        DcatDataset result = ModelFactory.createDefaultModel().createResource().as(DcatDataset.class);

        for(DcatDistribution dist : source.getDistributions()) {
            DcatDistribution newDist = transform.apply(dist);
            result.getModel().add(newDist.getModel());
            result.getDistributionsAs(DcatDistribution.class).add(newDist);
        }

//        for(DcatDataset ds : dcatDatasets) {
//            transformAllDists(ds, consumer);
//        }

        return result;
    }

    public static void transformAllDists(DcatDataset ds, Consumer<Resource> consumer) {
        for (DcatDistribution dist : ds.getDistributions()) {
            consumer.accept(dist);
        }
    }

    public static <T extends RDFNode> ExtendedIterator<T> listPolymorphicPropertyValues(Resource s, Property p, Class<T> clazz) {
        ExtendedIterator<T> result = ResourceUtils.listPropertyValues(s, p)
                .mapWith(o -> JenaPluginUtils.polymorphicCast(o, clazz))
                .filterKeep(Objects::nonNull);
        return result;
    }

    public static <T extends RDFNode> Optional<T> tryGetPolymorphicPropertyValue(Resource s, Property p, Class<T> clazz) {
        Optional<T> result = ResourceUtils.findFirst(listPolymorphicPropertyValues(s, p, clazz));
        return result;
    }

    public static <T extends RDFNode> T getPolymorphicPropertyValue(Resource s, Property p, Class<T> clazz) {
        T result = tryGetPolymorphicPropertyValue(s, p, clazz).orElse(null);
        return result;
    }



    public static Consumer<Resource> createDistMaterializer(Path targetFolder) {
        return dist -> {
//			Model xxx = org.apache.jena.util.ResourceUtils.reachableClosure(dist);
//			System.out.println(dist);
//			RDFDataMgr.write(System.out, xxx, RDFFormat.RDFJSON);
            Op op = getPolymorphicPropertyValue(dist, RPIF.op, Op.class);
            if(op != null) {
                // Check for content type and encoding
                RdfEntityInfo entityInfo = dist.as(RdfEntityInfo.class);
                String contentType = entityInfo.getContentType();
                String targetContentType = contentType;

                if(targetContentType == null) {
                    targetContentType = ResourceUtils.getLiteralPropertyValue(entityInfo, RPIF.targetContentType, String.class);
                }


                if(targetContentType == null) {
                    targetContentType = RDFFormat.TURTLE_PRETTY.getLang().getContentType().getContentTypeStr();
                }

                List<String> encodings = entityInfo.getContentEncodings();
//				if(encodings == null) {
//					encodings = ResourceUtils.getPropertyValue(entityInfo, RPIF.targetEncoding, RDFList.class);
//				}

                String ext = "." + ContentTypeUtils.toFileExtension(targetContentType, encodings);


                // Check if there is a targetFile attribute
                String targetBaseName = ResourceUtils.getLiteralPropertyValue(dist, RPIF.targetBaseName, String.class);

                if(targetBaseName == null) {
                    // TODO How to obtain a proper filename???
                    // The filename should be ${artifactId}-${version}.${format/encoding extension}

                    List<Resource> dss = ResourceUtils.listReversePropertyValues(dist, DCAT.distribution).toList();
                    if(dss.size() == 1) {
                        MavenEntity mvnEntity = dss.iterator().next().as(MavenEntity.class);
                        String artifactId = mvnEntity.getArtifactId();
                        String version = mvnEntity.getVersion();

                        if(artifactId != null) {
                            targetBaseName = artifactId + Optional.ofNullable(version).map(v -> "-" + v).orElse("");
                        }
                    }
                }

                // TODO We should keep track of all errors first and only then report them
//				if(targetBaseName == null) {
//					throw new RuntimeException("Could not obtain target base or file name for " + dist);
//				}

                // Probably use localId + file extension based on content type / encoding
                Path filename;
                if (targetBaseName != null) {
                    //String name = targetFolder.resolve(targetBaseName).getFileName().toString();
                    Path path = targetFolder.resolve(targetBaseName);
                    String filenameStr = path.getFileName().toString();
                    filename = path.resolveSibling(filenameStr + ext);
                } else {
                    try {
                        filename = Files.createTempFile(targetFolder, "file-", ext);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }

                // TODO Alternatively, use the localId

                try(OutputStream out = Files.newOutputStream(filename)) {
                    RdfDataPod dataPod = ExecutionUtils.executeJob(op);
                    Model model = dataPod.getModel();
                    RDFDataMgr.write(out, model, RDFFormat.TURTLE_PRETTY);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                String url = IRILib.fileToIRI(filename.toFile());
                Resource dl = dist.getModel().createResource(url);
                // Disconnect the op from the distribution
                dist.removeAll(RPIF.op);
                dist.addProperty(DCAT.downloadURL, dl);
            }
        };
    }

    public static Function<DcatDistribution, DcatDistribution> createDistMaterializerNew(Path targetFolder) {
        return dist -> {
            DcatDistribution r = null;
//			Model xxx = org.apache.jena.util.ResourceUtils.reachableClosure(dist);
//			System.out.println(dist);
//			RDFDataMgr.write(System.out, xxx, RDFFormat.RDFJSON);
            Op op = getPolymorphicPropertyValue(dist, RPIF.op, Op.class);
            if(op != null) {
                // Check for content type and encoding
                RdfEntityInfo entityInfo = dist.as(RdfEntityInfo.class);
                String contentType = entityInfo.getContentType();
                String targetContentType = contentType;

                if(targetContentType == null) {
                    targetContentType = ResourceUtils.getLiteralPropertyValue(entityInfo, RPIF.targetContentType, String.class);
                }


                if(targetContentType == null) {
                    targetContentType = RDFFormat.TURTLE_PRETTY.getLang().getContentType().getContentTypeStr();
                }

                List<String> encodings = entityInfo.getContentEncodings();
//				if(encodings == null) {
//					encodings = ResourceUtils.getPropertyValue(entityInfo, RPIF.targetEncoding, RDFList.class);
//				}

                String ext = "." + ContentTypeUtils.toFileExtension(targetContentType, encodings);


                // Check if there is a targetFile attribute
                String targetBaseName = ResourceUtils.getLiteralPropertyValue(dist, RPIF.targetBaseName, String.class);

                if(targetBaseName == null) {
                    // TODO How to obtain a proper filename???
                    // The filename should be ${artifactId}-${version}.${format/encoding extension}

                    List<Resource> dss = ResourceUtils.listReversePropertyValues(dist, DCAT.distribution).toList();
                    if(dss.size() == 1) {
                        MavenEntity mvnEntity = dss.iterator().next().as(MavenEntity.class);
                        String artifactId = mvnEntity.getArtifactId();
                        String version = mvnEntity.getVersion();

                        if(artifactId != null) {
                            targetBaseName = artifactId + Optional.ofNullable(version).map(v -> "-" + v).orElse("");
                        }
                    }
                }

                // TODO We should keep track of all errors first and only then report them
//				if(targetBaseName == null) {
//					throw new RuntimeException("Could not obtain target base or file name for " + dist);
//				}

                // Probably use localId + file extension based on content type / encoding
                Path filename;
                if (targetBaseName != null) {
                    //String name = targetFolder.resolve(targetBaseName).getFileName().toString();
                    Path path = targetFolder.resolve(targetBaseName);
                    String filenameStr = path.getFileName().toString();
                    filename = path.resolveSibling(filenameStr + ext);
                } else {
                    try {
                        filename = Files.createTempFile(targetFolder, "file-", ext);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }

                // TODO Alternatively, use the localId

                try(OutputStream out = Files.newOutputStream(filename)) {
                    RdfDataPod dataPod = ExecutionUtils.executeJob(op);
                    Model model = dataPod.getModel();
                    RDFDataMgr.write(out, model, RDFFormat.TURTLE_PRETTY);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                String url = IRILib.fileToIRI(filename.toFile());
                Model rModel = ModelFactory.createDefaultModel();
                //dist.getModel()
                // Disconnect the op from the distribution
                //r.removeAll(RPIF.op);
                r = rModel.createResource().as(DcatDistribution.class);
                r.setDownloadUrl(url);
            }
            return r;
        };
    }

    /**
     * Substitute each RDF term in subject position with a new blank node
     *
     * @param root
     * @return
     */
    public static RDFNode remapAllSubjects(RDFNode root) {
        Model model = root.getModel();

        Set<Node> allSubjects = Streams.stream(model.listSubjects())
                .map(RDFNode::asNode)
                .collect(Collectors.toSet());

        Map<Node, Node> remap = allSubjects.stream()
                .collect(Collectors.toMap(e -> e, e -> NodeFactory.createBlankNode()));

        RDFNode newRoot = NodeTransformLib2.applyNodeTransform(
                NodeTransformRenameMap.create(remap), root);

        return newRoot;
    }

    /**
     * Convenience method to apply a transformation by means of a sequence of SPARQL queries
     * to a distribution.
     *
     *
     * @param distribution
     * @param sparqlFile
     * @param targetFolder
     * @throws Exception
     */
    public static Consumer<Resource> createDistTransformer(
//			Resource distribution,
            List<String> sparqlFiles,
            Map<String, Node> env,
            Path targetFolder) throws Exception {

        // TODO Extend to multiple files
        String sparqlFile = Iterables.getOnlyElement(sparqlFiles);

        Job job = JobUtils.fromSparqlFile(sparqlFile);

        Consumer<Resource> result = distribution -> {
            // Copy the job into the distribution model
            // TODO Prevent repeatedly copying the job triples if the dists use the same model
            Job distJob = JenaPluginUtils.copyInto(job, Job.class, distribution.getModel());

            // Check whether this distribution qualifies for transform:
            // (.) it must either have a downloadURL, or
            // (.) it must have a rpif:op operation attached
            String downloadURL = org.aksw.dcat.jena.domain.api.DcatUtils.getFirstDownloadUrlFromDistribution(distribution);

            // If there is a downloadURL, create a DataRefUrl from it
            // otherwise, if there is an op, inject this one instead

            Op op;
            if(downloadURL != null) {
                op = ConjureBuilderImpl.start().fromUrl(downloadURL).getOp();
            } else {
                op = ResourceUtils.getLiteralPropertyValue(distribution, RPIF.op, Op.class);
            }

            if(op != null) {
                //DataRef dataRef = DataRefUrl.create(ModelFactory.createDefaultModel(), "http://foo.bar/baz");
    //			JenaSystem.init();

                Set<String> opVars = job.getOpVars();
                String opVar = Iterables.getOnlyElement(opVars);

                Map<String, Op> map = Collections.singletonMap(opVar, op);
                JobInstance ji = JobUtils.createJobInstanceWithCopy(distJob, env, map);

                OpJobInstance opInst = OpJobInstance.create(ji.getModel(), ji);

                // Note, that we do not materialize the job instance in order to retain
                // the original job
                // Op inst = JobUtils.materializeJobInstance(ji);

                // TODO Materializing an instance must allocate fresh resources instead of
                // performing in-place changes
                // Resource newRoot = remapAllSubjects(inst).asResource();

//				System.out.println(newRoot);
//				RDFDataMgr.write(System.out, newRoot.getModel(), RDFFormat.RDFJSON);
//
//				inst = JenaPluginUtils.polymorphicCast(newRoot, Op.class);

                // Copy the op instance into the distribution model
                distribution.getModel().add(opInst.getModel());

                Iterators.removeIf(
                        ResourceUtils.listPropertyValues(distribution, DCAT.downloadURL),
                        x -> true);
                Iterators.removeIf(
                        ResourceUtils.listPropertyValues(distribution, RPIF.op),
                        x -> true);

                distribution.addProperty(RPIF.op, opInst);
            }

        };

        return result;
        // This is the execution part that should be separate

//		Map<String, Node> env = ImmutableMap.<String, Node>builder()
//				.put("SOURCE_NS", NodeFactory.createLiteral("https://portal.limbo-project.org"))
//				.put("TARGET_NS", NodeFactory.createLiteral("https://data.limbo-project.org"))
//				.build();

//		String downloadUrl = org.aksw.dcat.ap.utils.DcatUtils.getFirstDownloadUrlFromDistribution(distribution);
//
//
//		try(RdfDataPod pod = ExecutionUtils.executeJob(inst)) {
//			Model m = pod.getModel();
//			System.out.println("Transformed Model:");
//			RDFDataMgr.write(System.out, m, RDFFormat.TURTLE_PRETTY);
//		}

        // TODO Where to write the data?

        // Convert the given sparql query to a conjure job

        // If the distribution is already a conjure distribution, extend upon it
        // Otherwise, wrap it as a one
        // JenaPluginUtils.copyClosureInto(rdfNode, viewClass, target)

    }


    public static Function<DcatDistribution, DcatDistribution> createDistTransformerNew(
//			Resource distribution,
            Job job,
            Map<String, Node> env) throws Exception {

        Function<DcatDistribution, DcatDistribution> result = distribution -> {
            DcatDistribution r = null;

            // Copy the job into the distribution model
            // TODO Prevent repeatedly copying the job triples if the dists use the same model

            // Check whether this distribution qualifies for transform:
            // (.) it must either have a downloadURL, or
            // (.) it must have a rpif:op operation attached
            String downloadURL = org.aksw.dcat.jena.domain.api.DcatUtils.getFirstDownloadUrlFromDistribution(distribution);

            // If there is a downloadURL, create a DataRefUrl from it
            // otherwise, if there is an op, inject this one instead

            Op op;
            if(downloadURL != null) {
                op = ConjureBuilderImpl.start().fromUrl(downloadURL).getOp();
            } else {
                op = ResourceUtils.getLiteralPropertyValue(distribution, RPIF.op, Op.class);
            }

            if(op != null) {
                Model rModel = ModelFactory.createDefaultModel();
                r = rModel.createResource().as(DcatDistribution.class);
                Job distJob = JenaPluginUtils.copyInto(job, Job.class, rModel);

                //DataRef dataRef = DataRefUrl.create(ModelFactory.createDefaultModel(), "http://foo.bar/baz");
    //			JenaSystem.init();

                Set<String> opVars = distJob.getOpVars();
                String opVar = Iterables.getOnlyElement(opVars);

                Map<String, Op> map = Collections.singletonMap(opVar, op);
                JobInstance ji = JobUtils.createJobInstance(distJob, env, map);

                OpJobInstance opInst = OpJobInstance.create(ji.getModel(), ji);

                // Note, that we do not materialize the job instance in order to retain
                // the original job
                // Op inst = JobUtils.materializeJobInstance(ji);

                // TODO Materializing an instance must allocate fresh resources instead of
                // performing in-place changes
                // Resource newRoot = remapAllSubjects(inst).asResource();

//				System.out.println(newRoot);
//				RDFDataMgr.write(System.out, newRoot.getModel(), RDFFormat.RDFJSON);
//
//				inst = JenaPluginUtils.polymorphicCast(newRoot, Op.class);

                // Copy the op instance into the distribution model
//                distribution.getModel().add(opInst.getModel());

//                Iterators.removeIf(
//                        ResourceUtils.listPropertyValues(distribution, DCAT.downloadURL),
//                        x -> true);
//                Iterators.removeIf(
//                        ResourceUtils.listPropertyValues(distribution, RPIF.op),
//                        x -> true);

                //distribution.addProperty(RPIF.op, opInst);
                r
                        .addProperty(RDF.type, DCAT.Distribution)
                        .addProperty(RPIF.op, opInst)
                        .as(DcatDistribution.class);

            }
            return r;
        };

        return result;
    }


    /**
     * Copy all referenced files into a target folder
     * Performs in-place update of download links
     *
     * @param dataset
     * @throws IOException
     */
    public static void createLocalCopyDataset(DcatDataset dataset, Path targetFolder) throws IOException {
        Files.createDirectories(targetFolder);

        Collection<DcatDistribution> dists = dataset.getDistributionsAs(DcatDistribution.class);
        for(DcatDistribution dist : dists) {
            for(String url : dist.getDownloadUrls()) {
                // TODO Reuse util function to resolve path to file
                // If we have a remote url, download it and get the filename
                // Use a flag for caching the downloads -

                Path src = null;
                Path tgt = null;
                Files.copy(src, tgt);
            }
        }
    }

    public static void createLocalCopy(DcatDistribution distribution, Path targetFolder) throws IOException {
        //return null;
    }


    // Apply encoding - actually this is just passing files / byte streams
    // to some function
    // ByteStreamProcessorFactory.accepts({hot-file, regular-file, input-stream})
    //   (actually a hot-file is more like an input stream however with an associated path)
    public static void sortNtriples() {

    }


    /**
     * pass each distribution to the provided command / script
     *
     */
    public static void applyTransform() {

    }
}
