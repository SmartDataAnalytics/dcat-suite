package org.aksw.dcat_suite.app.vaadin.view;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nimbusds.jose.util.StandardCharset;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.TextArea;

import de.f0rce.ace.AceEditor;

public class FileDetailsDialog
	extends VerticalLayout
{
	private static final Logger logger = LoggerFactory.getLogger(FileDetailsDialog.class);
	
	protected Path basePath;
	protected Path relPath;
	
	protected Dataset metadata;
	
	protected VerticalLayout tabContent;
	
	protected TextArea fileContent;
	protected TextArea fileMetadata;

	protected Tab contentTab;
	protected Tab metadataTab;

	public FileDetailsDialog(Path basePath, Path relPath, Dataset metadata) {
		this.basePath = basePath;
		this.relPath = relPath;
		
		
		this.metadata = metadata;
		
		Path absPath = basePath.resolve(relPath);
		boolean isText;
		float TEXT_THRESHOLD = 0.8f;
		try {
			float textRatio = CharacterSourceUtils.getTextRatio(absPath, StandardCharsets.UTF_8);
			logger.debug("Text ratio for " + absPath + ": " + textRatio);
			isText = textRatio > TEXT_THRESHOLD;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		
		Tabs tabs = new Tabs();
		
//		fileContent = new AceEditor();
//		fileMetadata = new AceEditor();
		fileContent = new TextArea();
		fileMetadata = new TextArea();
		
		contentTab = new Tab("Content");
		metadataTab = new Tab("Metadata");
		
		tabs.add(contentTab, metadataTab);
		
		tabs.addSelectedChangeListener(event -> setContent(event.getSelectedTab()));
		
		tabContent = new VerticalLayout();
		
		
		add(tabs, tabContent);
		tabContent.add(fileContent, fileMetadata);
		
		setContent(contentTab);
		
		String iri = relPath.toString();
		Model model = metadata.getNamedModel(iri);
		fileMetadata.setValue(model.toString());
				
		if (isText) {
			try {
				String str = Files.lines(absPath, StandardCharset.UTF_8).collect(Collectors.joining("\n"));
				fileContent.setValue(str);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		
	}
	
	protected void setContent(Tab tab) {
		// tabContent.removeAll();
		fileContent.setVisible(false);
		fileMetadata.setVisible(false);
		if (contentTab.equals(tab)) {
			// tabContent.add(fileContent);
			fileContent.setVisible(true);
		} else if (contentTab.equals(metadataTab)) {
			//tabContent.add(fileMetadata);
			fileMetadata.setVisible(true);
		}
	}
}
