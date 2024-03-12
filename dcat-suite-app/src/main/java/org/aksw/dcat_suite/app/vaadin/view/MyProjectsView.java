package org.aksw.dcat_suite.app.vaadin.view;

import java.io.IOException;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.dcat_suite.app.model.api.GroupMgrFactory;
import org.aksw.dcat_suite.app.model.api.SystemSpace;
import org.aksw.dcat_suite.app.model.api.UserProject;
import org.aksw.dcat_suite.app.model.api.UserSpace;
import org.aksw.dcat_suite.app.session.UserSession;
import org.aksw.dcat_suite.app.vaadin.layout.DmanMainLayout;
import org.aksw.jenax.model.rdfs.domain.api.HasRdfsLabel;
import org.apache.jena.rdf.model.Resource;
import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteParam;
import com.vaadin.flow.router.RouteParameters;

@Route(value = ":user", layout = DmanMainLayout.class)
public class MyProjectsView
    extends VerticalLayout
    implements BeforeEnterObserver, AfterNavigationObserver
{

    protected UserSpace userSpace;

    Grid<String> projectGrid;

    protected TextField projectNameField;
    protected Button createBtn;

    // Helper panel to center components in a box
    protected FormLayout form;
    protected VerticalLayout panel;

    // protected GroupMgrFactory dcatRepoMgr;

    protected UserSession userSession;
    protected SystemSpace systemSpace;

    public MyProjectsView(
            @Autowired GroupMgrFactory dcatRepoMgr,
            SystemSpace systemSpace,
            UserSession userSession
    ) {
        // this.dcatRepoMgr = dcatRepoMgr;
        this.systemSpace = systemSpace;
        this.userSession = userSession;

    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        String user = event.getRouteParameters().get("user").orElse(null);


        try {
            userSpace = systemSpace.getUserSpaceMgr().get(user);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        if (userSpace == null) {
            throw new RuntimeException("User not found");
        }


        // TODO if not logged in delegate to login page
    }


    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        // setDefaultHorizontalComponentAlignment(Alignment.CENTER);


        projectGrid = new Grid<>();

        // new HierarchicalDataProviderForPath(null, isTemplateMapped(), null)
        Supplier<Stream<String>> streamFactory = () -> {
                List<String> items;
                try (Stream<Resource> stream = userSpace.getProjectMgr().list()) {
                    items = stream
                            .map(item -> item.as(HasRdfsLabel.class).getLabel())
                            .collect(Collectors.toList());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                // items.stream().map(item -> item.as(RdfsLabel.class).getLabel()).collect(Collectors.toList())
                return items.stream();
        };

        DataProvider<String, String> dataProvider = new CallbackDataProvider<>(query -> streamFactory.get().skip(query.getOffset()).limit(query.getLimit()), query -> (int)streamFactory.get().count());
        projectGrid.setDataProvider(dataProvider);
        projectGrid.addColumn(item -> item);

        String accountName = userSession.getUser().getAccountName();
        projectGrid.addItemClickListener(ev -> {
            UI.getCurrent().navigate(DataProjectMgmtView.class, new RouteParameters(
                    new RouteParam("user", accountName),
                    new RouteParam("project", ev.getItem())));
        });
        dataProvider.refreshAll();

        add(projectGrid);

        panel = new VerticalLayout();
        panel.getStyle().set("border-style", "solid");
        panel.getStyle().set("border-color", "#808080");
        panel.setMaxWidth(40, Unit.EM);

        H1 h1 = new H1("New Data Project");
        Paragraph paragraph = new Paragraph("A group id is needed to set up a data project. You must be authorized to create data projects in that group.");

//        form = new FormLayout();
//        form.setWidthFull();
//        form.setResponsiveSteps(
//            // Use one column by default
//            new ResponsiveStep("0", 1)
//        );
//
        projectNameField = new TextField();
        projectNameField.setLabel("Project name");
        projectNameField.setPlaceholder("e.g. myproject");
        projectNameField.setWidthFull();


        createBtn = new Button("Create");
        createBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_LARGE);

        createBtn.addClickListener(ev -> createGroup());
        createBtn.addClickShortcut(Key.ENTER);
        // groupIdField.addKeyDownListener(com.vaadin.flow.component.Key.ENTER, ev -> createGroup());

        // form.addFormItem(groupIdField, "Group ID");

        panel.add(h1);
        panel.add(paragraph);
//        panel.add(form);
        panel.add(projectNameField);
        panel.add(createBtn);

        add(panel);


        // TODO Whenever the value changes check whether the group can be created.
        // If not then show an error and disable the create button; if the group already exists change 'create' to 'show'.
        // groupIdField.addValueChangeListener(null)

        // groupIdField.
    }

    public void createGroup() {
        String projectName = projectNameField.getValue();

        UserProject projectMgr;
        try {
            projectMgr = userSpace.getProjectMgr().get(projectName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        try {
            projectMgr.createIfNotExists();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        projectGrid.getDataProvider().refreshAll();

        // GroupMgr groupMgr = dcatRepoMgr.create(projectName);
        // groupMgr.createIfNotExists();

        UI.getCurrent().navigate(DataProjectMgmtView.class, new RouteParameters(
                new RouteParam("user", userSession.getUser().getAccountName()),
                new RouteParam("project", projectName)));
    }

}
