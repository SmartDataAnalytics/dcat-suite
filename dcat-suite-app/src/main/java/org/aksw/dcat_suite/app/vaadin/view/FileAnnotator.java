package org.aksw.dcat_suite.app.vaadin.view;

import java.nio.file.Path;

import org.aksw.jenax.arq.dataset.api.DatasetOneNg;

public interface FileAnnotator {
	public DatasetOneNg annotate(Path path);
}
