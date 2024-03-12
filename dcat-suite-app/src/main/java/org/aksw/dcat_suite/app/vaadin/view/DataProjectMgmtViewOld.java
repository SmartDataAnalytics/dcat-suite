package org.aksw.dcat_suite.app.vaadin.view;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.aksw.commons.collection.observable.ObservableValue;
import org.aksw.commons.collection.observable.ObservableValueImpl;
import org.aksw.commons.io.util.FileUtils;
import org.aksw.dcat_suite.app.gtfs.DetectorGtfs;
import org.aksw.dcat_suite.app.model.api.GroupMgr;
import org.aksw.dcat_suite.app.model.api.GroupMgrFactory;
import org.aksw.dcat_suite.cli.cmd.file.DcatRepoLocal;
import org.aksw.jena_sparql_api.vaadin.util.GridWrapperBase;
import org.aksw.jena_sparql_api.vaadin.util.VaadinSparqlUtils;
import org.aksw.jenax.dataaccess.sparql.execution.query.QueryExecutionWrapperTxn;
import org.aksw.jenax.path.core.PathPE;
import org.aksw.jenax.stmt.core.SparqlParserConfig;
import org.aksw.jenax.stmt.parser.query.SparqlQueryParser;
import org.aksw.jenax.stmt.parser.query.SparqlQueryParserImpl;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sys.JenaSystem;
import org.apache.jena.system.Txn;
import org.apache.jena.vocabulary.DCAT;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.claspina.confirmdialog.ButtonOption;
import org.claspina.confirmdialog.ConfirmDialog;
import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.FileData;
import com.vaadin.flow.component.upload.receivers.MultiFileBuffer;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalConfigurableFilterDataProvider;
import com.vaadin.flow.data.provider.hierarchy.TreeData;
import com.vaadin.flow.data.provider.hierarchy.TreeDataProvider;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;

//@Route(value = "group/:groupIdOld*", layout = DmanMainLayout.class)
//@PageTitle("Data Project Management")
public class DataProjectMgmtViewOld
    extends VerticalLayout
    implements BeforeEnterObserver
{

    static { JenaSystem.init(); }

    protected GroupMgrFactory dcatRepoMgr;
    protected GroupMgr groupMgr;
    // protected DcatRepoLocal dcatRepo;

//    protected FileRepoResolver fileRepoResolver;

    protected Grid<Binding> datasetGrid;
    protected HeaderRow datasetGridHeaderRow;

    // protected Path fileRepoRootPath;


    /** The active path within the file repository; used e.g. for uploads and searches */
    protected ObservableValue<Path> activePath = ObservableValueImpl.create(null);

    protected HorizontalLayout headingLayout;
    protected Image logoImg;
    protected Div description;
    protected H1 heading;

    protected boolean groupExists;
    protected String groupId;


    protected Button addDatasetFromFileBtn;

    protected TreeGrid<Path> folderTreeGrid;

    protected Button createGroupBtn = new Button("Create Group");

    protected Upload upload;

    protected Path groupFileStore;

    protected TextField txtSearch;

    protected Button addDatasetBtn = new Button("Add Dataset");

    protected TreeGrid<Path> fileGrid;

    protected Dataset dataset;

    /** List member datasets of the active group */
    public static String listDatasets() {
        // "CONSTRUCT { ?s a RootVar } { ?s }";
        // Sparqlqueryte
        return null;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        groupId = event.getRouteParameters().get("groupId").orElse(null);

        groupMgr = dcatRepoMgr.create(groupId);

        if (groupExists) {
            DcatRepoLocal repo = groupMgr.get();
            Dataset dataset = repo.getDataset();
        }
        // dcatRepo = groupMgr.get();
        // fileRepoRootPath = fileRepoResolver.getRepo(groupId);
        updateView();
    }

    protected void updateView() {
        groupExists = groupMgr.exists(); // Txn.calculateRead(dataset, () -> dataset.containsNamedModel(groupId));


        heading.setText(groupId);
        description.getElement().setProperty("innerHTML", "<i>no description</i>");

        createGroupBtn.setVisible(!groupExists);


        SparqlQueryParser parser = SparqlQueryParserImpl.create(SparqlParserConfig.newInstance()
                .setIrixResolverAsGiven()
                .setBaseURI(""));
        Query q = parser.apply("SELECT * { GRAPH <" + groupId + "> { ?s <http://www.w3.org/2000/01/rdf-schema#member> ?o } }");

        if (groupExists) {
            DcatRepoLocal repo = groupMgr.get();
            Dataset dataset = repo.getDataset();

            VaadinSparqlUtils.setQueryForGridBinding(
                    new GridWrapperBase<>(datasetGrid),
                    datasetGridHeaderRow,
                    (Query query) -> QueryExecutionWrapperTxn.wrap(QueryExecutionFactory.create(query, dataset), dataset),
                    q);

            logoImg.setSrc("http://localhost/webdav/gitalog/logo.png");


        } else {
            datasetGrid.setDataProvider(new ListDataProvider<>(Collections.emptyList()));
        }

        datasetGrid.getDataProvider().refreshAll();


        Path fileRepoRootPath = groupMgr.getBasePath();

        HierarchicalConfigurableFilterDataProvider<Path, Void, String> folderDataProvider =
                HierarchicalDataProviderForPath.createForFolderStructure(fileRepoRootPath).withConfigurableFilter();

        folderTreeGrid.setDataProvider(folderDataProvider);
        folderTreeGrid.addSelectionListener(ev -> {
            activePath.set(ev.getFirstSelectedItem().orElse(null));
        });

        activePath.addValueChangeListener(ev -> {
            txtSearch.setLabel("Search " + pathToString(ev.getNewValue()));
        });

        updateFileSearch();
    }

    protected String pathToString(Path path) {
        Path fileRepoRootPath = groupMgr.getBasePath();

        return path == null ? "(null)" : (fileRepoRootPath.equals(path) ? "/" : path.getFileName().toString());
    }


    /**
     * Invoke the callable and return an optional with the value.
     * In case of an exception yields an empty optional.
     */
    public static <T> Optional<T> tryCall(Callable<T> callable) {
        Optional<T> result;
        try {
            result = Optional.ofNullable(callable.call());
        } catch (Exception e) {
            result = Optional.empty();
        }
        return result;
    }

    public static <T> void invoke(AutoCloseable action, Consumer<? super Exception> exceptionHandler) {
        try {
            action.close();
        } catch (Exception e) {
            if (exceptionHandler != null) {
                exceptionHandler.accept(e);
            }
        }
    }


    public DataProjectMgmtViewOld(
            @Autowired GroupMgrFactory dcatRepoMgr
//            @Autowired Dataset dataset,
//            FileRepoResolver fileRepoResolver
            ) throws Exception {

        headingLayout = new HorizontalLayout();
        headingLayout.setWidthFull();

        logoImg = new Image(); // "", "logo");
//        logoImg.setAlt("logo");
//        logoImg.getStyle().set("float", "left");
        logoImg.setMaxWidth(10, Unit.EM);
        logoImg.setMaxHeight(10, Unit.EM);

        VerticalLayout headingAndDescriptionSeparator = new VerticalLayout();

        heading = new H1();
//        heading.getStyle().set("float", "none");

        description = new Div();
//        heading.getStyle().set("float", "none");



        // datasetGrid.setDataProvider(null);

        this.dcatRepoMgr = dcatRepoMgr;
        // this.dataset = dataset;

        this.createGroupBtn.addClickListener(ev -> {
            groupMgr.create();
//            Txn.executeWrite(dataset, () -> {
//                Model m = dataset.getNamedModel(groupId);
//                Resource s = m.createResource(groupId);
//                s.addProperty(RDF.type, ResourceFactory.createResource("https://example.org/DataProject"));
//                updateView();
//            });
        });


        this.addDatasetBtn.addClickListener(ev -> {
            Txn.executeWrite(dataset, () -> {
                String newDatasetName = IntStream.range(1, Integer.MAX_VALUE)
                    .mapToObj(i -> groupId + "/dataset" + i)
                    .filter(name -> !dataset.containsNamedModel(name))
                    .findFirst()
                    .orElse(null);

                Model m = dataset.getNamedModel(newDatasetName);
                Resource s = m.createResource(newDatasetName);
                s.addProperty(RDF.type, DCAT.Dataset);


                Model x = dataset.getNamedModel(groupId);
                Resource groupRes = x.createResource(groupId);

                groupRes.addProperty(RDFS.member, s);

                updateView();
            });
        });

        // Receiver receiver = new MultiFileBuffer();
        // AbstractFileBuffer receiver = new AbstractFileBuffer(file -> new File()) {};
        MultiFileBuffer receiver = new MultiFileBuffer();
        upload = new Upload(receiver);

        upload.setDropAllowed(true);
        upload.addSucceededListener(event -> {


            Path fileRepoRootPath;
//            try {
                fileRepoRootPath = groupMgr.getBasePath();
                // Files.createDirectories(fileRepoRootPath);
//            } catch (IOException e1) {
//                throw new RuntimeException(e1);
//            }


            String fileName = event.getFileName();

            FileData fileData = receiver.getFileData(fileName);
            File file = fileData.getFile();
            Path srcPath = file.toPath();
            Path tgtPath = fileRepoRootPath.resolve(fileName);

            try {
                FileUtils.moveAtomic(srcPath, tgtPath);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            Notification.show(String.format("Upload succeeded. Filename: '%s'", fileName));

            updateFileSearch();
        });
        upload.addFailedListener(event -> {
            Notification.show(String.format("Upload failed. Filename: %s", event.getFileName()));
        });
//        upload.getElement()
//        .addEventListener(
//                "file-remove",
//                event -> {
//                  enrichList.clear();
//                });
//        content.add(upload);



        this.datasetGrid = new Grid<>();
        this.datasetGridHeaderRow = datasetGrid.appendHeaderRow();

        txtSearch = new TextField("Search /");
        txtSearch.setValueChangeMode(ValueChangeMode.LAZY);


        // Path repoRootPath = Paths.get("/var/www/webdav/gitalog/store");


        TreeData<Path> treeData = new TreeData<>();
        TreeDataProvider<Path> treeDataProvider = new TreeDataProvider<>(treeData);


        folderTreeGrid = new TreeGrid<>();
        {
            Column<?> hierarchyColumn = folderTreeGrid.addHierarchyColumn(path -> {
                // System.out.println(path);
                // return "" + Optional.ofNullable(path).map(Path::getFileName).map(Object::toString).orElse("");
                return pathToString(path);
                // return path.toString();
            });
            hierarchyColumn.setResizable(true);
            hierarchyColumn.setFrozen(true);
        }


        fileGrid = new TreeGrid<>(treeDataProvider);
        fileGrid.setSizeFull();
        {
            Column<?> hierarchyColumn = fileGrid.addHierarchyColumn(path -> {
                // System.out.println(path);
                // return "" + Optional.ofNullable(path).map(Path::getFileName).map(Object::toString).orElse("");
                Path fileRepoRootPath = groupMgr.getBasePath();
                return Objects.toString(fileRepoRootPath.relativize(path));
                // return path.toString();
            });
            hierarchyColumn.setResizable(true);
            hierarchyColumn.setFrozen(true);
        }

        GridContextMenu<Path> contextMenu = fileGrid.addContextMenu();
        contextMenu.addOpenedChangeListener(ev -> {
            if (!ev.isOpened()) {
                contextMenu.removeAll();
            }
        });

        Dialog importGtfsDialog = new Dialog();
        Button closeBtn = new Button("Close", ev -> importGtfsDialog.close());
        importGtfsDialog.add("Import GTFS...");
        importGtfsDialog.add(closeBtn);

        contextMenu.setDynamicContentHandler(absPath -> {
            Path fileRepoRootPath = groupMgr.getBasePath();
            Path relPath = fileRepoRootPath.relativize(absPath);

            contextMenu.addItem("Actions for " + relPath.toString()).setEnabled(false);
            contextMenu.add(new Hr());

            int numOptions = 0;

            boolean isGtfs = tryCall(() -> DetectorGtfs.isGtfs(absPath)).orElse(false);
            if (isGtfs) {
                contextMenu.addItem("Import GTFS...", ev -> {
                    importGtfsDialog.open();
                });
                ++numOptions;
            }

            contextMenu.addItem("Delete", ev -> {
                ConfirmDialog dialog = confirmDialog("Confirm delete",
                        "You are about to delete: " + relPath,
                        "Delete", x -> {
                            invoke(() -> Files.delete(absPath), e -> {
                                // TODO Show a notification if delete failed
                            });
                            updateFileSearch();
                        }, "Cancel", x -> {});
                //dialog.setConfirmButtonTheme("error primary");
                dialog.open();
            });
            ++numOptions;

            if (numOptions == 0) {
                contextMenu.addItem("(no actions available)").setEnabled(false);
            }

            return true;
        });


        txtSearch.addValueChangeListener(ev -> {
            // ((TreeDataProvider<Hierarchy>)treegrid.getDataProvider())
            String value = ev.getValue();
            updateFileSearch(treeDataProvider, value);
        });
        txtSearch.setValue("");
        // updateFileSearch(treeData, treeDataProvider, "");


        headingAndDescriptionSeparator.add(heading);
        headingAndDescriptionSeparator.add(description);

        headingLayout.add(logoImg);
        headingLayout.add(headingAndDescriptionSeparator);

        add(headingLayout);

        add(upload);


        TreeGrid<PathPE> dataGrid = new TreeGrid<>();
        {
            Column<?> hierarchyColumn = dataGrid.addHierarchyColumn(path -> {

                PathPE fn = path.getFileName();
                String str = fn == null ? "/" : fn.toSegment().tryGetConstant()
                        .map(Object::toString).orElse(fn.toString());

                return str;
            });
            hierarchyColumn.setResizable(true);
            hierarchyColumn.setFrozen(true);
        }
        dataGrid.setDataProvider(HierarchicalDataProviderForPathPE.createTest());

        add(dataGrid);



        add(datasetGrid);

        add(createGroupBtn);
        // setSizeFull();

        add(addDatasetBtn);



        HorizontalLayout fileMgrPanel = new HorizontalLayout();
        fileMgrPanel.setWidthFull();

        VerticalLayout fileListPanel = new VerticalLayout();
        fileListPanel.setWidthFull();
        fileListPanel.add(txtSearch, fileGrid);
        fileGrid.setSizeFull();

        folderTreeGrid.setWidthFull();
        fileMgrPanel.add(folderTreeGrid, fileListPanel);
        fileMgrPanel.setFlexGrow(1, folderTreeGrid);
        fileMgrPanel.setFlexGrow(1, fileListPanel);

        add(fileMgrPanel);
    }

    public void updateFileSearch() {
        updateFileSearch((TreeDataProvider<Path>)fileGrid.getDataProvider(), txtSearch.getValue());
    }

    public void updateFileSearch(TreeDataProvider<Path> treeDataProvider, String value) {

        TreeData<Path> treeData = treeDataProvider.getTreeData();

        String str = Optional.ofNullable(value).orElse("");
        String v = str.isBlank() ? "*" : str;


        // PathMatcher pathMatcher = fileSystem.getPathMatcher("glob:" + v);

        Pattern pattern = Pattern.compile(buildPattern(v), Pattern.CASE_INSENSITIVE);
        // Pattern.compile(v, Pattern.LITERAL);

        PathMatcher pathMatcher = path -> pattern.matcher(path.toString()).find();
        // dataProvider.setFilter(v);


        try {
            TreeData<Path> td = new TreeData<>();

            Path fileRepoRootPath = groupMgr.getBasePath();

            List<Path> matches = fileRepoRootPath == null || !Files.exists(fileRepoRootPath)
                    ? Collections.emptyList()
                    : Files.walk(fileRepoRootPath)
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


    // Signature compatible with ConfirmDialog of vaadin pro
    public ConfirmDialog confirmDialog(String header, String text, String confirmText,
            Consumer<?> confirmListener,
            String cancelText,
            Consumer<?> cancelListener) {

        return ConfirmDialog
            .createQuestion()
            .withCaption(header)
            .withMessage(text)
            .withOkButton(() -> confirmListener.accept(null), ButtonOption.focus(), ButtonOption.caption(confirmText))
            .withCancelButton(() -> cancelListener.accept(null), ButtonOption.caption(cancelText));
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
