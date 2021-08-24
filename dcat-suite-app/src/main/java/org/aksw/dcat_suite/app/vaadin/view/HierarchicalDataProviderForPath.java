package org.aksw.dcat_suite.app.vaadin.view;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.Optional;
import java.util.stream.Stream;

import com.google.common.primitives.Ints;
import com.vaadin.flow.data.provider.hierarchy.AbstractBackEndHierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalQuery;

public class HierarchicalDataProviderForPath
    extends AbstractBackEndHierarchicalDataProvider<Path, String>
{
    protected Path basePath;

    public HierarchicalDataProviderForPath(Path basePath) {
        super();
        this.basePath = basePath;
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
        try {
            Path path = nullToRoot(item);
            boolean result = Files.isDirectory(path) ? Files.list(path).count() > 0 : false;
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected Stream<Path> fetchChildrenFromBackEnd(HierarchicalQuery<Path, String> query) {
        Path parent = nullToRoot(query.getParent());
        FileSystem fs = parent.getFileSystem();
        String filterStr = Optional.ofNullable(query.getFilter().orElse(null)).orElse("");

//        String filterStr = Optional.ofNullable(query.getFilter().orElse(null)).orElse("*");

        PathMatcher pathMatcher = filterStr.isBlank() ? path -> true : fs.getPathMatcher(filterStr);


        try {
            return Files.list(parent)
                .filter(pathMatcher::matches);

//            return FileUtils.listPaths(parent, filterStr).stream();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
