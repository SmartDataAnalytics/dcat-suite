package org.aksw.dcat_suite.app.vaadin.view;

import java.nio.file.Path;

import org.aksw.dcat_suite.cli.cmd.file.DcatRepoLocalUtils;
import org.aksw.jena_sparql_api.conjure.utils.ContentTypeUtils;
import org.aksw.jena_sparql_api.http.domain.api.RdfEntityInfo;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;

public class DatasetAndDistributionFromFile
	extends FormLayout
{
	// protected DcatRepoLocal dcatRepo;
	protected Path path;
	
	protected ComboBox<String> datasetId;
	protected ComboBox<String> distributionId;
	
	protected Button okBtn;
	protected Button cancelBtn;
	
	public DatasetAndDistributionFromFile(Path path) {
		setResponsiveSteps(
				new ResponsiveStep("0", 1)
		);

		this.path = path;
		
		
		datasetId = new ComboBox<>();
		datasetId.setAllowCustomValue(true);
		datasetId.setItems();
		
		distributionId = new ComboBox<>();
		distributionId.setAllowCustomValue(true);
		distributionId.setItems();
		
//		okBtn = new Button("Ok");
//		cancelBtn = new Button("Cancel");
//	
		addFormItem(datasetId, "Dataset");
		addFormItem(distributionId, "Distribution");
		
		setPath(path);
	}

	
	public void setPath(Path path) {
		this.path = path;
		
				
        RdfEntityInfo entityInfo = DcatRepoLocalUtils.probeFile(Path.of(""), path);

        // Derive the base name; remove file extensions
        String baseName = DcatRepoLocalUtils.deriveBaseName(path.getFileName().toString(), entityInfo, true);

        String distributionType = ContentTypeUtils.toFileExtension(entityInfo);
        // Cut off a preceding dot
        if (distributionType.startsWith(".")) {
            distributionType = distributionType.substring(1);
        }
        
        datasetId.setValue(baseName);
        distributionId.setValue(distributionType);
	}
}
