package org.aksw.dcat_suite.app.vaadin.view;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.aksw.commons.io.util.UriToPathUtils;
import org.aksw.commons.io.util.UriUtils;
import org.aksw.dcat.jena.domain.api.DcatDataset;
import org.aksw.dcat.repo.api.CatalogResolver;
import org.aksw.dcat.repo.impl.ckan.CatalogResolverCkan;
import org.aksw.dcat_suite.app.model.api.GroupMgr;
import org.aksw.dcat_suite.app.model.api.GroupMgrFactory;
import org.aksw.dcat_suite.app.vaadin.layout.DmanMainLayout;
import org.aksw.dcat_suite.app.vaadin.layout.DmanRoutes;
import org.aksw.dcat_suite.cli.cmd.file.DcatRepoLocal;
import org.aksw.dcat_suite.cli.main.MainCliDcatSuite;
import org.aksw.jenax.sparql.query.rx.RDFDataMgrEx;
import org.apache.jena.query.Dataset;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.system.Txn;
import org.springframework.beans.factory.annotation.Autowired;

import com.github.jsonldjava.shaded.com.google.common.collect.Iterables;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import de.f0rce.ace.AceEditor;
import de.f0rce.ace.enums.AceMode;
import de.f0rce.ace.enums.AceTheme;
import eu.trentorise.opendata.jackan.CkanClient;

@Route(value = DmanRoutes.CKAN_IMPORT, layout = DmanMainLayout.class)
@PageTitle("Ckan Import")
public class CkanImportView
	extends VerticalLayout
	implements BeforeEnterObserver
{
	private static final long serialVersionUID = 1L;

	protected String catalogUrl;
	protected String datasetId;
	
	protected DcatDataset dcatDataset;
	protected TextField groupField;
	
	@Autowired
	protected GroupMgrFactory groupMgrFactory;
	
	@Override
	public void beforeEnter(BeforeEnterEvent event) {
		Map<String, List<String>> params = event.getLocation().getQueryParameters().getParameters();
		catalogUrl = Iterables.getOnlyElement(params.get("url"));
		datasetId = Iterables.getOnlyElement(params.get("id"));
		refresh();
	}

	public void refresh() {
		removeAll();
		
		add(new H1("CKAN Import"));
		add(new Paragraph(datasetId + " from " + catalogUrl));

		// TODO Avoid re-downloading the data when we come from the search view
		CatalogResolver catalogResolver = new CatalogResolverCkan(new CkanClient(catalogUrl));
		dcatDataset = catalogResolver.resolveDataset(datasetId)
				.map(dr -> dr.getDataset())
				.map(ds -> MainCliDcatSuite.skolemizeDcatDataset(ds, catalogUrl))
				.blockingGet();

		URI uri = UriUtils.newURI(catalogUrl);
		String[] hostSegments = UriToPathUtils.javaifyHostnameSegments(uri.getHost());
		String groupId = Arrays.asList(hostSegments).stream().collect(Collectors.joining("."));

		
		groupField = new TextField("GroupId", groupId, "org.mydomain.mygroup");
//		groupField.addKeyDownListener(com.vaadin.flow.component.Key.ENTER, ev -> doImport());

		Button importBtn = new Button(VaadinIcon.CHECK.create());
		groupField.setSuffixComponent(importBtn);
		
		importBtn.addClickShortcut(Key.ENTER);
		importBtn.addClickListener(ev -> doImport());

		add(groupField);

		String str = RDFDataMgrEx.toString(dcatDataset.getModel(), RDFFormat.TURTLE_PRETTY);

		
		AceEditor textArea = new AceEditor();
		textArea.setWidthFull();
		textArea.setValue(str);
		textArea.setMode(AceMode.turtle);
        textArea.setTheme(AceTheme.chrome);
        textArea.setFontSize(18);
        textArea.setMinHeight(10, Unit.EM);
				
		add(textArea);

	}

	public void doImport() {
		String groupId = groupField.getValue();
		GroupMgr groupMgr = groupMgrFactory.create(groupId);
		groupMgr.createIfNotExists();
		DcatRepoLocal dcatRepo = groupMgr.get();
		Dataset dataset = dcatRepo.getDataset();
		
		Txn.executeWrite(dataset, () -> {
			String u = dcatDataset.getURI();
			dataset.getNamedModel(u).add(dcatDataset.getModel());
		});
	}
	
}

