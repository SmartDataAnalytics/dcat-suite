package org.aksw.dcat_suite.app.vaadin.view;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.aksw.dcat_suite.app.vaadin.layout.MClientMainLayout;
import org.apache.jena.sparql.lang.arq.ParseException;

import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.provider.hierarchy.TreeData;
import com.vaadin.flow.data.provider.hierarchy.TreeDataProvider;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "group/:groupId*", layout = MClientMainLayout.class)
@PageTitle("Data Project Management")
public class DataProjectMgmtView
    extends VerticalLayout
    implements BeforeEnterObserver
{


    protected H1 heading;

    protected String groupId;

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        groupId = event.getRouteParameters().get("groupId").orElse(null);

        updateView();
    }

    protected void updateView() {
        heading.setText(groupId);
    }

    public DataProjectMgmtView() throws FileNotFoundException, IOException, ParseException {
        heading = new H1();
        add(heading);

        setSizeFull();

        TextField txtSearch = new TextField("Search");
        txtSearch.setValueChangeMode(ValueChangeMode.LAZY);


        Path repoRootPath = Paths.get("/var/www/webdav/gitalog/store");
        FileSystem fileSystem = repoRootPath.getFileSystem();

//        HierarchicalConfigurableFilterDataProvider<Path, Void, String> dataProvider =
//                new HierarchicalDataProviderForPath(repoRootPath).withConfigurableFilter();


        TreeData<Path> treeData = new TreeData<>();
        TreeDataProvider<Path> treeDataProvider = new TreeDataProvider<>(treeData);

        TreeGrid<Path> treeGrid = new TreeGrid<>(treeDataProvider);
        treeGrid.setSizeFull();


        Column<?> hierarchyColumn = treeGrid.addHierarchyColumn(path -> {
            // System.out.println(path);
            // return "" + Optional.ofNullable(path).map(Path::getFileName).map(Object::toString).orElse("");
            return Objects.toString(repoRootPath.relativize(path));
            // return path.toString();
        });
        hierarchyColumn.setResizable(true);
        hierarchyColumn.setFrozen(true);



        txtSearch.addValueChangeListener(ev -> {
            // ((TreeDataProvider<Hierarchy>)treegrid.getDataProvider())
            String value = ev.getValue();
            String str = Optional.ofNullable(value).orElse("");
            String v = str.isBlank() ? "*" : str;


            // PathMatcher pathMatcher = fileSystem.getPathMatcher("glob:" + v);

            Pattern pattern = Pattern.compile(buildPattern(v), Pattern.CASE_INSENSITIVE);
            // Pattern.compile(v, Pattern.LITERAL);

            PathMatcher pathMatcher = path -> pattern.matcher(path.toString()).find();
            // dataProvider.setFilter(v);


            try {
                TreeData<Path> td = new TreeData<>();

                List<Path> matches = Files.walk(repoRootPath)
                        .filter(Files::isRegularFile)
                        .filter(pathMatcher::matches)
                        .collect(Collectors.toList());

                // List<Path> matches = FileUtils.listPaths(repoRootPath, v);

                for (Path path : matches) {
                    // add(path, repoRootPath, td);
                    td.addItem(null, path);
                }

                TreeDataSynchronizer.sync(td, treeData);
                treeDataProvider.refreshAll();

            } catch (Exception e) {
                throw new RuntimeException(e);
            }


        });

        add(txtSearch);
        add(treeGrid);


    }

    public void add(Path path, Path basePath, TreeData<Path> treeData) {
        if (!treeData.contains(path)) {
            Path parent = path.getParent();

            if (parent != null && parent.startsWith(basePath)) {
                Path effParent = parent.equals(basePath) ? null : parent;

                add(effParent, path, treeData);

                treeData.addItem(effParent, path);
            }

        }
    }



    /** "abc" -> ".*a.*b.*c.*" */
    public static String buildPatternMatchAnywhere(String str) {
        StringBuilder sb = new StringBuilder();
        sb.append(".*");
        for (int i = 0; i < str.length(); ++i) {
            char c = str.charAt(i);

            if ('*' == c) {
                // sb.append(".*");
            } else {
                sb.append(Pattern.quote(Character.toString(c)));
                sb.append(".*");
            }
        }
        return sb.toString();
    }


    public static String buildPattern(String str) {
        StringBuilder sb = new StringBuilder();

        String quotedAsterisk = Pattern.quote("*");

        boolean escaped = false;
        for (int i = 0; i < str.length(); ++i) {
            char c = str.charAt(i);

            if (c == '*') {
                if (!escaped) {
                    escaped = true;
                } else {
                    sb.append(quotedAsterisk);
                    escaped = false;
                }
            } else {
                if (escaped) {
                    sb.append(".*");
                    escaped = false;
                }
                sb.append(Pattern.quote(Character.toString(c)));
            }
        }

        if (escaped) {
            sb.append(".*");
            // escaped = false;
        }


        return sb.toString();
    }
    public static String buildPattern2(String str) {
        StringBuilder sb = new StringBuilder();

        char escapeChar = '\\';
        boolean escaped = false;
        for (int i = 0; i < str.length(); ++i) {
            char c = str.charAt(i);

            if (c == escapeChar) {
                if (!escaped) {
                    escaped = true;
                } else {
                    sb.append(escapeChar);
                    escaped = false;
                }
            } else if (c == '*') {
                if (!escaped) {
                    sb.append(".*");
                } else {
                    sb.append(Pattern.quote("*"));
                    escaped = false;
                }
            } else {
                if (escaped) {
                    sb.append(escapeChar);
                    escaped = false;
                }
                sb.append(Pattern.quote(Character.toString(c)));
            }
        }

        if (escaped) {
            sb.append(escapeChar);
            // escaped = false;
        }


        return sb.toString();
    }

}
