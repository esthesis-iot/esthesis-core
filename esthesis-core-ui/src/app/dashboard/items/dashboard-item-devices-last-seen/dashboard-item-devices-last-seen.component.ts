import {Component, OnDestroy, OnInit, ViewChild} from "@angular/core";
import {
  DashboardUpdateDevicesLastSeenDto
} from "../../dto/updates/DashboardUpdateDevicesLastSeenDto";
import {DashboardItemComponent} from "../dashboard-item.component";
import {BaseChartDirective} from "ng2-charts";
import {Subscription} from "rxjs";
import {ChartConfiguration, ChartData} from "chart.js";

@Component({
  selector: "app-dashboard-item-devices-last-seen",
  templateUrl: "./dashboard-item-devices-last-seen.component.html"
})
export class DashboardItemDevicesLastSeenComponent
  extends DashboardItemComponent<DashboardUpdateDevicesLastSeenDto>
  implements OnInit, OnDestroy {
  @ViewChild(BaseChartDirective) chart: BaseChartDirective<"pie"> | undefined;
  // A subscription to receive notification from the superclass when lastMessage is updated.
  lastMessageSubscription?: Subscription;
  public pieChartOptions: ChartConfiguration<"pie">["options"] = {
    maintainAspectRatio: false,
    responsive: true,
    layout: {
      autoPadding: false,
      padding: 0
    },
    plugins: {
      legend: {
        display: true,
        position: "right",
        labels: {
          color: this.utilityService.getTailwindColor("text-neutral-content"),
        }
      }
    }
  };
  public pieChartData: ChartData<"pie", number[], string | string[]> = {
    datasets: [
      {
        data: [0,0,0,0,0],
        backgroundColor: [
          this.utilityService.getTailwindColor("text-primary"),
          this.utilityService.getTailwindColor("text-secondary"),
          this.utilityService.getTailwindColor("text-accent"),
          this.utilityService.getTailwindColor("text-warning"),
          this.utilityService.getTailwindColor("text-info")
        ]
      },
    ],
  };

  override ngOnInit(): void {
    super.ngOnInit();
    this.lastMessageSubscription = this.lastMessageEmitter.subscribe(lastMessage => {
      this.pieChartData.datasets[0].data = [
        Number(lastMessage.lastMonth),
        Number(lastMessage.lastWeek),
        Number(lastMessage.lastDay),
        Number(lastMessage.lastHour),
        Number(lastMessage.lastMinute)
      ];
      this.pieChartData.labels = [" Last month", " Last week", " Last day", " Last hour", " Last minute"];
      this.chart?.update();
    });
  }

  override ngOnDestroy() {
    super.ngOnDestroy();
    if (this.lastMessageSubscription) {
      this.lastMessageSubscription.unsubscribe();
    }
  }

}
