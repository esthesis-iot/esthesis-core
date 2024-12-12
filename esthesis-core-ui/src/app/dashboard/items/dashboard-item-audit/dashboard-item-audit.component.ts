import {Component, OnInit} from "@angular/core";
import {
  DashboardItemGenericComponent
} from "../dashboard-item-generic/dashboard-item-generic.component";
import {DashboardService} from "../../dashboard.service";
import {DashboardUpdateAuditDto} from "../../dto/updates/DashboardUpdateAuditDto";
import {
  DashboardItemAuditConfigurationDto
} from "../../dto/configuration/dashboard-item-audit-configuration-dto";

@Component({
  selector: "app-dashboard-item-audit",
  templateUrl: "./dashboard-item-audit.component.html"
})
export class DashboardItemAuditComponent extends DashboardItemGenericComponent implements OnInit {
  auditItems?: DashboardUpdateAuditDto[];
  config?: DashboardItemAuditConfigurationDto;
  protected readonly Number = Number;

  constructor(private dashboardService: DashboardService) {
    super();
  }

  ngOnInit(): void {
    if (this.item.configuration) {
      this.config = JSON.parse(this.item.configuration!) as DashboardItemAuditConfigurationDto;
    }
    this.dashboardService.getMessage().subscribe((message) => {
      if (message.audit) {
        this.auditItems = message.audit;
      }
    });
  }

}
