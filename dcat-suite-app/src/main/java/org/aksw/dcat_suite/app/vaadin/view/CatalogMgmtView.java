package org.aksw.dcat_suite.app.vaadin.view;

import java.util.Arrays;
import java.util.List;

import org.aksw.dcat_suite.app.vaadin.layout.DmanMainLayout;
import org.aksw.dcat_suite.app.vaadin.layout.DmanRoutes;
import org.aksw.jena_sparql_api.common.DefaultPrefixes;
import org.aksw.jena_sparql_api.vaadin.util.VaadinSparqlUtils;
import org.aksw.jenax.connection.query.QueryExecutionDecoratorTxn;
import org.aksw.jenax.stmt.core.SparqlParserConfig;
import org.aksw.jenax.stmt.parser.query.SparqlQueryParser;
import org.aksw.jenax.stmt.parser.query.SparqlQueryParserImpl;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = DmanRoutes.CATALOGS, layout = DmanMainLayout.class)
@PageTitle("Catalogs")
public class CatalogMgmtView
    extends VerticalLayout
{
    protected Button newConnectionButton;

	protected Grid<Binding> catalogsGrid;
	
	
	public CatalogMgmtView() {
		add(new H1("Catalogs"));
		
		catalogsGrid = new Grid<>();
		// catalogsGrid.setHeightByRows(true);
		 catalogsGrid.setItemDetailsRenderer(new ComponentRenderer<>(CatalogDetailsView::new , CatalogDetailsView::setBinding));
		
		// GridContextMenu<Resource> contextMenu = catalogsGrid.addContextMenu();

		add(catalogsGrid);
		
//		Button addDistributionBtn = new Button("Add distribution");
//		
//		addDistributionBtn.addClickListener(ev -> {
//			if (dcatDataset != null) {
//				dcatDataset.addProperty(DCAT.distribution, ResourceFactory.createResource());
//				setDataset(dcatDataset); // refresh
//			}
//		});
//		
//		add(addDistributionBtn);
//		
        
//		contextMenu.setDynamicContentHandler(r -> {
//        	// Resource r = qs.getResource("s");
//        	contextMenu.removeAll();
//        	
//        	int numOptions = 0;
//            contextMenu.addItem("Actions for " + r).setEnabled(false);
//            contextMenu.add(new Hr());
//            
//            contextMenu.addItem("Delete", ev -> {
//                ConfirmDialog dialog = VaadinDialogUtils.confirmDialog("Confirm delete",
//                        String.format("You are about to delete: %s (affects %d triples). This operation cannot be undone.", r.asNode(), dcatDataset.getModel().size()),
//                        "Delete", x -> {
//                        	dcatDataset.getModel().remove(dcatDataset, DCAT.distribution, r);
////                        	Txn.executeWrite(dcatDataset, () -> {
////                        		dcatDataset.asDatasetGraph().removeGraph(r.asNode());
////                        	});
////                        	updateView();
//
//                        	setDataset(dcatDataset);
//                            // TODO Show a notification if delete failed
//                        }, "Cancel", x -> {});
//                //dialog.setConfirmButtonTheme("error primary");
//                dialog.open();
//            });
//            ++numOptions;
//
//            if (numOptions == 0) {
//                contextMenu.addItem("(no actions available)").setEnabled(false);
//            }
//
//            return true;
//        });

		refresh();
	}

	void refresh() {
		Dataset ds = RDFDataMgr.loadDataset("opendataportals.ttl");		
		
        SparqlQueryParser parser = SparqlQueryParserImpl.create(SparqlParserConfig.newInstance()
                .setSharedPrefixes(DefaultPrefixes.get())
        		.setIrixResolverAsGiven()
                .setBaseURI(""));

        List<Var> vars = Var.varList(Arrays.asList("Name", "Technology"));
		Query q = parser.apply("SELECT * { [] rdfs:label ?Name ; eg:url ?Url ; eg:technology ?Technology }");
		
		VaadinSparqlUtils.setQueryForGridBinding(
				catalogsGrid,
				(Query query) -> QueryExecutionDecoratorTxn.wrap(QueryExecutionFactory.create(query, ds), ds),
				q,
				vars);
		
		VaadinSparqlUtils.configureGridFilter(catalogsGrid, vars);

	}
}
