package org.aksw.dcat_suite.app.vaadin.view;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.LocalDate;
import java.time.ZoneId;

import org.aksw.dcat_suite.cli.cmd.file.DcatRepoLocalUtils;
import org.aksw.jena_sparql_api.conjure.utils.ContentTypeUtils;
import org.aksw.jena_sparql_api.http.domain.api.RdfEntityInfo;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;

public class DatasetAndDistributionFromFile
	extends FormLayout
{
	// protected DcatRepoLocal dcatRepo;
	protected Path path;
	
	protected ComboBox<String> datasetId;
	protected ComboBox<String> distributionId;
	protected ComboBox<String> version;
	
	
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

		version = new ComboBox<>();
		version.setAllowCustomValue(true);
		version.setItems("1.0.0");

//		okBtn = new Button("Ok");
//		cancelBtn = new Button("Cancel");
//	
		addFormItem(datasetId, "Dataset");
		addFormItem(distributionId, "Distribution");
		addFormItem(version, "Version");
		
		setPath(path);
	}

	public String getVersion() {
		return version.getValue();
	}

	public String getDatasetId() {
		return datasetId.getValue();
	}
	
	public String getDistributionId() {
		return distributionId.getValue();
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

        String dateStr = "";
        try {
        	FileTime time = Files.getLastModifiedTime(path);
        	LocalDate date = LocalDate.ofInstant(time.toInstant(), ZoneId.systemDefault());
        	dateStr = date.toString();
        } catch (Exception e) {
        	throw new RuntimeException(e);
        }
        
        datasetId.setValue(baseName);
        distributionId.setValue(distributionType);
    	version.setValue(dateStr);
	}
}
