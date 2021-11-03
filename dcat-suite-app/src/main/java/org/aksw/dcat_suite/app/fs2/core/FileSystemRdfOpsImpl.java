package org.aksw.dcat_suite.app.fs2.core;

import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.aksw.commons.io.util.symlink.SymbolicLinkStrategies;
import org.aksw.difs.builder.DifsFactory;
import org.aksw.difs.index.impl.RdfTermIndexerFactoryIriToFolder;
import org.aksw.difs.system.domain.StoreDefinition;
import org.aksw.jenax.arq.engine.quad.RDFConnectionFactoryQuadForm;
import org.aksw.jenax.arq.util.syntax.QueryUtils;
import org.aksw.jenax.arq.util.update.UpdateRequestUtils;
import org.aksw.jenax.arq.util.var.Vars;
import org.aksw.jenax.reprogen.core.JenaPluginUtils;
import org.aksw.jenax.sparql.query.rx.SparqlRx;
import org.aksw.jenax.stmt.parser.update.SparqlUpdateParser;
import org.aksw.jenax.stmt.parser.update.SparqlUpdateParserImpl;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.graph.NodeTransform;
import org.apache.jena.sparql.syntax.syntaxtransform.NodeTransformSubst;
import org.apache.jena.system.Txn;
import org.apache.jena.update.UpdateRequest;
import org.apache.jena.vocabulary.RDFS;

import com.google.common.base.Preconditions;

import io.reactivex.rxjava3.core.Flowable;


/**
 * A file system comprised of two rooted file systems - one for the actual files
 * and another for an file-based rdf store. The rdf store is used store file
 * annotations, such as content type, schemas / standard conformances, relations to
 * other files, etc.
 *
 * A file in the RDF store is represented using a uuid which is used as an IRI.
 * The entry of the uuri points to the location of the file.
 * Moving a file by default only updates the entry in the uuid record
 * rather than renaming references across the whole RDF store.
 *
 * <pre>
 * <a-b-c-d> {
 *   <a-b-c-d> :path <relative/path/to/file> .
 * }
 *
 * <a-b-c-d/customAnnotation> {
 *   <a-b-c-d/customAnnotation>
 *     :annotationTarget <a-b-c-d> .
 * }
 * </pre>
 *
 *
 * @author raven
 *
 */
public class FileSystemRdfOpsImpl
    implements FileSystemRdfOps
{
    public static void main(String[] args) throws Exception {
        main2(args);
    }

    public static void mainCleanStore(String[] args) throws Exception {
        Path storePath = Paths.get("/tmp/repo");
        Files.createDirectories(storePath);

        Dataset dataset = initFsRdfStore(storePath);
    }

    public static void main2(String[] args) throws Exception {
        JenaPluginUtils.registerResourceClasses(RdfFileObject.class, RdfAnnotation.class);

        Path storePath = Paths.get("/tmp/repo");
        Files.createDirectories(storePath);

        Dataset dataset = initFsRdfStore(storePath);
        Path repoRoot = Paths.get("/tmp");

        FileSystemRdfOpsImpl fs = new FileSystemRdfOpsImpl(dataset, repoRoot);


        Flowable.fromStream(Files.walk(repoRoot)
                // .map(path -> repoRoot.resolve(path))
                .filter(Files::isRegularFile)
                .filter(path -> !path.startsWith(storePath)))
            .forEach(path -> fs.mutateAnnotation(path, "test", res -> res.addProperty(RDFS.label, "yay")));

    }

    public static void main3(String[] args) throws Exception {
        JenaPluginUtils.registerResourceClasses(RdfFileObject.class, RdfAnnotation.class);

        if (false) {
            UpdateRequest ur = setPath("uuid", "newPath");
            System.out.println(ur);
            return;
        }

        Path tmpFile = Paths.get("/tmp/test.txt");


        if (!Files.exists(tmpFile)) {
            Files.createFile(tmpFile);
        }

        Path storePath = Paths.get("/tmp/repo");
        Files.createDirectories(storePath);


        Dataset dataset = initFsRdfStore(storePath);
        Path repoRoot = Paths.get("/tmp");

        FileSystemRdfOpsImpl fs = new FileSystemRdfOpsImpl(dataset, repoRoot);

        Path importedFile = fs.importFile(tmpFile, storePath, false);
        Path moveTgt = importedFile.resolveSibling("yay.txt");

        String uuid = fs.getUuid(importedFile);
        System.out.println("Import uuid = " + uuid);

        fs.move(importedFile, moveTgt);

        Txn.executeWrite(dataset, () -> {
            Resource ann = fs.getAnnotation(uuid, "myQualifier");
            ann.addProperty(RDFS.label, "yay");
        });

        fs.delete(moveTgt);
    }


    public static Dataset initFsRdfStore(Path repoRoot) throws IOException {
        StoreDefinition sd = ModelFactory.createDefaultModel().createResource().as(StoreDefinition.class)
                .setStorePath("store")
                .setIndexPath("index")
                .addIndex("http://www.example.org/path", "path", RdfTermIndexerFactoryIriToFolder.class);

        DatasetGraph dg = DifsFactory.newInstance()
                .setStoreDefinition(sd)
                .setUseJournal(true)
                .setSymbolicLinkStrategy(SymbolicLinkStrategies.FILE)
                .setConfigFile(repoRoot.resolve("store.conf.ttl"))
                .setMaximumNamedGraphCacheSize(10000)
                .connect();
        Dataset d = DatasetFactory.wrap(dg);
        return d;
    }


    protected Dataset dataset;
    protected RDFConnection conn;
    protected Path repoRoot;

    public FileSystemRdfOpsImpl(Dataset dataset, Path repoRoot) {
        super();
        this.dataset = dataset;
        this.conn = RDFConnectionFactoryQuadForm.connect(dataset);
        this.repoRoot = repoRoot;
    }


    public static Path resolveWithin(Path given, Path prefix) {
        Path resolved = prefix.resolve(given).normalize();
        if (!resolved.startsWith(prefix)) {
            throw new IllegalArgumentException("Target path is not within the repository at " + prefix + "; target " + given + " resolved to " + resolved);
        }
        return resolved;
    }

    /** Resolve a path against the given prefix path and require the result to start with prefix */
    public static Path relativizeWithin(Path given, Path prefix) {
        Path resolved = resolveWithin(given, prefix);
        Path result = prefix.relativize(resolved);
        return result;
    }

    @Override
    public Path importFile(Path srcFile, Path tgtPath, boolean deleteSource) throws IOException {
        if (!Files.isRegularFile(srcFile)) {
            throw new IllegalArgumentException("Not a regular file: " + srcFile);
        }

        Path effectiveTgtPath = repoRoot.resolve(tgtPath).normalize();

        if (Files.isDirectory(effectiveTgtPath)) {
            effectiveTgtPath = effectiveTgtPath.resolve(srcFile.getFileName());
        }

        if (!effectiveTgtPath.startsWith(repoRoot)) {
            throw new IllegalArgumentException("Target path is not within the repository at " + repoRoot + "; target " + tgtPath + " resolved to " + effectiveTgtPath);
        }


        Path relPath = repoRoot.relativize(effectiveTgtPath);


        // TODO We should use the txn manager to lock the target path
        // But this would require additional efforts to manually handle txns outside of the dataset context


        if (deleteSource) {
            Files.move(srcFile, effectiveTgtPath);
        } else {
            Files.copy(srcFile, effectiveTgtPath);
        }

        allocateUuid(relPath);


        Path result = repoRoot.relativize(effectiveTgtPath);
        return result;
    }


    public String allocateUuid(Path relPath) {
        String pathStr = relPath.toString();

        // Allocate a UUID for the path
        UUID uuid = UUID.randomUUID();

        Txn.executeWrite(dataset, () -> {
            String uuidStr = uuid.toString();
            Model model = dataset.getNamedModel(uuidStr);
            model.createResource(uuidStr).as(RdfFileObject.class).setPath(pathStr);
        });

        return uuid.toString();
    }


    /**
     * Move a file within the filesystem and
     * updates the path in the files' UUID record
     *
     * Both the srcFile and the tgtPath must be within the same repository
     * @throws IOException
     *
     */
    @Override
    public void move(Path srcFile, Path tgtPath, CopyOption... options) throws IOException {

        Path absSrcFile = resolveWithin(srcFile, repoRoot);
        Path absTgtFile = resolveWithin(tgtPath, repoRoot);
        Preconditions.checkArgument(Files.exists(absSrcFile), "src does not exist: " + absSrcFile);

        Path src = repoRoot.relativize(absSrcFile);
        Path tgt = repoRoot.relativize(absTgtFile);

        String srcUuid = getUuid(src);

        // TODO Self-heal: Create a new uuid; optionally try to relink / clean up dangling references
        Objects.requireNonNull(srcUuid, "No uuid assigned to srcFile; repo corrupted?");

        deleteIfExists(tgt);

        Files.move(absSrcFile, absTgtFile, options);

        String after = tgt.toString();
        UpdateRequest ur = setPath(srcUuid, after);
        System.out.println("Update: " + ur);
        Txn.executeWrite(conn, () -> conn.update(ur));
    }


    /*
    // @QueryString("SELECT * { ?s ?p ?o }")
    // @Source("my/query.rq")
    interface MyQueryTemplate extends SparqlQueryTemplate {
        default String getBaseString() {
            return "SELECT * { ?s ?p ?o }";
        }

        @QueryParam("s")
        QueryParam getSubject();
    }
    */



    /**
     * Delete a file and all related metadata by uuid.
     *
     * Deletes all graphs starting with uuid - e.g. abcd/customAnnotations
     *
     * @param uuid
     */
    public void deleteGraphsByUuid(String uuid) {
        // String queryStr = "DELETE { GRAPH ?g { ?s ?p ?o } } WHERE { ?uuid fsrdf:decendents ?g } }";
        SparqlUpdateParser parser = SparqlUpdateParserImpl.createAsGiven();
        //UpdateRequest ur = parser.apply("DELETE { GRAPH ?g { ?s ?p ?o } } WHERE { { GRAPH ?g { ?s <http://www.example.org/annotationTarget> ?x } } UNION { BIND(?x AS ?g) } }");
        UpdateRequest ur = parser.apply("DELETE { GRAPH ?g { ?s ?p ?o } } WHERE { { GRAPH ?g { [] <http://www.example.org/annotationTarget> ?x } } UNION { BIND(?x AS ?g) } GRAPH ?g { ?s ?p ?o } }");

        Map<Var, Node> map = new HashMap<>();
        map.put(Vars.x, NodeFactory.createURI(uuid));
        NodeTransform xform = new NodeTransformSubst(map);

        UpdateRequest result = UpdateRequestUtils.applyNodeTransform(ur, xform);
        System.out.println(result);
        Txn.executeWrite(conn, () -> conn.update(result));
    }

    public boolean deleteIfExists(Path path) throws IOException {
        boolean result = false;
        String uuid = getUuid(path);

        if (uuid != null) {
            deleteGraphsByUuid(uuid);

            Path absPath = repoRoot.resolve(path);
            result = Files.deleteIfExists(absPath);
        }
        return result;
    }

    public void delete(Path path) throws IOException {
        String uuid = getUuid(path);

        if (uuid != null) {
            deleteGraphsByUuid(uuid);

            Path absPath = repoRoot.resolve(path);
            Files.delete(absPath);
        }
    }

    public String getUuid(Path path) {
        Path effectiveTgtPath = relativizeWithin(path, repoRoot);
        String pathStr = effectiveTgtPath.toString();

        String result = getUuid(pathStr);
        return result;
    }

    /**
     * DOES NOT RETAIN RELATIVE IRIs.
     *
     * Update the value of subject s for predicate p to newValue in graph g
     * Deletes all triples matching (g, s, p, ?) and inserts (g, s, p, newValue).
     *
     * @param g
     * @param s
     * @param p
     * @param newValue
     */
    public static UpdateRequest setValueOld(Node g, Node s, Node p, Node newValue) {
        ParameterizedSparqlString ps = new ParameterizedSparqlString("DELETE { GRAPH ?g { ?s ?p ?oldO } } INSERT { GRAPH ?g { ?s ?p ?newO } } WHERE { GRAPH ?g { ?s ?p ?oldO } }");
        ps.setParam("g", g);
        ps.setParam("s", s);
        ps.setParam("p", p);
        ps.setParam("newO", newValue);
        UpdateRequest result = ps.asUpdate();
        return result;
    }

    public static UpdateRequest setValue(Node g, Node s, Node p, Node newValue) {
        SparqlUpdateParser parser = SparqlUpdateParserImpl.createAsGiven();
        UpdateRequest ur = parser.apply("DELETE { GRAPH ?g { ?s ?p ?oldO } } INSERT { GRAPH ?g { ?s ?p ?o } } WHERE { GRAPH ?g { ?s ?p ?oldO } }");
        Map<Var, Node> map = new HashMap<>();
        map.put(Vars.g, g);
        map.put(Vars.s, s);
        map.put(Vars.p, p);
        map.put(Vars.o, newValue);
        NodeTransform xform = new NodeTransformSubst(map);

        UpdateRequest result = UpdateRequestUtils.applyNodeTransform(ur, xform);
        return result;
    }

    public static UpdateRequest setPath(String uuid, String pathStr) {
        UpdateRequest result = setValue(
                NodeFactory.createURI(uuid),
                null,
                NodeFactory.createURI("http://www.example.org/path"),
                NodeFactory.createURI(pathStr));
        return result;
    }


    public Resource getAnnotation(String uuid, String qualifier) {
        String qualifiedName = qualifier == null ? uuid : uuid + "/" + qualifier;

        Model model = dataset.getNamedModel(qualifiedName);
        Resource result = model.createResource(qualifiedName);

        RdfAnnotation ann = result.as(RdfAnnotation.class);
        if (ann.getAnnotationTarget() == null) {
            Txn.executeWrite(dataset, () -> ann.setAnnotationTarget(uuid));
        }

        return result;
    }

    public String getUuid(String pathStr) {
        String str = "SELECT ?uuid { GRAPH ?uuid { ?s <http://www.example.org/path> ?path } }";
        Query query = QueryFactory.create(str);

        String result = Txn.calculateRead(dataset, () -> {
            Query q = query.cloneQuery();
            QueryUtils.injectFilter(q, "?path = <" + pathStr + ">");

            System.out.println(q);


            Node node = SparqlRx.execConceptRaw(conn, q, Var.alloc("uuid")).firstElement().blockingGet();
            return node == null ? null : node.getURI();
        });
        return result;
    }


    @Override
    public Resource getAnnotation(Path path, String qualifier, boolean createIfNotExists) {
        Path absPath = resolveWithin(path, repoRoot);

        Path relPath = repoRoot.relativize(absPath);

        // TODO Lock relPath in the txnMgr to prevent concurrent annotation generation

        String uuid = getUuid(relPath);
        if (uuid == null && createIfNotExists) {
            uuid = allocateUuid(relPath);
        }

        Resource result = uuid == null ? null : getAnnotation(uuid, qualifier);
        return result;
    }



}
