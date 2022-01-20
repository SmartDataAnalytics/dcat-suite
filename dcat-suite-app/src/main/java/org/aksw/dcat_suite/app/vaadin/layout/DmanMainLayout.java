package org.aksw.dcat_suite.app.vaadin.layout;

import org.aksw.dcat_suite.app.HomeView;
import org.aksw.dcat_suite.app.vaadin.view.ConnectionMgmtView;
import org.aksw.dcat_suite.app.vaadin.view.NewDataProjectView;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;


@CssImport(value = "./styles/shared-styles.css", include = "lumo-badge")
@CssImport(value = "./styles/vaadin-text-field-styles.css", themeFor = "vaadin-text-field")
@CssImport(value = "./styles/vaadin-grid-styles.css", themeFor = "vaadin-grid")
//@CssImport(value = "./styles/vaadin-tab-styles.css", themeFor = "vaadin-tab")
@CssImport(value = "./styles/vaadin-select-text-field-styles.css", themeFor = "vaadin-select-text-field")
@CssImport(value = "./styles/vaadin-select-styles.css", themeFor = "vaadin-select")
@CssImport(value = "./styles/vaadin-text-area-styles.css", themeFor = "vaadin-text-area")
@CssImport(value = "./styles/flow-component-renderer-styles.css", themeFor = "flow-component-renderer")
@CssImport(value = "./styles/vaadin-grid-tree-toggle-styles.css", themeFor = "vaadin-grid-tree-toggle")
// @JsModule("@vaadin/vaadin-lumo-styles/presets/compact.js")
@Theme(value = Lumo.class)
@JsModule("@vaadin/vaadin-lumo-styles/badge.js")
public class DmanMainLayout
    extends AppLayout
{
    protected DrawerToggle drawerToggle;
    protected Button mainViewBtn;
    protected Button newDataProjectBtn;
    protected Button connectionMgmtBtn;


    public DmanMainLayout () {
        drawerToggle = new DrawerToggle();

        H1 title = new H1("MClient Data Manager");
        title.getStyle()
          .set("font-size", "var(--lumo-font-size-l)")
          .set("margin", "0");

        mainViewBtn = new Button("Home", VaadinIcon.HOME.create());
        mainViewBtn.setWidthFull();

        newDataProjectBtn = new Button("New Data Project", VaadinIcon.FOLDER_ADD.create());
        // newDataProjectBtn.
        newDataProjectBtn.setWidthFull();

        connectionMgmtBtn = new Button("Connections", VaadinIcon.CONNECT.create());
        connectionMgmtBtn.setWidthFull();

        mainViewBtn.addClickListener(ev -> {
            UI.getCurrent().navigate(DmanRoutes.HOME);
        });

        newDataProjectBtn.addClickListener(ev -> {
            UI.getCurrent().navigate(DmanRoutes.NEW_DATA_PROJECT);
        });

        connectionMgmtBtn.addClickListener(ev -> {
            UI.getCurrent().navigate(DmanRoutes.CONNECTIONS);
        });

        setDrawerOpened(true);


        addToNavbar(drawerToggle, title);

        addToDrawer(getTabs());
    }


    private Tabs getTabs() {
      Tabs tabs = new Tabs();
      tabs.add(
        createTab(VaadinIcon.HOME, "Home", HomeView.class),
        createTab(VaadinIcon.FOLDER_ADD, "New Data Project", NewDataProjectView.class),
        createTab(VaadinIcon.CONNECT, "Connections", ConnectionMgmtView.class)
      );
      tabs.setOrientation(Tabs.Orientation.VERTICAL);
      return tabs;
    }

    private Tab createTab(VaadinIcon viewIcon, String viewName, Class<? extends Component> routeClass) {
      Icon icon = viewIcon.create();
      icon.getStyle()
              .set("box-sizing", "border-box")
              .set("margin-inline-end", "var(--lumo-space-m)")
              .set("margin-inline-start", "var(--lumo-space-xs)")
              .set("padding", "var(--lumo-space-xs)");

      RouterLink link = new RouterLink();
      link.add(icon, new Span(viewName));
      link.setTabIndex(-1);
      link.setRoute(routeClass);

      return new Tab(link);
    }
}
