import {Component, OnDestroy} from "@angular/core";
import {SidebarItemDto, SidebarItemType} from "./sidebar-item.dto";
import {
  faAddressCard,
  faBroadcastTower,
  faBuildingShield,
  faCertificate,
  faCubes,
  faDashboard,
  faDesktop,
  faDiagramProject,
  faGear,
  faGlobe,
  faMicrochip,
  faNetworkWired,
  faShieldHalved,
  faStamp,
  faTag,
  faUser,
  faUsers,
  faUsersBetweenLines,
  faXmarksLines
} from "@fortawesome/free-solid-svg-icons";
import {SecurityService} from "../../security/security.service";
import {AppConstants} from "../../app.constants";

@Component({
  selector: "app-sidebar",
  templateUrl: "./sidebar.component.html"
})
export class SidebarComponent implements OnDestroy {
  menuItemType = SidebarItemType;
  sidebar = true;
  menuItems: SidebarItemDto[] = [];
  private categoryGeneral: SidebarItemDto[] = [];
  private categoryDevices: SidebarItemDto[] = [];
  private categoryKeyManagement: SidebarItemDto[] = [];
  private categoryIntegrations: SidebarItemDto[] = [];
  private categorySettings: SidebarItemDto[] = [];
  private categorySecurity: SidebarItemDto[] = [];
  private categoryAbout: SidebarItemDto[] = [];
  private authSubscription;

  constructor(private securityService: SecurityService) {
    this.authSubscription = this.securityService.isAuthDone().subscribe((authResult) => {
      if (authResult) {
        this.buildSidebar();
      }
    });
  }

  buildSidebar() {
    // GENERAL
    if (this.securityService.isPermitted(AppConstants.SECURITY.CATEGORY.DASHBOARD, AppConstants.SECURITY.OPERATION.READ)) {
      this.categoryGeneral.push({icon: faDashboard, title: "Dashboard", route: "dashboard", type: SidebarItemType.ITEM});
    }

    // DEVICES
    if (this.securityService.isPermitted(AppConstants.SECURITY.CATEGORY.DEVICE, AppConstants.SECURITY.OPERATION.READ)) {
      this.categoryDevices.push({icon: faMicrochip, title: "Devices", route: "devices", type: SidebarItemType.ITEM});
    }
    if (this.securityService.isPermitted(AppConstants.SECURITY.CATEGORY.COMMAND, AppConstants.SECURITY.OPERATION.READ)) {
      this.categoryDevices.push({icon: faBroadcastTower, title: "Command", route: "command", type: SidebarItemType.ITEM});
    }
    if (this.securityService.isPermitted(AppConstants.SECURITY.CATEGORY.PROVISIONING, AppConstants.SECURITY.OPERATION.READ)) {
      this.categoryDevices.push({icon: faCubes, title: "Provisioning", route: "provisioning", type: SidebarItemType.ITEM});
    }
    if (this.securityService.isPermitted(AppConstants.SECURITY.CATEGORY.CAMPAIGN, AppConstants.SECURITY.OPERATION.READ)) {
      this.categoryDevices.push({icon: faGlobe, title: "Campaigns", route: "campaigns", type: SidebarItemType.ITEM});
    }

    // KEY MANAGEMENT
    if (this.securityService.isPermitted(AppConstants.SECURITY.CATEGORY.CERTIFICATES, AppConstants.SECURITY.OPERATION.READ)) {
      this.categoryKeyManagement.push({icon: faCertificate, title: "Certificates", route: "certificates", type: SidebarItemType.ITEM});
    }
    if (this.securityService.isPermitted(AppConstants.SECURITY.CATEGORY.CA, AppConstants.SECURITY.OPERATION.READ)) {
      this.categoryKeyManagement.push({icon: faStamp, title: "CAs", route: "cas", type: SidebarItemType.ITEM});
    }
    if (this.securityService.isPermitted(AppConstants.SECURITY.CATEGORY.KEYSTORE, AppConstants.SECURITY.OPERATION.READ)) {
      this.categoryKeyManagement.push({icon: faShieldHalved, title: "Keystores", route: "keystores", type: SidebarItemType.ITEM});
    }

    // INTEGRATIONS
    if (this.securityService.isPermitted(AppConstants.SECURITY.CATEGORY.DATAFLOW, AppConstants.SECURITY.OPERATION.READ)) {
      this.categoryIntegrations.push({icon: faDiagramProject, title: "Dataflows", route: "dataflow", type: SidebarItemType.ITEM});
    }
    if (this.securityService.isPermitted(AppConstants.SECURITY.CATEGORY.INFRASTRUCTURE, AppConstants.SECURITY.OPERATION.READ)) {
      this.categoryIntegrations.push({icon: faNetworkWired, title: "Infrastructure", route: "infrastructure", type: SidebarItemType.ITEM});
    }
    if (this.securityService.isPermitted(AppConstants.SECURITY.CATEGORY.APPLICATION, AppConstants.SECURITY.OPERATION.READ)) {
      this.categoryIntegrations.push({icon: faDesktop, title: "Applications", route: "applications", type: SidebarItemType.ITEM});
    }

    // SETINGS
    if (this.securityService.isPermitted(AppConstants.SECURITY.CATEGORY.TAG, AppConstants.SECURITY.OPERATION.READ)) {
      this.categorySettings.push({icon: faTag, title: "Tags", route: "tags", type: SidebarItemType.ITEM});
    }
    if (this.securityService.isPermitted(AppConstants.SECURITY.CATEGORY.SETTINGS, AppConstants.SECURITY.OPERATION.READ)) {
      this.categorySettings.push({icon: faGear, title: "Settings", route: "settings", type: SidebarItemType.ITEM});
    }

    // SECURITY
    if (this.securityService.isPermitted(AppConstants.SECURITY.CATEGORY.USERS, AppConstants.SECURITY.OPERATION.READ)) {
      this.categorySecurity.push({icon: faUser, title: "Users", route: "security/users", type: SidebarItemType.ITEM});
    }
    if (this.securityService.isPermitted(AppConstants.SECURITY.CATEGORY.GROUPS, AppConstants.SECURITY.OPERATION.READ)) {
      this.categorySecurity.push({icon: faUsers, title: "Groups", route: "security/groups", type: SidebarItemType.ITEM});
    }
    if (this.securityService.isPermitted(AppConstants.SECURITY.CATEGORY.ROLES, AppConstants.SECURITY.OPERATION.READ)) {
      this.categorySecurity.push({icon: faUsersBetweenLines, title: "Roles", route: "security/roles", type: SidebarItemType.ITEM});
    }
    if (this.securityService.isPermitted(AppConstants.SECURITY.CATEGORY.POLICIES, AppConstants.SECURITY.OPERATION.READ)) {
      this.categorySecurity.push({icon: faBuildingShield, title: "Policies", route: "security/policies", type: SidebarItemType.ITEM});
    }
    if (this.securityService.isPermitted(AppConstants.SECURITY.CATEGORY.AUDIT, AppConstants.SECURITY.OPERATION.READ)) {
      this.categorySecurity.push({icon: faXmarksLines, title: "Audit", route: "audit", type: SidebarItemType.ITEM});
    }

    // ABOUT
    if (this.securityService.isPermitted(AppConstants.SECURITY.CATEGORY.ABOUT, AppConstants.SECURITY.OPERATION.READ)) {
      this.categoryAbout.push({icon: faAddressCard, title: "About", route: "about", type: SidebarItemType.ITEM});
    }

    // Create sidebar.
    if (this.categoryGeneral.length > 0) {
      this.menuItems = this.menuItems.concat(this.categoryGeneral);
    }
    if (this.categoryDevices.length > 0) {
      this.menuItems = this.menuItems.concat(
        {type: SidebarItemType.CATEGORY, title: "Devices"}, this.categoryDevices);
    }
    if (this.categoryKeyManagement.length > 0) {
      this.menuItems = this.menuItems.concat(
        {type: SidebarItemType.CATEGORY, title: "Key Management"}, this.categoryKeyManagement);
    }
    if (this.categoryIntegrations.length > 0) {
      this.menuItems = this.menuItems.concat(
        {type: SidebarItemType.CATEGORY, title: "Integrations"}, this.categoryIntegrations);
    }
    if (this.categorySettings.length > 0) {
      this.menuItems = this.menuItems.concat(
        {type: SidebarItemType.CATEGORY, title: "Settings"}, this.categorySettings);
    }
    if (this.categorySecurity.length > 0) {
      this.menuItems = this.menuItems.concat(
        {type: SidebarItemType.CATEGORY, title: "Security"}, this.categorySecurity);
    }
    if (this.categoryAbout.length > 0) {
      this.menuItems = this.menuItems.concat(
        {type: SidebarItemType.CATEGORY, title: "About"}, this.categoryAbout);
    }
  }

  ngOnDestroy(): void {
    this.authSubscription.unsubscribe();
  }

  toggleSidebar() {
    this.sidebar = !this.sidebar;
  }
}
