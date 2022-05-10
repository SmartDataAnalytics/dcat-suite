package org.aksw.dcat_suite.app.vaadin.layout;

import javax.annotation.security.PermitAll;

import org.aksw.dcat_suite.app.session.UserSession;
import org.aksw.dcat_suite.app.vaadin.view.BrowseRepoView;
import org.aksw.dcat_suite.app.vaadin.view.CatalogMgmtView;
import org.aksw.dcat_suite.app.vaadin.view.ConnectionMgmtView;
import org.aksw.dcat_suite.app.vaadin.view.DmanLandingPageView;
import org.aksw.dcat_suite.app.vaadin.view.MyProjectsView;

import com.google.common.collect.ImmutableMap;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.RouteParameters;
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
@PermitAll
public class DmanMainLayout
    extends AppLayout
{
    protected UserSession userSession;

    protected DrawerToggle drawerToggle;
    protected Button mainViewBtn;
    protected Button newDataProjectBtn;
    protected Button connectionMgmtBtn;


    public DmanMainLayout (UserSession userSession) {

        this.userSession = userSession;

        drawerToggle = new DrawerToggle();

        H1 title = new H1("MClient Data Manager");
        title.getStyle()
          .set("font-size", "var(--lumo-font-size-l)")
          .set("margin", "0");


        HorizontalLayout navbarLayout = new HorizontalLayout();
        navbarLayout.setWidthFull();
        navbarLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);


        MenuBar menuBar = new MenuBar();
        menuBar.setOpenOnHover(true);

        String accountName = userSession.getUser().getAccountName();
        String avatarUrl = userSession.getUser().getOwner().getDepiction();

        Avatar avatar = new Avatar(accountName, avatarUrl);
        MenuItem userOptions = menuBar.addItem(avatar);

        userOptions.getSubMenu().addItem("Logout", click -> {
            userSession.logout();
        });

        // setAlignItems(Alignment.CENTER);
        navbarLayout.add(menuBar);


        // setDrawerOpened(true);
        addToNavbar(drawerToggle);
        addToNavbar(title, navbarLayout);
        addToDrawer(getTabs());
    }


    private Tabs getTabs() {
        RouteTabs tabs = new RouteTabs();

        tabs.add(
                createTab(VaadinIcon.HOME, "Home", DmanLandingPageView.class, null),
                createTab(VaadinIcon.EYE, "Browse", BrowseRepoView.class, null),
                createTab(VaadinIcon.FOLDER_ADD, "My Projects", MyProjectsView.class,
                        new RouteParameters("user", userSession.getUser().getAccountName())),
                createTab(VaadinIcon.CONNECT, "Connections", ConnectionMgmtView.class, null),
                createTab(VaadinIcon.DATABASE, "Catalogs", CatalogMgmtView.class, null)
        );
        tabs.setOrientation(Tabs.Orientation.VERTICAL);
//      Tabs tabs = new Tabs();
//      tabs.add(
//        createTab(VaadinIcon.HOME, "Home", DmanLandingPageView.class),
//        createTab(VaadinIcon.FOLDER_ADD, "New Data Project", NewDataProjectView.class),
//        createTab(VaadinIcon.EYE, "Browse", BrowseRepoView.class),
//        createTab(VaadinIcon.CONNECT, "Connections", ConnectionMgmtView.class)
//      );
//      tabs.setOrientation(Tabs.Orientation.VERTICAL);
      return tabs;
    }

    private RouterLink createTab(
            VaadinIcon viewIcon, String viewName, Class<? extends Component> routeClass, RouteParameters routeParams) {
      Icon icon = viewIcon.create();
      icon.getStyle()
              .set("box-sizing", "border-box")
              .set("margin-inline-end", "var(--lumo-space-m)")
              .set("margin-inline-start", "var(--lumo-space-xs)")
              .set("padding", "var(--lumo-space-xs)");

      RouterLink link = new RouterLink();
      link.add(icon, new Span(viewName));
      link.setTabIndex(-1);
      link.setRoute(routeClass, routeParams == null ? RouteParameters.empty() : routeParams);

      // return new Tab(link);
      return link;
    }


}
