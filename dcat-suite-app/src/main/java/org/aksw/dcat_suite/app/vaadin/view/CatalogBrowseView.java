package org.aksw.dcat_suite.app.vaadin.view;

import java.util.List;

import org.aksw.commons.util.string.StringUtils;
import org.aksw.dcat.repo.api.CatalogResolver;
import org.aksw.dcat.repo.impl.ckan.CatalogResolverCkan;
import org.aksw.dcat_suite.app.vaadin.layout.DmanMainLayout;
import org.aksw.dcat_suite.app.vaadin.layout.DmanRoutes;
import org.apache.jena.rdf.model.Resource;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import eu.trentorise.opendata.jackan.CkanClient;

@Route(value = DmanRoutes.CKAN_IMPORT, layout = DmanMainLayout.class)
@PageTitle("Ckan Search and Import")
public class CatalogBrowseView
	extends VerticalLayout
	implements HasUrlParameter<String>
{
	protected String catalogUrl;
	
	protected TextField searchText;
	protected Button searchBtn;
	protected Grid<Resource> grid;
	
	@Override
	public void setParameter(BeforeEvent event, String parameter) {
		catalogUrl = StringUtils.urlDecode(parameter);
		
		refresh();
	}
	
	public CatalogBrowseView() {
		
	}
	
	
	public void refresh() {
		searchText = new TextField();
		searchBtn = new Button(VaadinIcon.SEARCH.create());
		searchText.setSuffixComponent(searchBtn);
		searchText.addKeyDownListener(com.vaadin.flow.component.Key.ENTER, ev -> doSearch());

		searchBtn.addClickListener(ev -> doSearch());		
		grid = new Grid<>();
		grid.addColumn(r -> r.toString());
		
		add(searchText);
		add(grid);
	}
	
	protected void doSearch() {
		CatalogResolver catalogResolver = new CatalogResolverCkan(new CkanClient(catalogUrl));
		List<Resource> list = catalogResolver.search(searchText.getValue())
				.map(dr -> dr.getDataset().asResource())
				.toList().blockingGet();
		System.out.println("Got: " + list);
		grid.setItems(list);
	}
}
