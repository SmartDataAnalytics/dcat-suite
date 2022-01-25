package org.aksw.dcat_suite.app.vaadin.view;

import java.nio.file.Path;
import java.util.Collections;
import java.util.stream.IntStream;

import org.aksw.commons.collection.observable.ObservableValue;
import org.aksw.commons.collection.observable.ObservableValueImpl;
import org.aksw.dcat.jena.domain.api.DcatDataset;
import org.aksw.dcat_suite.app.QACProvider;
import org.aksw.dcat_suite.app.model.api.GroupMgr;
import org.aksw.dcat_suite.app.model.api.GroupMgrFactory;
import org.aksw.dcat_suite.app.vaadin.layout.DmanMainLayout;
import org.aksw.dcat_suite.cli.cmd.file.DcatRepoLocal;
import org.aksw.jena_sparql_api.common.DefaultPrefixes;
import org.aksw.jena_sparql_api.vaadin.util.VaadinSparqlUtils;
import org.aksw.jenax.arq.util.binding.QuerySolutionUtils;
import org.aksw.jenax.arq.util.streamrdf.StreamRDFWriterEx;
import org.aksw.jenax.connection.query.QueryExecutionDecoratorTxn;
import org.aksw.jenax.stmt.core.SparqlParserConfig;
import org.aksw.jenax.stmt.parser.query.SparqlQueryParser;
import org.aksw.jenax.stmt.parser.query.SparqlQueryParserImpl;
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
import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "group/:groupId*", layout = DmanMainLayout.class)
@PageTitle("Data Project Management")
public class DataProjectMgmtView
    extends VerticalLayout
    implements BeforeEnterObserver
{
    static { JenaSystem.init(); }

    protected GroupMgrFactory dcatRepoMgr;
    protected GroupMgr groupMgr;
    // protected DcatRepoLocal dcatRepo;

//    protected FileRepoResolver fileRepoResolver;

    protected Grid<DcatDataset> datasetGrid;

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

    
    protected Tab datasetsTab;
	protected Tab resourcesTab;
	protected VerticalLayout content;
    
	protected BrowseRepoComponent fileBrowser;
	protected QACProvider gtfsValidator;
	

    public DataProjectMgmtView(
            @Autowired GroupMgrFactory dcatRepoMgr,
            @Autowired QACProvider gtfsValidator
//            @Autowired Dataset dataset,
//            FileRepoResolver fileRepoResolver
            ) throws Exception {

    	this.gtfsValidator = gtfsValidator;
    	
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
        datasetGrid.setItemDetailsRenderer(new ComponentRenderer<>(DatasetDetailsView::new, DatasetDetailsView::setDataset));
        
        
        headingAndDescriptionSeparator.add(heading);
        headingAndDescriptionSeparator.add(description);

        headingLayout.add(logoImg);
        headingLayout.add(headingAndDescriptionSeparator);

        
        add(headingLayout);

        
        datasetsTab = new Tab(
        	VaadinIcon.CONNECT.create(),
			new Span("Datasets"),
			createBadge("24")
		);
		resourcesTab = new Tab(
			VaadinIcon.FILE.create(),
			new Span("Files"),
			createBadge("439")
		);
//		Tab cancelled = new Tab(
//			new Span("Cancelled"),
//			createBadge("5")
//		);
        
        Tabs tabs = new Tabs();
        tabs.setHeightFull();
        tabs.add(datasetsTab, resourcesTab);
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


    }
  
    
    protected void setContent(Tab tab) {
    	content.removeAll();
		if (tab.equals(datasetsTab)) {
			content.add(datasetGrid, addDatasetBtn);
		} else if (tab.equals(resourcesTab)) {
			content.add(fileBrowser);
		}
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        groupId = event.getRouteParameters().get("groupId").orElse(null);

        groupMgr = dcatRepoMgr.create(groupId);

        if (groupExists) {
            DcatRepoLocal repo = groupMgr.get();
            dataset = repo.getDataset();
        }
        
        fileBrowser = new BrowseRepoComponent(groupMgr.get(), gtfsValidator);

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
                .setSharedPrefixes(DefaultPrefixes.get())
        		.setIrixResolverAsGiven()
                .setBaseURI(""));
        //Query q = parser.apply("SELECT * { GRAPH <" + groupId + "> { ?s <http://www.w3.org/2000/01/rdf-schema#member> ?o } }");
        Query q = parser.apply("SELECT ?s ?g { GRAPH ?g { ?s a dcat:Dataset } }");

        if (groupExists) {
            DcatRepoLocal repo = groupMgr.get();
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

            logoImg.setSrc("http://localhost/webdav/gitalog/logo.png");


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
