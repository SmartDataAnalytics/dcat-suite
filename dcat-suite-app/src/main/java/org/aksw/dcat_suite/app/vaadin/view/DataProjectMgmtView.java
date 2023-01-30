package org.aksw.dcat_suite.app.vaadin.view;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.IntStream;

import javax.imageio.ImageIO;

import org.aksw.commons.collection.observable.ObservableValue;
import org.aksw.commons.collection.observable.ObservableValueImpl;
import org.aksw.dcat.jena.domain.api.DcatDataset;
import org.aksw.dcat.jena.domain.api.DcatDistribution;
import org.aksw.dcat.mgmt.api.DataProject;
import org.aksw.dcat_suite.app.QACProvider;
import org.aksw.dcat_suite.app.model.api.GroupMgr;
import org.aksw.dcat_suite.app.model.api.GroupMgrFactory;
import org.aksw.dcat_suite.app.model.api.UserProject;
import org.aksw.dcat_suite.app.session.UserSession;
import org.aksw.dcat_suite.app.vaadin.layout.DmanMainLayout;
import org.aksw.dcat_suite.cli.cmd.file.DcatRepoLocal;
import org.aksw.jena_sparql_api.common.DefaultPrefixes;
import org.aksw.jena_sparql_api.concepts.UnaryXExpr;
import org.aksw.jena_sparql_api.vaadin.util.VaadinSparqlUtils;
import org.aksw.jenax.arq.util.binding.QuerySolutionUtils;
import org.aksw.jenax.arq.util.streamrdf.StreamRDFWriterEx;
import org.aksw.jenax.connection.query.QueryExecutionDecoratorTxn;
import org.aksw.jenax.path.core.PathOpsNode;
import org.aksw.jenax.stmt.core.SparqlParserConfig;
import org.aksw.jenax.stmt.parser.query.SparqlQueryParser;
import org.aksw.jenax.stmt.parser.query.SparqlQueryParserImpl;
import org.aksw.vaadin.jena.geo.GeoJsonJenaUtils;
import org.apache.jena.geosparql.implementation.GeometryWrapper;
import org.apache.jena.geosparql.implementation.vocabulary.SRS_URI;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.sys.JenaSystem;
import org.apache.jena.system.Txn;
import org.apache.jena.vocabulary.DCAT;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.claspina.confirmdialog.ConfirmDialog;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.addon.leaflet4vaadin.LeafletMap;
import com.vaadin.addon.leaflet4vaadin.layer.groups.FeatureGroup;
import com.vaadin.addon.leaflet4vaadin.layer.groups.LayerGroup;
import com.vaadin.addon.leaflet4vaadin.layer.map.options.DefaultMapOptions;
import com.vaadin.addon.leaflet4vaadin.layer.map.options.MapOptions;
import com.vaadin.addon.leaflet4vaadin.types.LatLng;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalConfigurableFilterDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteParameters;
import com.vaadin.flow.router.internal.AfterNavigationHandler;
import com.vaadin.flow.server.StreamResource;

@Route(value = ":user/:project*", layout = DmanMainLayout.class)
@PageTitle("Data Project Management")
public class DataProjectMgmtView
    extends VerticalLayout
    implements BeforeEnterObserver, AfterNavigationHandler
{
    static { JenaSystem.init(); }

    protected UserSession userSession;

    protected DcatRepoLocal dcatRepo;
    protected UserProject userProject;

    // protected GroupMgrFactory dcatRepoMgr;
    // protected GroupMgr groupMgr;
    // protected DcatRepoLocal dcatRepo;

//    protected FileRepoResolver fileRepoResolver;

    protected Grid<DcatDataset> datasetGrid;

    // protected Path fileRepoRootPath;


    /** The active path within the file repository; used e.g. for uploads and searches */
    protected ObservableValue<Path> activePath = ObservableValueImpl.create(null);

    protected HorizontalLayout headingLayout;

    protected Dimension logoDim = new Dimension(256, 256);
    protected Image logoImg;
    protected Div description;
    protected H1 heading;

    protected boolean groupExists;
    protected String groupId;


    protected Button addDatasetFromFileBtn;

    protected TreeGrid<Path> folderTreeGrid;

    protected Button createGroupBtn = new Button("Create Group");


    protected Path groupFileStore;

    protected TextField txtSearch;

    protected Button addDatasetBtn = new Button("Add Dataset");

    protected TreeGrid<Path> fileGrid;

    protected Dataset dataset;


    protected Tab datasetsTab;
    protected Tab resourcesTab;
    protected Tab filesTab;

    protected VerticalLayout content;

    protected BrowseRepoComponent fileBrowser;
    protected QACProvider gtfsValidator;

    protected LeafletMap leafletMap;

    /** Vector layer */
    protected LayerGroup group;



    public static UserProject getProject(UserSession userSession, RouteParameters params) throws IOException {
        String user = params.get("user").orElse(null);
        String project = params.get("project").orElse(null);
        UserProject result = user != null && project != null ? userSession.getUserSpace().getProjectMgr().get(project) : null;
        return result;
    }

    public DataProjectMgmtView(
            @Autowired GroupMgrFactory dcatRepoMgr,
            @Autowired QACProvider gtfsValidator,
            UserSession userSession
//            @Autowired Dataset dataset,
//            FileRepoResolver fileRepoResolver
            ) throws Exception {

        this.userSession = userSession;

        this.gtfsValidator = gtfsValidator;

        headingLayout = new HorizontalLayout();
        headingLayout.setWidthFull();

        logoImg = new Image(); // "", "logo");
//        logoImg.setAlt("logo");
//        logoImg.getStyle().set("float", "left");
        logoImg.setMaxWidth((int)logoDim.getWidth(), Unit.PIXELS);
        logoImg.setMaxHeight((int)logoDim.getHeight(), Unit.PIXELS);


        VerticalLayout headingAndDescriptionSeparator = new VerticalLayout();

        heading = new H1();
//        heading.getStyle().set("float", "none");

        description = new Div();
//        heading.getStyle().set("float", "none");



        // datasetGrid.setDataProvider(null);

        // this.dcatRepoMgr = dcatRepoMgr;
        // this.dataset = dataset;

        this.createGroupBtn.addClickListener(ev -> {
            try {
                userProject.create();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
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
                    .mapToObj(i -> "dataset" + i)
//                    .mapToObj(i -> groupId + "/dataset" + i)
                    .filter(name -> !dataset.containsNamedModel(name))
                    .findFirst()
                    .orElse(null);

                Model m = dataset.getNamedModel(newDatasetName);
                Resource s = m.createResource(newDatasetName);
                s.addProperty(RDF.type, DCAT.Dataset);


                Model x = dataset.getNamedModel(groupId);
                Resource groupRes = x.createResource(groupId);

                groupRes.addProperty(RDFS.member, s);
            });
            updateView();

        });

        datasetGrid = new Grid<>();

        VaadinGridUtils.allowMultipleVisibleItemDetails(datasetGrid);
        datasetGrid.setItemDetailsRenderer(new ComponentRenderer<>(() -> new DatasetDetailsView(() -> {
            datasetGrid.getElement().executeJs("requestAnimationFrame((function() { this.notifyResize(); }).bind(this))");
        }), DatasetDetailsView::setDataset));


        headingAndDescriptionSeparator.add(heading);
        headingAndDescriptionSeparator.add(description);

        headingLayout.add(logoImg);
        headingLayout.add(headingAndDescriptionSeparator);


        add(headingLayout);

        Button gitPushBtn = new Button("Git Sync");
        add(gitPushBtn);

        gitPushBtn.addClickListener(ev -> {
            Repository gitRepo = userProject.getGitRepository();
            try (Git git = new Git(gitRepo)) {
                try {
                    //git.pull().call();
                    git.add().setUpdate(true).addFilepattern(".").call();
                    git.add().addFilepattern(".").call();
                    git.commit().setMessage("Updated").call();
                    Notification.show("Sync sucessful");
                } catch (Exception e) {
                    Notification.show("Sync failed");
                    throw new RuntimeException(e);
                }
            }
        });

//        MapOptions options = new DefaultMapOptions();
//        options.setCenter(new LatLng(47.070121823, 19.204101562500004));
//        options.setZoom(7);
//        leafletMap = new LeafletMap(options);
//        leafletMap.setBaseUrl("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png");
//        leafletMap.setWidth(128, Unit.PIXELS);
//        leafletMap.setHeight(128, Unit.PIXELS);
//        group = new FeatureGroup();
//        group.addTo(leafletMap);
//
//        add(leafletMap);
//        VaadinGeoUtils.toGeoJson(ConvertLatLon.toLiteral(0, 0).asNode()).addTo(group);



        datasetsTab = new Tab(
            VaadinIcon.CONNECT.create(),
            new Span("Datasets")
            //createBadge("24")
        );

        resourcesTab = new Tab(
            VaadinIcon.RECORDS.create(),
            new Span("Resources")
            //createBadge("439")
        );

        filesTab = new Tab(
            VaadinIcon.FILE.create(),
            new Span("Files")
            //createBadge("439")
        );
//		Tab cancelled = new Tab(
//			new Span("Cancelled"),
//			createBadge("5")
//		);

        Tabs tabs = new Tabs();
        tabs.setHeightFull();
        tabs.add(datasetsTab, resourcesTab, filesTab);
        content = new VerticalLayout();
        content.setSpacing(false);

        tabs.addSelectedChangeListener(event -> setContent(event.getSelectedTab()));
        setContent(tabs.getSelectedTab());


//          createTab(VaadinIcon.HOME, "Home", DmanLandingPageView.class),
//          createTab(VaadinIcon.FOLDER_ADD, "New Data Project", NewDataProjectView.class),
//          createTab(VaadinIcon.EYE, "Browse", BrowseRepoView.class),
//          createTab(VaadinIcon.CONNECT, "Connections", ConnectionMgmtView.class)

        // tabs.setOrientation(Tabs.Orientation.VERTICAL);

        add(tabs, content);


//        add(datasetGrid);
//        AppLayout appLayout = new AppLayout();
//        appLayout.addToDrawer(datasetGrid);
//        appLayout.setContent(new Paragraph("Main content"));
//        add(appLayout);


//        add(addDatasetBtn);

//        add(createGroupBtn);
        // setSizeFull();

//        add(addDatasetBtn);



        GridContextMenu<DcatDataset> contextMenu = datasetGrid.addContextMenu();
        contextMenu.addOpenedChangeListener(ev -> {
            if (!ev.isOpened()) {
                contextMenu.removeAll();
            }
        });

        contextMenu.setDynamicContentHandler(r -> {
            // Resource r = qs.getResource("s");

            int numOptions = 0;
            contextMenu.addItem("Actions for " + r).setEnabled(false);
            contextMenu.add(new Hr());

            contextMenu.addItem("Delete", ev -> {
                ConfirmDialog dialog = VaadinDialogUtils.confirmDialog("Confirm delete",
                        String.format("You are about to delete: %s (affects %d triples). This operation cannot be undone.", r.asNode(), dataset.asDatasetGraph().getGraph(r.asNode()).size()),
                        "Delete", x -> {
                            Txn.executeWrite(dataset, () -> {
                                dataset.asDatasetGraph().removeGraph(r.asNode());
                            });
                            updateView();

                            // TODO Show a notification if delete failed
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


        MapOptions mapOptions = new DefaultMapOptions();
        mapOptions.setCenter(new LatLng(47.070121823, 19.204101562500004));
        mapOptions.setZoom(7);
        mapOptions.setPreferCanvas(true);


//        Button x = new Button("Dialog");
//        x.addClickListener(ev -> {
//        	Dialog dlg = new Dialog();
//
//	        LeafletMap map = new LeafletMap(mapOptions);
//	        map.setBaseUrl("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png");
////	        map.setWidth(512, Unit.PIXELS);
////	        map.setHeight(512, Unit.PIXELS);
//
//	           dlg.setResizable(true);
//	            dlg.setDraggable(true);
//	            dlg.setWidth("600px");
//	            dlg.setHeight("400px");
//	            dlg.addResizeListener((dialogResizeEvent)-> {
//	               leafletMap.invalidateSize();
//	            });
//	        dlg.add(map);
//        	dlg.open();
//        });
//        add(x);


        if (false) {
        Grid<String> testGrid = new Grid<>();

        testGrid.addComponentColumn(str -> {
            Div r = new Div();
            r.add(str);

//	        VaadinGeoUtils.toGeoJson(ConvertLatLon.toLiteral(0, 0).asNode()).addTo(group);
            LeafletMap map = new LeafletMap(mapOptions);
            r.setWidth(512, Unit.PIXELS);
            r.setHeight(512, Unit.PIXELS);

//	        Button btn = new Button("test");
            UI.getCurrent().access(() -> {
                 map.setBaseUrl("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png");
                    FeatureGroup group = new FeatureGroup();
                    group.addTo(map);
                    GeoJsonJenaUtils.toWgs84GeoJson(GeometryWrapper.fromPoint(0, 0, SRS_URI.DEFAULT_WKT_CRS84)).addTo(group);
            });



            add(map);

//	        btn.addClickListener(ev -> {
//	        });
//	        r.add(btn);




            r.add(map);

//		        FeatureGroup group = new FeatureGroup();
            // group.addTo(map);
            // r.add(map);
            return r;
        });
        testGrid.setItems("a", "b", "c");

        add(testGrid);


        {
            LeafletMap map = new LeafletMap(mapOptions);
            map.setBaseUrl("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png");
            map.setWidth(512, Unit.PIXELS);
            map.setHeight(512, Unit.PIXELS);
            add(map);
        }
        {
            LeafletMap map = new LeafletMap(mapOptions);
            map.setBaseUrl("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png");
            map.setWidth(512, Unit.PIXELS);
            map.setHeight(512, Unit.PIXELS);
            add(map);
        }
        {
            LeafletMap map = new LeafletMap(mapOptions);
            map.setBaseUrl("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png");
            map.setWidth(512, Unit.PIXELS);
            map.setHeight(512, Unit.PIXELS);
            add(map);
        }
        }
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {

    }



    public Function<org.aksw.commons.path.core.Path<Node>, String> pathToString(org.aksw.commons.path.core.Path<Node> basePath) {
        return path -> {
            Dataset dataset = userProject.getDcatRepo().getDataset();

            // FIXME Starting a txn on every lookup is far from optimal
            String location = Txn.calculateRead(dataset, () ->
                 Optional.ofNullable(GraphEntityUtils.getSelfResource(dataset, path.getSegments()))
                     .map(r -> r.as(DcatDistribution.class).getDownloadUrl()).orElse(null));

            Node node = path == null || path.getNameCount() == 0 ? null : path.getFileName().toSegment();

            String str = path == null ? "(null)" : (Objects.equals(path, basePath) ? "/" : node.toString(false)); // + ": " + node.getClass());

            if (location != null) {
                str += " --- " + location;
            }

            return str;
        };
    }

    protected void setContent(Tab tab) {
        content.removeAll();
        if (tab.equals(datasetsTab)) {
            content.add(datasetGrid, addDatasetBtn);
        } else if (tab.equals(filesTab)) {
            content.add(fileBrowser);
        } else {

            HierarchicalConfigurableFilterDataProvider<org.aksw.commons.path.core.Path<Node>, Void, UnaryXExpr> folderDataProvider =
                    new HierarchicalDataProviderFromCompositeId(userProject.getDcatRepo().getDataset(), false).withConfigurableFilter();

            Function<org.aksw.commons.path.core.Path<Node>, String> pathToString = pathToString(PathOpsNode.newAbsolutePath());

            TreeGrid<org.aksw.commons.path.core.Path<Node>> artifactTreeGrid = new TreeGrid<>();
            //folderTreeGrid.setSizeFull();

            Column<?> hierarchyColumn = artifactTreeGrid.addHierarchyColumn(path -> {
                // return "" + Optional.ofNullable(path).map(Path::getFileName).map(Object::toString).orElse("");
                return pathToString.apply(path);
                // return path.toString();
            }); //.setKey("key");

            // folderTreeGrid.addColumn(p -> "");
            hierarchyColumn.setResizable(true);
            hierarchyColumn.setFrozen(true);

            artifactTreeGrid.setDataProvider(folderDataProvider);
            artifactTreeGrid.setSizeFull();
            artifactTreeGrid.setHeightByRows(true);
            // artifactTreeGrid.setUniqueKeyDataGenerator("key", path -> path.toString());
            artifactTreeGrid.addItemClickListener(ev -> {
                org.aksw.commons.path.core.Path<Node> item = ev.getItem();
                if (!artifactTreeGrid.isExpanded(item)) {
                    artifactTreeGrid.expand(item);
                }
            });

            GridContextMenu<org.aksw.commons.path.core.Path<Node>> contextMenu = artifactTreeGrid.addContextMenu();
            contextMenu.setDynamicContentHandler(path -> {
                contextMenu.removeAll();

                int numOptions = 0;
                contextMenu.addItem("Actions for " + path).setEnabled(false);
                contextMenu.add(new Hr());

                contextMenu.addItem("Delete Tree", ev -> {
                    List<Node> graphNodes = Txn.calculateRead(dataset, () ->
                        GraphEntityUtils.findEntities(dataset, path.getSegments(), true));

                    System.out.println("Lookup with: " + path.getSegments());
                    System.out.println("Graph nodes " + graphNodes);

                    ConfirmDialog dialog = VaadinDialogUtils.confirmDialog("Confirm delete",
                            String.format("You are about to delete: %s (affects %d graphs). This operation cannot be undone.", path.getSegments(), graphNodes.size()),
                            "Delete", x -> {
                                Txn.executeWrite(dataset, () -> {
                                    for (Node node : graphNodes) {
                                        dataset.asDatasetGraph().removeGraph(node);
                                    }
                                });
                                // updateView();
                                artifactTreeGrid.getDataProvider().refreshAll();

                                // TODO Show a notification if delete failed
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


            content.add(artifactTreeGrid);
        }
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        // groupId = event.getRouteParameters().get("groupId").orElse(null);

        try {
            userProject = getProject(userSession, event.getRouteParameters());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // groupMgr = dcatRepoMgr.create(groupId);

        if (groupExists) {
            DcatRepoLocal repo = userProject.getDcatRepo();
            dataset = repo.getDataset();
        }

        fileBrowser = new BrowseRepoComponent(userProject.getDcatRepo(), gtfsValidator) {
            @Override
            public int addExtraOptions(GridContextMenu<Path> contextMenu, Path relPath) {
                int result = super.addExtraOptions(contextMenu, relPath);

                // View (RDF) Metadata
                contextMenu.addItem("Set as Logo ...", ev -> {
                    Dataset dataset = dcatRepo.getDataset();
                    Txn.executeWrite(dataset, () -> {
                        dataset.getNamedModel(".").createResource(".")
                            .as(DataProject.class)
                            .setDepiction(relPath.toString());
                    });
                    updateView();
                });



                return result;
            }
        };

        // dcatRepo = groupMgr.get();
        // fileRepoRootPath = fileRepoResolver.getRepo(groupId);
        updateView();
    }

    protected void updateView() {
        groupExists = userProject.exists(); // Txn.calculateRead(dataset, () -> dataset.containsNamedModel(groupId));


        heading.setText(groupId);
        description.getElement().setProperty("innerHTML", "<i>no description</i>");

        createGroupBtn.setVisible(!groupExists);


        SparqlQueryParser parser = SparqlQueryParserImpl.create(SparqlParserConfig.newInstance()
                .setSharedPrefixes(DefaultPrefixes.get())
                .setIrixResolverAsGiven()
                .setBaseURI(""));
        //Query q = parser.apply("SELECT * { GRAPH <" + groupId + "> { ?s <http://www.w3.org/2000/01/rdf-schema#member> ?o } }");
        Query q = parser.apply("SELECT ?s ?g { GRAPH ?g { ?s a dcat:Dataset } }");

        if (groupExists) {
            DcatRepoLocal repo = userProject.getDcatRepo();;
            dataset = repo.getDataset();

            Txn.executeRead(dataset, () ->
                //RDFDataMgr.write(System.out, dataset, RDFFormat.TRIG_PRETTY)
                StreamRDFWriterEx.writeAsGiven(dataset.asDatasetGraph(), System.out, RDFFormat.NQUADS, null, null)
            );

            VaadinSparqlUtils.setQueryForGridResource(
                    datasetGrid,
                    (Query query) -> QueryExecutionDecoratorTxn.wrap(QueryExecutionFactory.create(query, dataset), dataset),
                    q,
                    DcatDataset.class,
                    "s",
                    QuerySolutionUtils.newGraphAwareBindingMapper(dataset, "s", "g")
                    );

            Dataset dataset = repo.getDataset();

            String logoUrl = Txn.calculateRead(dataset, () -> {
                StreamResource sr = null;

                DataProject dp = dataset.getNamedModel(".").createResource(".")
                    .as(DataProject.class);

                return dp.getDepiction();
            });

            if (logoUrl != null) {
                Path logoPath = logoUrl == null ? null : userProject.getDcatRepo().getBasePath().resolve(logoUrl);
                // new StreamResource("logo.png", () -> DataProjectMgmtView.class.getClassLoader().getResourceAsStream("mclient-logo.png"))

                if (Files.exists(logoPath)) {

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    try {
                        BufferedImage img = ImageIO.read(Files.newInputStream(logoPath));
                        BufferedImage img2 = ImageUtils.scaleImage(img, logoDim);

                        ImageIO.write(img2, "png", baos);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }

                    StreamResource sr = baos == null ? null : new StreamResource("logo.png", () -> new ByteArrayInputStream(baos.toByteArray()));
    //        		StreamResource sr = logoPath == null ? null : new StreamResource("logo.png", () -> {
    //					try {
    //						return Files.newInputStream(logoPath);
    //					} catch (IOException e) {
    //						throw new RuntimeException(e);
    //					}
    //				});

                    if (sr != null) {
                        logoImg.setSrc(sr);
                    }
                }
            }

            // logoImg.setSrc("http://localhost/webdav/gitalog/logo.png");


        } else {
            datasetGrid.setDataProvider(new ListDataProvider<>(Collections.emptyList()));
        }

        datasetGrid.getDataProvider().refreshAll();
    }


    /**
     * Helper method for creating a badge.
     */
    public static Span createBadge(String str) {
        Span badge = new Span(str);
        badge.getElement().getThemeList().add("badge small contrast");
        badge.getStyle().set("margin-inline-start", "var(--lumo-space-xs)");
        return badge;
    }

}
