package org.aksw.dcat_suite.app.vaadin.view;

import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.TextField;

public class GavFormMixin {
	protected TextField groupIdField;
	protected TextField artifactIdField;
	protected TextField versionField;

	public GavFormMixin() {
		groupIdField = new TextField();
		artifactIdField = new TextField();
		versionField = new TextField();
	}
	
	public String getGroupId() {
		return groupIdField.getValue();
	}

	public String getArtifactId() {
		return artifactIdField.getValue();
	}
	public String getVersion() {
		return versionField.getValue();
	}

	public void addTo(FormLayout formLayout) {
		formLayout.addFormItem(groupIdField, "GroudId");
		formLayout.addFormItem(artifactIdField, "ArtifactId");
		formLayout.addFormItem(versionField, "Version");
	}
}