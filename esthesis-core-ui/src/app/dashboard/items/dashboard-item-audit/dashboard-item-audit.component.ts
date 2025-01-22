import {Component} from "@angular/core";
import {DashboardUpdateAuditDto} from "../../dto/updates/DashboardUpdateAuditDto";
import {
  DashboardItemAuditConfigurationDto
} from "../../dto/configuration/dashboard-item-audit-configuration-dto";
import {DashboardItemComponent} from "../dashboard-item.component";

@Component({
  selector: "app-dashboard-item-audit",
  templateUrl: "./dashboard-item-audit.component.html"
})
export class DashboardItemAuditComponent extends DashboardItemComponent<DashboardUpdateAuditDto, DashboardItemAuditConfigurationDto> {
  protected readonly Object = Object;

}
