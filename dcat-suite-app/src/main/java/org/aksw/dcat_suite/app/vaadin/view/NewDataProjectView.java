package org.aksw.dcat_suite.app.vaadin.view;

import org.aksw.dcat_suite.app.model.api.GroupMgr;
import org.aksw.dcat_suite.app.model.api.GroupMgrFactory;
import org.aksw.dcat_suite.app.vaadin.layout.DmanMainLayout;
import org.aksw.dcat_suite.app.vaadin.layout.DmanRoutes;
import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;

@Route(value = DmanRoutes.NEW_DATA_PROJECT, layout = DmanMainLayout.class)
public class NewDataProjectView
    extends VerticalLayout
{
    protected TextField groupIdField;
    protected Button createBtn;

    // Helper penal to center components in a box
    protected FormLayout form;
    protected VerticalLayout panel;

    protected GroupMgrFactory dcatRepoMgr;

    public NewDataProjectView(
            @Autowired GroupMgrFactory dcatRepoMgr
    ) {
        this.dcatRepoMgr = dcatRepoMgr;

        // setDefaultHorizontalComponentAlignment(Alignment.CENTER);

        panel = new VerticalLayout();
        panel.getStyle().set("border-style", "solid");
        panel.getStyle().set("border-color", "#808080");
        panel.setWidth("500px");

        H1 h1 = new H1("New Data Project");
        Paragraph paragraph = new Paragraph("A group id is needed to set up a data project. You must be authorized to create data projects in that group.");

        form = new FormLayout();
        form.setWidthFull();
        form.setResponsiveSteps(
            // Use one column by default
            new ResponsiveStep("0", 1)
        );

        groupIdField = new TextField();
        groupIdField.setPlaceholder("e.g. org.mydomain.myproject");
        groupIdField.setWidth(100, Unit.PERCENTAGE);

        createBtn = new Button("Create");
        createBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_LARGE);

        form.addFormItem(groupIdField, "Group ID");

        panel.add(h1);
        panel.add(paragraph);
        panel.add(form);
        panel.add(createBtn);

        add(panel);


        // TODO Whenever the value changes check whether the group can be created.
        // If not then show an error and disable the create button; if the group already exists change 'create' to 'show'.
        // groupIdField.addValueChangeListener(null)

        createBtn.addClickListener(ev -> {
            String groupId = groupIdField.getValue();

            GroupMgr groupMgr = dcatRepoMgr.create(groupId);
            groupMgr.createIfNotExists();


            UI.getCurrent().navigate(DmanRoutes.group(groupId));
        });

    }
}
