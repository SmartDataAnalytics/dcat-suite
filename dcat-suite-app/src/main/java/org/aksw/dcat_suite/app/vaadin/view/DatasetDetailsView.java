package org.aksw.dcat_suite.app.vaadin.view;

import org.aksw.dcat.jena.domain.api.DcatDataset;
import org.aksw.jena_sparql_api.common.DefaultPrefixes;
import org.aksw.jena_sparql_api.vaadin.util.GridWrapperBase;
import org.aksw.jena_sparql_api.vaadin.util.VaadinSparqlUtils;
import org.aksw.jenax.arq.util.syntax.QueryUtils;
import org.aksw.jenax.dataaccess.sparql.execution.query.QueryExecutionWrapperTxn;
import org.aksw.jenax.stmt.core.SparqlParserConfig;
import org.aksw.jenax.stmt.parser.query.SparqlQueryParser;
import org.aksw.jenax.stmt.parser.query.SparqlQueryParserImpl;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.vocabulary.DCAT;
import org.claspina.confirmdialog.ConfirmDialog;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;

public class DatasetDetailsView
    extends VerticalLayout
{
    protected DcatDataset dcatDataset;

    protected Runnable fixParentHeight;
    protected Grid<Resource> distributionsGrid;


    public DatasetDetailsView(Runnable fixParentHeight) {
        this.fixParentHeight = fixParentHeight;

        add(new Paragraph("Distributions"));

        distributionsGrid = new Grid<>();
        distributionsGrid.setHeightByRows(true);
        distributionsGrid.setItemDetailsRenderer(new ComponentRenderer<>(DistributionDetails::new , DistributionDetails::setDistribution));

        GridContextMenu<Resource> contextMenu = distributionsGrid.addContextMenu();

        contextMenu.setDynamicContentHandler(r -> {
            // Resource r = qs.getResource("s");
            contextMenu.removeAll();

            int numOptions = 0;
            contextMenu.addItem("Actions for " + r).setEnabled(false);
            contextMenu.add(new Hr());

            contextMenu.addItem("Delete", ev -> {
                ConfirmDialog dialog = VaadinDialogUtils.confirmDialog("Confirm delete",
                        String.format("You are about to delete: %s (affects %d triples). This operation cannot be undone.", r.asNode(), dcatDataset.getModel().size()),
                        "Delete", x -> {
                            dcatDataset.getModel().remove(dcatDataset, DCAT.distribution, r);
//                        	Txn.executeWrite(dcatDataset, () -> {
//                        		dcatDataset.asDatasetGraph().removeGraph(r.asNode());
//                        	});
//                        	updateView();

                            setDataset(dcatDataset);
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


        add(distributionsGrid);

        Button addDistributionBtn = new Button("Add distribution");

        addDistributionBtn.addClickListener(ev -> {
            if (dcatDataset != null) {
                dcatDataset.addProperty(DCAT.distribution, ResourceFactory.createResource());
                setDataset(dcatDataset); // refresh
                UI.getCurrent().accessSynchronously(() -> {
                    distributionsGrid.getElement().executeJs("requestAnimationFrame((function() { this.notifyResize(); }).bind(this))");
                    fixParentHeight.run();
                });
                // https://vaadin.com/forum/thread/17019831/grid-items-details-height-bug-with-content-loaded-asynchronously
            }
        });

        add(addDistributionBtn);

    }

    void setDataset(Resource resource) {
        this.dcatDataset = resource.as(DcatDataset.class);

        Model model = dcatDataset.getModel();
        Dataset ds = DatasetFactory.wrap(model);

        SparqlQueryParser parser = SparqlQueryParserImpl.create(SparqlParserConfig.newInstance()
                .setSharedPrefixes(DefaultPrefixes.get())
                .setIrixResolverAsGiven()
                .setBaseURI(""));

        Query q = parser.apply("SELECT ?distribution { ?dataset dcat:distribution ?distribution }");
        QueryUtils.injectFilter(q, "dataset", dcatDataset.asNode());

        VaadinSparqlUtils.setQueryForGridResource(
                new GridWrapperBase<>(distributionsGrid),
                (Query query) -> QueryExecutionWrapperTxn.wrap(QueryExecutionFactory.create(query, ds), ds),
                q,
                Resource.class,
                null,
                null);


    }
}
