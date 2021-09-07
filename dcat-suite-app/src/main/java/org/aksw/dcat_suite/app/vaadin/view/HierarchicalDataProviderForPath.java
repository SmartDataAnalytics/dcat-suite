package org.aksw.dcat_suite.app.vaadin.view;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.Collections;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.aksw.commons.io.util.FileUtils;

import com.google.common.primitives.Ints;
import com.vaadin.flow.data.provider.hierarchy.AbstractBackEndHierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalQuery;

import io.reactivex.rxjava3.core.Flowable;

public class HierarchicalDataProviderForPath
    extends AbstractBackEndHierarchicalDataProvider<Path, String>
{

    protected Path basePath;

    // If includeBasePath is true (default), then null has basePath as its only child
    // Otherwise the children of null are the children of basePath
    protected boolean includeBasePath;
    // protected boolean foldersOnly;
    protected Predicate<Path> folderItemFilter;

    /** Whether to only show the folder structure */
    public HierarchicalDataProviderForPath(Path basePath, boolean includeBasePath, Predicate<Path> folderItemFilter) {
        super();
        this.basePath = basePath;
        // this.foldersOnly = foldersOnly;
        this.includeBasePath = includeBasePath;
        this.folderItemFilter = folderItemFilter;
    }

    public static HierarchicalDataProviderForPath createForFolderStructure(Path basePath) {
        return new HierarchicalDataProviderForPath(basePath, true, Files::isDirectory);
    }


    protected Path nullToRoot(Path path) {
        return path == null ? basePath : path;
    }

    @Override
    public int getChildCount(HierarchicalQuery<Path, String> query) {
        int result = Ints.saturatedCast(fetchChildrenFromBackEnd(query).count());
        return result;
    }

    @Override
    public boolean hasChildren(Path item) {
        boolean result;
        try {
            if (item == null && includeBasePath) {
                result = true;
            } else {
                Path path = nullToRoot(item);
                result = Files.isDirectory(path)
                    ? !Flowable.fromStream(Files.list(path))
                            .filter(folderItemFilter::test).isEmpty().blockingGet()
                    : false;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    @Override
    protected Stream<Path> fetchChildrenFromBackEnd(HierarchicalQuery<Path, String> query) {
        Stream<Path> result;

        Path parent = query.getParent();
        if (parent == null && includeBasePath) {
            result = Collections.singleton(basePath).stream();
        } else {

            parent = nullToRoot(query.getParent());
            FileSystem fs = parent.getFileSystem();
            String filterStr = Optional.ofNullable(query.getFilter().orElse(null)).orElse("");

    //        String filterStr = Optional.ofNullable(query.getFilter().orElse(null)).orElse("*");

            PathMatcher pathMatcher = filterStr.isBlank() ? path -> true : fs.getPathMatcher(filterStr);


            try {
                result = Flowable.fromStream(Files.list(parent))
                    .filter(folderItemFilter::test)
                    .filter(pathMatcher::matches)
                    .toList()
                    .blockingGet() // Materialize and implicitly close the stream before returning
                    .stream();

    //            return FileUtils.listPaths(parent, filterStr).stream();

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return result;
    }
}
