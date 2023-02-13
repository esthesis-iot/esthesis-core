import {Component} from "@angular/core";
import {SidebarItemDto, SidebarItemType} from "./sidebar-item.dto";
import {
  faAddressCard,
  faBell,
  faBoxArchive,
  faBroadcastTower,
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
  faTag
} from "@fortawesome/free-solid-svg-icons";

@Component({
  selector: "app-sidebar",
  templateUrl: "./sidebar.component.html",
  styleUrls: ["./sidebar.component.scss"]
})
export class SidebarComponent {
  sidebar = true;

  menuItems: SidebarItemDto[] = [
    {icon: faDashboard, title: "Dashboard", route: "dashboard", type: SidebarItemType.ITEM},
    {type: SidebarItemType.CATEGORY, title: "Devices"},
    {icon: faMicrochip, title: "Devices", route: "devices", type: SidebarItemType.ITEM},
    {icon: faBroadcastTower, title: "Command", route: "command", type: SidebarItemType.ITEM},
    {type: SidebarItemType.CATEGORY, title: "Provisioning"},
    {icon: faCubes, title: "Provisioning", route: "provisioning", type: SidebarItemType.ITEM},
    {icon: faGlobe, title: "Campaigns", route: "campaigns", type: SidebarItemType.ITEM},
    {type: SidebarItemType.CATEGORY, title: "Security"},
    {icon: faCertificate, title: "Certificates", route: "certificates", type: SidebarItemType.ITEM},
    {icon: faStamp, title: "CAs", route: "cas", type: SidebarItemType.ITEM},
    {icon: faShieldHalved, title: "Stores", route: "stores", type: SidebarItemType.ITEM},
    {type: SidebarItemType.CATEGORY, title: "Integrations"},
    {icon: faDiagramProject, title: "Dataflows", route: "dataflow", type: SidebarItemType.ITEM},
    {icon: faNetworkWired, title: "Infrastructure", route: "infrastructure", type: SidebarItemType.ITEM},
    {icon: faDesktop, title: "Applications", route: "applications", type: SidebarItemType.ITEM},
    {type: SidebarItemType.CATEGORY, title: "Settings"},
    {icon: faTag, title: "Tags", route: "tags", type: SidebarItemType.ITEM},
    {icon: faGear, title: "Settings", route: "settings", type: SidebarItemType.ITEM},
    {type: SidebarItemType.CATEGORY, title: "Monitoring"},
    {icon: faBell, title: "Users", route: "users", type: SidebarItemType.ITEM},
    {icon: faBoxArchive, title: "Audit", route: "audit", type: SidebarItemType.ITEM},
    {type: SidebarItemType.CATEGORY, title: "About"},
    {icon: faAddressCard, title: "About", route: "about", type: SidebarItemType.ITEM},
  ];
  menuItemType = SidebarItemType;

  toggleSidebar() {
    this.sidebar = !this.sidebar;
  }
}
