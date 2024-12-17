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

@Component({
  selector: "app-dashboard-item-sensor",
  templateUrl: "./dashboard-item-sensor.component.html"
})
export class DashboardItemSensorComponent extends DashboardItemGenericComponent implements OnInit, OnDestroy {
  sensorValueHistory: string[] = [];
  sensorValue?: string;
  @ViewChild(BaseChartDirective) chart: BaseChartDirective<"bar"> | undefined;
  // The subscription to receive dashboard updates.
  emitterSub?: Subscription;
  config?: DashboardItemSensorConfigurationDto;

  constructor(private utilityService: UtilityService,
    private readonly dashboardService: DashboardService) {
    super();
  }

  ngOnInit(): void {
    if (this.item.configuration) {
      this.config = JSON.parse(this.item.configuration) as DashboardItemSensorConfigurationDto;
    }
    this.emitterSub = this.dashboardService.getMessage().subscribe((message) => {
      // if (message.sensor) {
      //   console.log(">>>>>>>>>> FROM SENSOR: ", message.sensor);
      // }
    });
  }

  ngOnDestroy() {
    if (this.emitterSub) {
      this.emitterSub.unsubscribe();
    }
  }

  public barChartOptions: ChartConfiguration<"line">["options"] = {
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

  public barChartData: ChartData<"line"> = {
    labels: ["2006", "2007", "2008", "2009", "2010", "2011", "2012"],
    datasets: [
      {
        data: [65, 59, 80, 81, 56, 55, 40],
        label: "Series A",
        borderColor: [this.utilityService.getTailwindColor("text-primary")],
        backgroundColor: [this.utilityService.getTailwindColor("text-base-content/50")],
      }
    ],
  };

}
