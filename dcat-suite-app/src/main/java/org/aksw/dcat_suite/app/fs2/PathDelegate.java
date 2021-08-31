package org.aksw.dcat_suite.app.fs2;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchEvent.Modifier;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

//public interface PathDelegate<T extends Path> extends Path
//{
//    Path getDelegate();
//
//    T wrap(Path path);
//
//
//    @Override
//    default FileSystem getFileSystem() {
//        return getDelegate().getFileSystem();
//    }
//
//    @Override
//    default boolean isAbsolute() {
//        return getDelegate().isAbsolute();
//    }
//
//    @Override
//    default Path getRoot() {
//        return wrap(getDelegate().getRoot());
//    }
//
//    @Override
//    default Path getFileName() {
//        return wrap(getDelegate().getFileName());
//    }
//
//    @Override
//    public Path getParent() {
//        return getDelegate().getParent();
//    }
//
//    @Override
//    default int getNameCount() {
//        return getDelegate().getNameCount();
//    }
//
//    @Override
//    default Path getName(int index) {
//        return getDelegate().getName(index);
//    }
//
//    @Override
//    default Path subpath(int beginIndex, int endIndex) {
//        get
//    }
//
//    @Override
//    public boolean startsWith(Path other) {
//        // TODO Auto-generated method stub
//        return false;
//    }
//
//    @Override
//    public boolean endsWith(Path other) {
//        // TODO Auto-generated method stub
//        return false;
//    }
//
//    @Override
//    public Path normalize() {
//        // TODO Auto-generated method stub
//        return null;
//    }
//
//    @Override
//    public Path resolve(Path other) {
//        // TODO Auto-generated method stub
//        return null;
//    }
//
//    @Override
//    public T relativize(Path other) {
//        return wrap(getDelegate().relativize(other));
//    }
//
//    @Override
//    public URI toUri() {
//        return getDelegate().toUri();
//    }
//
//    @Override
//    default T toAbsolutePath() {
//        return wrap(getDelegate().toAbsolutePath());
//    }
//
//    @Override
//    default T toRealPath(LinkOption... options) throws IOException {
//        return wrap(getDelegate().toRealPath(options));
//    }
//
//    @Override
//    default WatchKey register(WatchService watcher, Kind<?>[] events, Modifier... modifiers) throws IOException {
//        return getDelegate().register(watcher, events, modifiers);
//    }
//
//    @Override
//    default int compareTo(Path other) {
//        return getDelegate().compareTo(other);
//    }
//
//}
