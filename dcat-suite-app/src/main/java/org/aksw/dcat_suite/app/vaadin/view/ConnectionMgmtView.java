package org.aksw.dcat_suite.app.vaadin.view;

import java.util.Collections;
import java.util.List;

import org.aksw.commons.path.core.Path;
import org.aksw.commons.rx.lookup.LookupService;
import org.aksw.dcat_suite.app.vaadin.layout.DmanMainLayout;
import org.aksw.dcat_suite.app.vaadin.layout.DmanRoutes;
import org.aksw.dcat_suite.conn.CkanDataSource;
import org.aksw.jena_sparql_api.collection.observable.GraphChange;
import org.aksw.jena_sparql_api.schema.NodeSchema;
import org.aksw.jena_sparql_api.schema.NodeSchemaDataFetcher;
import org.aksw.jena_sparql_api.schema.NodeSchemaFromNodeShape;
import org.aksw.jena_sparql_api.schema.PropertySchemaFromPropertyShape;
import org.aksw.jena_sparql_api.schema.ResourceCache;
import org.aksw.jena_sparql_api.schema.SHAnnotatedClass;
import org.aksw.jena_sparql_api.schema.ShapedNode;
import org.aksw.jenax.dataaccess.LabelUtils;
import org.aksw.jenax.reprogen.core.JenaPluginUtils;
import org.aksw.jenax.reprogen.shacl.ShaclGenerator;
import org.aksw.vaadin.shacl.ShaclTreeGrid;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.rdfconnection.SparqlQueryConnection;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.sys.JenaSystem;
import org.apache.jena.vocabulary.RDFS;
import org.topbraid.shacl.model.SHFactory;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = DmanRoutes.CONNECTIONS, layout = DmanMainLayout.class)
@PageTitle("Connections")
public class ConnectionMgmtView
    extends VerticalLayout
{
    protected Button newConnectionButton;

    public ConnectionMgmtView() {
        setSizeFull();
        add(new H1("Connections"));

        newConnectionButton = new Button("New Connection...");
        newConnectionButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_LARGE);



        newConnectionButton.addClickListener(ev -> {
            createNewConnectionDlg().open();
        });

        add(newConnectionButton);

        Node root = NodeFactory.createBlankNode();
        GraphChange delta = new GraphChange();

        add(createShaclForm(root, delta));


        Button createBtn = new Button("Create");
        createBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        createBtn.addClickListener(ev -> {
            Graph d = GraphChange.createEffectiveGraphView(GraphFactory.createDefaultGraph(), delta);
            Resource r = ModelFactory.createModelForGraph(d).wrapAsResource(root);
            RDFDataMgr.write(System.out, r.getModel(), RDFFormat.TURTLE_PRETTY);
        });

        add(createBtn);
    }


    public Dialog createNewConnectionDlg() {
        Dialog dialog = new Dialog();
        dialog.getElement().setAttribute("aria-label", "Add Connection");

        VerticalLayout dialogLayout = new VerticalLayout();

        Image image = new Image("images/logos/ckan.png", "ckan");
        image.setWidth("5em");
        image.setHeight("5em");
        dialogLayout.add(image);

        image.addClickListener(ev -> {

            // document.getElementById('overlay').style.width = 'auto'; document.getElementById('overlay').style.height = 'auto';
            dialogLayout.removeAll();
            dialogLayout.add(new Html("<h1>Add CKAN</h1>"));
            // dialogLayout.add(createShaclForm());
            // dialogLayout.ref
        });


        dialog.add(dialogLayout);
        return dialog;
    }


    public TreeGrid<Path<Node>> createShaclForm(Node datasetNode, GraphChange graphEditorModel) {
        JenaSystem.init();
        SHFactory.ensureInited();
        JenaPluginUtils.registerResourceClasses(CkanDataSource.class, NodeSchemaFromNodeShape.class, PropertySchemaFromPropertyShape.class, SHAnnotatedClass.class);
        ModelFactory.createDefaultModel().createResource().as(CkanDataSource.class);


        NodeSchemaFromNodeShape schema = ShaclGenerator.create(CkanDataSource.class);
        RDFDataMgr.write(System.out, schema.getModel(), RDFFormat.TURTLE_PRETTY);


        // GraphChange graphEditorModel = new GraphChange();
        // Node datasetNode = NodeFactory.createURI("http://dcat.linkedgeodata.org/dataset/osm-bremen-2018-04-04");
        // Dataset ds = RDFDataMgr.loadDataset("linkedgeodata-2018-04-04.dcat.ttl");


        Dataset ds = DatasetFactory.create();
        // Node datasetNode = NodeFactory.createBlankNode();
        ResourceCache resourceCache = new ResourceCache();
        SparqlQueryConnection conn = RDFConnectionFactory.connect(ds);
        ShapedNode sn = ShapedNode.create(datasetNode, schema, resourceCache, conn);
//        LookupService<Node, ResourceMetamodel> metaDataService = ResourceExplorer.createMetamodelLookup(conn);

        Multimap<NodeSchema, Node> mm = HashMultimap.create();
        mm.put(schema, datasetNode);

        NodeSchemaDataFetcher dataFetcher = new NodeSchemaDataFetcher();
//        dataFetcher.sync(mm, conn, metaDataService, resourceCache);

        List<ShapedNode> rootNodes = Collections.singletonList(sn);

        Model prefixes = RDFDataMgr.loadModel("rdf-prefixes/prefix.cc.2019-12-17.ttl");
        LookupService<Node, String> labelService =
                LabelUtils.createLookupServiceForLabels(LabelUtils.getLabelLookupService(conn, RDFS.label, prefixes), prefixes, prefixes).cache();


        TreeGrid<Path<Node>> treeGrid = ShaclTreeGrid.createShaclEditor(
                graphEditorModel, rootNodes, labelService);

        treeGrid.addClassName("compact");

        treeGrid.setSizeFull();

        return treeGrid;
    }
}
