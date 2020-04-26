package org.aksw.dcat_suite.service;

import static org.apache.jena.query.ReadWrite.WRITE;
import static org.apache.jena.system.Txn.executeWrite;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Semaphore;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.aksw.jena_sparql_api.io.common.Reference;
import org.aksw.jena_sparql_api.io.common.ReferenceImpl;
import org.apache.commons.io.input.CloseShieldInputStream;
import org.apache.commons.io.output.CloseShieldOutputStream;
import org.apache.jena.ext.com.google.common.cache.Cache;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.TxnType;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.shared.Lock;
import org.apache.jena.shared.LockMRPlusSW;
import org.apache.jena.sparql.JenaTransactionException;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.core.DatasetGraphWrapper;
import org.apache.jena.sparql.core.Transactional;


/**
 * Lock policy controls on which granularity to lock the underlying store.
 *
 * @author raven
 *
 */
enum LockPolicy {
    /**
     * Lock and unlock on every transaction
     *
     */
    TRANSACTION,

    /**
     * Aquire a lock for the lifetime of the graph object.
     * Hence, eventually calling graph.close() is essential.
     *
     * Every write transaction will still cause writing to the store
     * (which means rewriting the whole file),
     * but the inter-process lock only has to be aquired once instead of
     * on every transaction
     *
     */
    LIFETIME,

    /**
     * Similar to graph, except that the graph-lifetime lock is only aquired
     * on the first transaction
     *
     */
    LIFETIME_DEFERRED
}
class GraphCache {
    public static class State{
        public State(DatasetGraph cachedData, String version) {
            super();
            this.cachedData = cachedData;
            this.version = version;
        }

        public DatasetGraph cachedData;
        String version; // timestamp of the file
    }

    protected Cache<Path, State> cache;

    public DatasetGraph load(Path path) throws ExecutionException, IOException {
        FileTime time = Files.getLastModifiedTime(path);
        String actualVersion = time.toInstant().toString();

        State state = cache.get(path, () -> {
            DatasetGraph data = null;
            State newState = new State(data, actualVersion);
            return newState;
        });

        if(!actualVersion.equals(state.version)) {
            // Reload the data
        }

        DatasetGraph result = null;
        return result;
    }
}

class FileLockUtils {
    public static class State {
        public FileChannel fileChannel;
        public FileLock processLock;
        //public java.util.concurrent.locks.Lock threadLock;
        public Semaphore threadLock;
    }

    private static final Map<Path, State> pathToState = new HashMap<>();

    /**
     * Request an exclusive file channel. The reference to the channel can be shared
     * among several threads, but the channel itself exists only once.
     *
     * Do not directly close the FileChannel!
     * Always close the reference as this also releases locks.
     *
     * @param path
     * @param readLockRequested
     * @param openOptions
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    public static Reference<FileChannel> open(Path path, boolean readLockRequested, OpenOption... openOptions) throws IOException, InterruptedException {
        Path norm = path.normalize();

//        System.out.println("pathToState " + Thread.currentThread() + " " + pathToState);

        State state;
        synchronized(pathToState) {
            state = pathToState.get(norm);
            if(state == null) {
                System.out.println("Aquired system wide lock");
                state = new State();
                state.fileChannel = FileChannel.open(path, openOptions);
                state.processLock =  state.fileChannel.lock(0, Long.MAX_VALUE, readLockRequested);
                //state.threadLock = new ReentrantLock(true);
                state.threadLock = new Semaphore(1);
                pathToState.put(norm, state);
            }
        }


        // If we (process-level) locked a file channel, it seems we cannot have multiple channels on
        // the file within the same JVM(?)

        // We use a semaphore here because we can hand out the file channel only once
        // and another thread my trigger the closing
        // If we used a lock, then then the implicit restriction is,
        // that the same thread that aquired it would also have to do the release,
        // otherwise it would result in an
        // IllegalMonitorStateException
        // state.threadLock.lock();
        state.threadLock.acquire();

        State s = state;
        Reference<FileChannel> result = ReferenceImpl.create(
                s.fileChannel, () -> {
                    synchronized(pathToState) {
                        s.processLock.close();
                        s.fileChannel.close();
                        pathToState.remove(norm);
                        s.threadLock.release();
                    }
                }, null);


        return result;
    }
}

abstract class FileSyncBase
    implements Transactional, AutoCloseable
{
    public static class State {
        Reference<FileChannel> channelRef;
        // FileLock lock;
        Lock lock;
        ReadWrite transactionMode;
    }

//    protected Path path;
//    protected OpenOption[] openOptions;
    protected Supplier<Reference<FileChannel>> rootFileChannelSupp;


    protected Reference<FileChannel> rootFileChannelRef = null;
    protected ThreadLocal<State> localState = new ThreadLocal<>();

    protected Lock transactionLock = new LockMRPlusSW();


    protected LockPolicy lockPolicy;

//    public FileSyncBase(Path path) {
//        this(path, StandardOpenOption.CREATE, StandardOpenOption.READ, StandardOpenOption.WRITE);
//    }

//    public FileSyncBase(Path path, OpenOption ... openOptions) {
//        super();
//        this.path = path;
//        this.openOptions = openOptions;
//    }
    public FileSyncBase(LockPolicy lockPolicy, Supplier<Reference<FileChannel>> rootFileChannelSupp) throws Exception {
        super();
//        this.path = path;
//        this.openOptions = openOptions;
        this.lockPolicy = lockPolicy;
        this.rootFileChannelSupp = rootFileChannelSupp;

        if(LockPolicy.LIFETIME.equals(lockPolicy)) {
            // Initialize the root lock immediately
            // and close the local lock the method returns
            aquireLocalFileChannelRef().close();
        }
    }

    /**
     * Override this method to read the content of the channel
     * into some object
     *
     * @param fc
     */
    protected abstract void loadFrom(FileChannel fc);

    /**
     * Override this method to write some object into the file channel
     * into a target object
     *
     * @param fc
     */
    protected abstract void storeTo(FileChannel fc);


    @Override
    public void begin(TxnType type) {
        ReadWrite readWrite = TxnType.convert(type);
        begin(readWrite);
    }

    @Override
    public void begin(ReadWrite readWrite) {
        try {
            prepareBegin(readWrite);
            boolean readLockRequested = readWrite.equals(ReadWrite.READ);

//            System.out.println(Thread.currentThread() + " wants to enter");
            transactionLock.enterCriticalSection(readLockRequested);
//            System.out.println(Thread.currentThread() + " entered");

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected Reference<FileChannel> aquireLocalFileChannelRef() throws Exception {

        // If the file has just been opened, we need to load the data
        // once we obtained the file lock
        boolean needsDataLoading = false;

        Reference<FileChannel> localFcRef;
        synchronized(this) {
            if(rootFileChannelRef == null) {
                // if .isAlive returns false, it means that the close action
                // is running
                //Reference<FileChannel> ref = FileLockUtils.open(path, readLockRequested, openOptions);
                Reference<FileChannel> ref = rootFileChannelSupp.get();

                FileChannel fileChannel = ref.get();
                rootFileChannelRef = ReferenceImpl.create(fileChannel,
                        () -> {
                            ref.close();
                            rootFileChannelRef = null;
                        }, null);

                // After closing the rootFileChannelRef the open-state of the file channel
                // depends on the local reference
                localFcRef = rootFileChannelRef.aquire(null);

                if(LockPolicy.TRANSACTION.equals(lockPolicy)) {
                    rootFileChannelRef.close();
                }
                needsDataLoading = true;


                // If the file has not changed since the last loading
                // we do not have to read it again
                // We still need to aquire a file lock though

            } else {
                // Aquire may wait for close to finish
                localFcRef = rootFileChannelRef.aquire(null);
            }
        }

        if(needsDataLoading) {
            // The input stream is intentionally not closed;
            // as it would close the file cannel.
            // The locks depend on the file channel, so the channel
            // needs to remain open for the time of transaction
//            Lang lang = Lang.TRIG;
//            InputStream in = Channels.newInputStream(localFc);
//            RDFDataMgr.read(getW(), in, lang);
            localFcRef.get().position(0);
            loadFrom(localFcRef.get());
        }

        return localFcRef;
    }

    public void prepareBegin(ReadWrite readWrite) throws Exception {
//        System.out.println("begin " + Thread.currentThread());
        boolean readLockRequested = readWrite.equals(ReadWrite.READ);


        // Check that the thread is not already in a transaction
        State state = localState.get();
        if(state != null) {
            throw new RuntimeException("Thread is already in a transaction");
        }


        // Open the file if it has not been opened by another transaction before
        Reference<FileChannel> localFcRef = aquireLocalFileChannelRef();



        state = new State();
        state.channelRef = localFcRef;
        state.transactionMode = readWrite;

        localState.set(state);
    }

    protected State local() {
        State result = localState.get();
        Objects.requireNonNull(result);
        return result;
    }

    @Override
    public boolean promote(Promote mode) {
        return false;
    }

    @Override
    public void commit() {
        if(!isInTransaction()) {
            throw new JenaTransactionException("commit called outside of transaction");
        }
//        System.out.println("commit " + Thread.currentThread());

        State state = local();
        FileChannel fc = state.channelRef.get();
        try {
            fc.position(0);
            storeTo(fc);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void abort() {
//        System.out.println("abort " + Thread.currentThread());

        end();
    }

    @Override
    public ReadWrite transactionMode() {
        ReadWrite result = local().transactionMode;
        return result;
    }

    @Override
    public TxnType transactionType() {
        return null;
    }

    @Override
    public boolean isInTransaction() {
        boolean result = localState.get() != null;
        return result;
    }

    @Override
    public void end() {
//        System.out.println("end " + Thread.currentThread());

        if(isInTransaction()) {
            try {
                State state = local();
                synchronized(this) {
                    state.channelRef.close();
                }
                localState.remove();
                transactionLock.leaveCriticalSection();
    //            System.out.println(Thread.currentThread() + " left");

            } catch(Exception e) {
                throw new RuntimeException(e);
            }
        }
//        System.out.println("done end " + Thread.currentThread());
    }

    @Override
    public void close() throws Exception {
        syncronizeOn(this, () -> rootFileChannelRef != null, rootFileChannelRef::close);
    }

    public static void syncronizeOn(Object syncObj, Supplier<Boolean> condition, AutoCloseable action) throws Exception {
        if(condition.get()) {
            synchronized(syncObj) {
                if(condition.get()) {
                    action.close();
                }
            }
        }
    }
}

/**
 * Write to a file when committing a transaction.
 *
 * The file content is cached in memory
 * When obtaining a read lock,
 *
 * Acquiring a read or write lock will lock the file for other processes.
 *
 * If a write there is a read transaction
 *
 *
 */
public class GraphWithSync
    extends DatasetGraphWrapper
{
    protected Transactional syncher;



    public GraphWithSync(DatasetGraph dsg, LockPolicy lockPolicy, Path path) throws Exception {
        super(dsg);

        Supplier<Reference<FileChannel>> rootRefSupp = () -> {
            try {
                return FileLockUtils.open(path, false, StandardOpenOption.CREATE, StandardOpenOption.READ, StandardOpenOption.WRITE);
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        };



        //Transactional that = super.be
        RDFFormat fmt = RDFFormat.TRIG_PRETTY;
        //TxnSync<? extends DatasetGraph> syncher
        syncher = new FileSyncBase(lockPolicy, rootRefSupp) {
//            @Override
//            public void begin(ReadWrite readWrite) {
//                super.begin(readWrite);
//
//            }
            @Override
            protected void loadFrom(FileChannel localFc) {
//                if(!dsg.isInTransaction()) {
//                    throw new RuntimeException("we should be in a transaction here");
//                }
                dsg.clear();

                // The input stream is intentionally not closed;
                // as it would close the file cannel.
                // The locks depend on the file channel, so the channel
                // needs to remain open for the time of transaction
                Lang lang = fmt.getLang();
                InputStream in = new CloseShieldInputStream(Channels.newInputStream(localFc));
                RDFDataMgr.read(getW(), in, lang);
            }

            @Override
            protected void storeTo(FileChannel localFc) {
                OutputStream out = new CloseShieldOutputStream(Channels.newOutputStream(localFc));
                RDFDataMgr.write(out, dsg, fmt);
            }
        };

        //this.syncher = syncher;
    }

    @Override
    public void begin() {
        begin(TxnType.READ);
    }

    @Override
    public void begin(TxnType type) {
        ReadWrite readWrite = TxnType.convert(type);
        begin(readWrite);
    }

    /**
     * Beginning a transaction always starts with aquiration
     * of a lock on the file opened either in read or write mode.
     *
     */
    @Override
    public void begin(ReadWrite readWrite) {
        // Prepare the txn on the in memory model first, because we may need to
        // load the data from the file
        super.begin(readWrite);
        syncher.begin(readWrite);
//        try {
//            super.begin(readWrite);
//        } catch(Exception e) {
//            syncher.end();//finishTransaction();
//            throw new RuntimeException(e);
//        }
    }

    @Override
    public void commit() {
        try {
            // FIXME the second commit may fail even after writing to file succeeded
            // Then we'd have a desync
            syncher.commit();
            super.commit();
        } finally {
            syncher.end();
        }
    }

    @Override
    public void abort() {
        super.abort();
        //end();
    }

    @Override
    public void close() {
        if (isInTransaction()) {
            abort();
        }

        if(syncher instanceof AutoCloseable) {
            try {
                ((AutoCloseable)syncher).close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        super.close();
    }

    @Override
    public void end() {
        super.end();
        syncher.end();
    }

    /**
     * Copied from {@link DatasetGraphWrapper}
     *
     * @param <T>
     * @param mutator
     * @param payload
     */
    private <T> void mutate(final Consumer<T> mutator, final T payload) {
        if (isInTransaction()) {
            if (!transactionMode().equals(WRITE)) {
                TxnType mode = transactionType();
                switch (mode) {
                case WRITE:
                    break;
                case READ:
                    throw new JenaTransactionException("Tried to write inside a READ transaction!");
                case READ_COMMITTED_PROMOTE:
                case READ_PROMOTE:
                    throw new RuntimeException("promotion not implemented");
//                    boolean readCommitted = (mode == TxnType.READ_COMMITTED_PROMOTE);
//                    promote(readCommitted);
                    //break;
                }
            }
            mutator.accept(payload);
        } else executeWrite(this, () -> mutator.accept(payload));
    }

    @Override
    public void clear() {
        mutate(x -> {
            getW().clear();
        } , null);
    }


    public static void main(String[] args) throws Exception {
        Path file = Paths.get("/tmp/txn-test.trig");

        DatasetGraph dg = new GraphWithSync(DatasetGraphFactory.createTxnMem(), LockPolicy.LIFETIME, file);
        Dataset ds = DatasetFactory.wrap(dg);

        dg.clear();

        List<String> test = IntStream.range(0, 1000)
                //.mapToObj(i -> new Triple(RDF.type.asNode(), RDF.type.asNode(), NodeFactory.createLiteral("" + i)))
                .mapToObj(i -> "INSERT DATA { <foo> <bar> " + i + "}")
                .collect(Collectors.toList());

        test.parallelStream().forEach(stmt -> {
                    System.out.println(Thread.currentThread() + " working");
                    RDFConnection conn = RDFConnectionFactory.connect(ds);
                    conn.begin(ReadWrite.WRITE);
                    conn.update(stmt);
                    conn.commit();
        });
    }
}
