package org.aksw.dcat_suite.app.vaadin.view;

import org.aksw.dcat.jena.domain.api.DcatDistribution;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;

public class DistributionDetails
	extends VerticalLayout
{
	protected DcatDistribution dcatDistribution;

	// Select box for linking a distribution to a data resource (e.g. file)
	protected Select<Resource> resourceLinkSelect;
	
	public DistributionDetails() {
		add(new Paragraph("details"));
		
		resourceLinkSelect = new Select<>();
		resourceLinkSelect.setEmptySelectionAllowed(true);
		resourceLinkSelect.setLabel("Size");
		resourceLinkSelect.setItems(ResourceFactory.createResource("XS"));
		resourceLinkSelect.setPlaceholder("Select size");
		
		add(resourceLinkSelect);
	}
	
	
	public void setDistribution(Resource dcatDistribution) {
		this.dcatDistribution = dcatDistribution.as(DcatDistribution.class);
	}
}
