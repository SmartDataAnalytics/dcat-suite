package org.aksw.dcat_suite.app.vaadin.view;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.aksw.ckan_deploy.core.CKAN;
import org.aksw.dcat.repo.api.CatalogResolver;
import org.aksw.dcat.repo.impl.ckan.CatalogResolverCkan;
import org.aksw.dcat_suite.app.vaadin.layout.DmanMainLayout;
import org.aksw.dcat_suite.app.vaadin.layout.DmanRoutes;
import org.aksw.dcat_suite.cli.main.MainCliDcatSuite;
import org.apache.jena.rdf.model.Resource;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.router.WildcardParameter;

import eu.trentorise.opendata.jackan.CkanClient;

@Route(value = DmanRoutes.CKAN_SEARCH, layout = DmanMainLayout.class)
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
	public void setParameter(BeforeEvent event,  @WildcardParameter String parameter) {
		catalogUrl = parameter;// StringUtils.urlDecode(parameter);
		
		refresh();
	}
	
	public CatalogBrowseView() {
		
	}
	
	
	public static class CatalogItem
		extends VerticalLayout
	{
		protected String catalogUrl;
		protected Resource resource;
		// protected Button importBtn;
		protected RouterLink importBtn;
		
		public CatalogItem(String catalogUrl) {
			this.catalogUrl = catalogUrl;
		}
		
		public void setResource(Resource binding) {
			this.resource = binding;
			refresh();
		}
		
		public void refresh() {
			removeAll();
//			importBtn = new Button("Import...");
//			importBtn.addClickListener(ev -> navigateToImport());
			Map<String, String> args = new LinkedHashMap<>();
			args.put("url", catalogUrl);
			args.put("id", resource.getProperty(CKAN.id).getString());

			importBtn = new RouterLink("Import...", CkanImportView.class);
			importBtn.setQueryParameters(QueryParameters.simple(args));
			
			add(importBtn);
		}		
	}
	
	public void refresh() {
		searchText = new TextField();
		searchBtn = new Button(VaadinIcon.SEARCH.create());
		searchText.setSuffixComponent(searchBtn);
		searchText.addKeyDownListener(com.vaadin.flow.component.Key.ENTER, ev -> doSearch());

		searchBtn.addClickListener(ev -> doSearch());		
		grid = new Grid<>();
		grid.addColumn(r -> r.toString());
		grid.setItemDetailsRenderer(new ComponentRenderer<>(() -> new CatalogItem(catalogUrl), CatalogItem::setResource));
		add(searchText);
		add(grid);
	}
	
	protected void doSearch() {
		CatalogResolver catalogResolver = new CatalogResolverCkan(new CkanClient(catalogUrl));
		List<Resource> list = catalogResolver.search(searchText.getValue())
				.map(dr -> dr.getDataset())
				.map(ds -> MainCliDcatSuite.skolemizeDcatDataset(ds, catalogUrl))
				.map(Resource::asResource)
				.toList().blockingGet();
		System.out.println("Got: " + list);
		grid.setItems(list);
	}
}
