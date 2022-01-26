package org.aksw.dcat_suite.app.vaadin.view;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.aksw.commons.io.util.PathUtils;
import org.aksw.dcat_suite.app.qualifier.FileStore;
import org.aksw.dcat_suite.app.vaadin.layout.DmanMainLayout;
import org.aksw.dcat_suite.app.vaadin.layout.DmanRoutes;

import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteParameters;
import com.vaadin.flow.router.RouterLink;

@Route(value = DmanRoutes.BROWSE, layout = DmanMainLayout.class)
public class BrowseRepoView
	extends FileBrowserComponent
{
	public BrowseRepoView(@FileStore Path path) {
		super(path);

//		fileGrid.setItemDetailsRenderer(new ComponentRenderer<>(path -> new Component(), c::set));
		fileGrid.setItemDetailsRenderer(new ComponentRenderer<>(tipPath -> {
			Path absPath = path.resolve(activePath.get()).resolve(tipPath);
			Path relPath = path.relativize(absPath);
			
			
			
			Path repoPath = relPath.getParent();
			String groupId = Arrays.asList(PathUtils.getPathSegments(repoPath)).stream()
						.map(Object::toString).collect(Collectors.joining("."));
			
			return new RouterLink("Visit", DataProjectMgmtView.class, new RouteParameters("groupId", groupId));
		}));
	}

}
