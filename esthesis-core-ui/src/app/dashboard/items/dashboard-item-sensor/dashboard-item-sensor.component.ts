import {Component, OnDestroy, OnInit, ViewChild} from "@angular/core";
import {
  DashboardItemGenericComponent
} from "../dashboard-item-generic/dashboard-item-generic.component";
import {ChartConfiguration, ChartData} from "chart.js";
import {BaseChartDirective} from "ng2-charts";
import {Subscription} from "rxjs";
import {
  DashboardItemSensorConfigurationDto
} from "../../dto/configuration/dashboard-item-sensor-configuration-dto";
import {DashboardService} from "../../dashboard.service";
import {UtilityService} from "../../../shared/services/utility.service";
import {DashboardUpdateSensorDto} from "../../dto/updates/DashboardUpdateSensorDto";

@Component({
  selector: "app-dashboard-item-sensor",
  templateUrl: "./dashboard-item-sensor.component.html"
})
export class DashboardItemSensorComponent extends DashboardItemGenericComponent implements OnInit, OnDestroy {
  lastMessage?: DashboardUpdateSensorDto;
  @ViewChild(BaseChartDirective) chart: BaseChartDirective<"line"> | undefined;
  // The subscription to receive dashboard updates.
  emitterSub?: Subscription;
  // The dashboard item configuration.
  config?: DashboardItemSensorConfigurationDto;

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

  constructor(private utilityService: UtilityService,
    private readonly dashboardService: DashboardService) {
    super();
  }

  ngOnInit(): void {
    if (this.item.configuration) {
      this.config = JSON.parse(this.item.configuration) as DashboardItemSensorConfigurationDto;
    }
    this.emitterSub = this.dashboardService.getMessage().subscribe((message) => {
      if (message.id === this.item.id) {
        this.lastMessage = message as DashboardUpdateSensorDto;
        if (this.config?.sparkline) {
          this.sparkLineData.datasets[0].data.push(Number(this.lastMessage.value));
          this.sparkLineData.labels?.push(this.lastMessage.value);

          if (this.sparkLineData.datasets[0].data.length > this.config.sparklinePoints) {
            this.sparkLineData.datasets[0].data.shift();
            this.sparkLineData.labels?.shift();
          }

          this.chart?.update();
        }
      }
    });
  }

  ngOnDestroy() {
    if (this.emitterSub) {
      this.emitterSub.unsubscribe();
    }
  }

  computeBackgroundColor(): string {
    if (this.config?.threshold) {
      if (this.lastMessage) {
        const lastValue = Number(this.lastMessage.value);
        if ( lastValue <= this.config.thresholdLow) {
          return this.config.thresholdLowColor;
        } else if (lastValue <= this.config.thresholdMiddle) {
          return this.config.thresholdMiddleColor;
        } else {
          return this.config.thresholdHighColor;
        }
      } else {
        return "inherit";
      }
    } else {
      return "inherit";
    }
  }
}
