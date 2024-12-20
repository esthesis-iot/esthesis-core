import {Component, OnDestroy, OnInit, ViewChild} from "@angular/core";
import {ChartConfiguration, ChartData} from "chart.js";
import {BaseChartDirective} from "ng2-charts";
import {
  DashboardItemSensorConfigurationDto
} from "../../dto/configuration/dashboard-item-sensor-configuration-dto";
import {DashboardUpdateSensorDto} from "../../dto/updates/DashboardUpdateSensorDto";
import {DashboardItemComponent} from "../dashboard-item.component";
import {Subscription} from "rxjs";

@Component({
  selector: "app-dashboard-item-sensor",
  templateUrl: "./dashboard-item-sensor.component.html"
})
export class DashboardItemSensorComponent
  extends DashboardItemComponent<DashboardUpdateSensorDto, DashboardItemSensorConfigurationDto>
  implements OnInit, OnDestroy {
  @ViewChild(BaseChartDirective) chart: BaseChartDirective<"line"> | undefined;
  // A subscription to receive notification from the superclass when lastMessage is updated.
  lastMessageSubscription?: Subscription;
  public sparkLineData: ChartData<"line"> = {
    labels: [],
    datasets: [
      {
        data: [],
        borderColor: [this.utilityService.getTailwindColor("text-primary")],
        backgroundColor: [this.utilityService.getTailwindColor("text-base-content/50")],
      }
    ],
  };
  public sparkLineOptions: ChartConfiguration<"line">["options"] = {
    responsive: true,
    maintainAspectRatio: false,
    events: [],
    layout: {
      padding: 0
    },
    scales: {
      x: {
        display: false,
        grid: {
          display: false
        }
      },
      y: {
        display: false,
        grid: {
          display: false
        }
      },
    },
    plugins: {
      legend: {
        display: false,
      },
    },
  };

  override ngOnInit(): void {
    super.ngOnInit();
    this.lastMessageSubscription = this.lastMessageEmitter.subscribe(lastMessage => {
      if (this.config?.sparkline) {
        this.sparkLineData.datasets[0].data.push(Number(lastMessage.value));
        this.sparkLineData.labels?.push(lastMessage.value);
        if (this.sparkLineData.datasets[0].data.length > this.config.sparklinePoints) {
          this.sparkLineData.datasets[0].data.shift();
          this.sparkLineData.labels?.shift();
        }
        this.chart?.update();
      }
    });
  }

  override ngOnDestroy() {
    super.ngOnDestroy();
    if (this.lastMessageSubscription) {
      this.lastMessageSubscription.unsubscribe();
    }
  }

  computeBackgroundColor(): string {
    if (this.config?.threshold) {
      if (this.lastMessage) {
        const lastValue = Number(this.lastMessage.value);
        if (lastValue <= this.config.thresholdLow) {
          return this.config.thresholdLowColor;
        } else if (lastValue <= this.config.thresholdMiddle) {
          return this.config.thresholdMiddleColor;
        } else {
          return this.config.thresholdHighColor;
        }
      } else {
        return "bg-base-100";
      }
    } else {
      return "bg-base-100";
    }
  }
}
