import {Component, OnDestroy, OnInit} from "@angular/core";
import {
  DashboardItemGenericComponent
} from "../dashboard-item-generic/dashboard-item-generic.component";
import {DashboardUpdateAboutDto} from "../../dto/updates/DashboardUpdateAboutDto";
import {Subscription} from "rxjs";
import {DashboardService} from "../../dashboard.service";

@Component({
  selector: 'app-dashboard-item-about',
  templateUrl: './dashboard-item-about.component.html'
})
export class DashboardItemAboutComponent extends DashboardItemGenericComponent implements OnInit, OnDestroy {
  // The last message this component received.
  lastMessage?: DashboardUpdateAboutDto;
  // The subscription to receive dashboard updates.
  emitterSub?: Subscription;

  constructor(private readonly dashboardService: DashboardService) {
    super();
  }

  ngOnInit(): void {
    this.emitterSub = this.dashboardService.getMessage().subscribe((message) => {
      if (message.id === this.item.id) {
        this.lastMessage = message as DashboardUpdateAboutDto;
      }
    });
  }

  ngOnDestroy() {
    if (this.emitterSub) {
      this.emitterSub.unsubscribe();
    }
  }
}
