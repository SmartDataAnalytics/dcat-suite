package org.aksw.dcat_suite.app.vaadin.view;

import java.nio.file.Path;

import org.aksw.dcat_suite.app.qualifier.FileStore;
import org.aksw.dcat_suite.app.vaadin.layout.DmanMainLayout;
import org.aksw.dcat_suite.app.vaadin.layout.DmanRoutes;

import com.vaadin.flow.router.Route;

@Route(value = DmanRoutes.BROWSE, layout = DmanMainLayout.class)
public class BrowseRepoView
	extends FileBrowserComponent
{
	public BrowseRepoView(@FileStore Path path) {
		super(path);
	}

}
