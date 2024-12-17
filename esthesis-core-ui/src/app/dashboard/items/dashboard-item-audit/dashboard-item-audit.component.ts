import {Component, OnDestroy, OnInit} from "@angular/core";
import {
  DashboardItemGenericComponent
} from "../dashboard-item-generic/dashboard-item-generic.component";
import {DashboardService} from "../../dashboard.service";
import {DashboardUpdateAuditDto} from "../../dto/updates/DashboardUpdateAuditDto";
import {
  DashboardItemAuditConfigurationDto
} from "../../dto/configuration/dashboard-item-audit-configuration-dto";
import {Subscription} from "rxjs";
import {UtilityService} from "../../../shared/services/utility.service";

@Component({
  selector: "app-dashboard-item-audit",
  templateUrl: "./dashboard-item-audit.component.html"
})
export class DashboardItemAuditComponent extends DashboardItemGenericComponent implements OnInit, OnDestroy {
  // The last message this component received.
  lastMessage?: DashboardUpdateAuditDto;
  // The dashboard item configuration.
  config?: DashboardItemAuditConfigurationDto;
  // The subscription to receive dashboard updates.
  emitterSub?: Subscription;

  constructor(private readonly dashboardService: DashboardService,
    readonly utilityService: UtilityService) {
    super();
  }

  ngOnInit(): void {
    if (this.item.configuration) {
      this.config = JSON.parse(this.item.configuration) as DashboardItemAuditConfigurationDto;
    }

    this.emitterSub = this.dashboardService.getMessage().subscribe((message) => {
      if (message.id === this.item.id) {
        this.lastMessage = message as DashboardUpdateAuditDto;
      }
    });
  }

  ngOnDestroy() {
    if (this.emitterSub) {
      this.emitterSub.unsubscribe();
    }
  }

  protected readonly Object = Object;
}
