import {Component, OnDestroy, OnInit, ViewChild} from "@angular/core";
import {DashboardItemComponent} from "../dashboard-item.component";
import {Subscription} from "rxjs";
import {DashboardUpdateChartDto} from "../../dto/updates/DashboardUpdateChartDto";
import {
  DashboardItemChartConfigurationDto
} from "../../dto/configuration/dashboard-item-chart-configuration-dto";
import {BaseChartDirective} from "ng2-charts";
import {ChartConfiguration, ChartData} from "chart.js";

@Component({
  selector: "app-dashboard-item-chart",
  templateUrl: "./dashboard-item-chart.component.html"
})
export class DashboardItemChartComponent
  extends DashboardItemComponent<DashboardUpdateChartDto, DashboardItemChartConfigurationDto>
  implements OnInit, OnDestroy {
  @ViewChild(BaseChartDirective) chart: BaseChartDirective<"line"> | undefined;

  // A subscription to receive notification from the superclass when lastMessage is updated.
  lastMessageSubscription?: Subscription;

  public chartData: ChartData<"line"> = {
    labels: [],
    datasets: []
  };

  public chartOptions: ChartConfiguration['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    scales: {
      x: {
        display: false,
        grid: {
          display: false
        },
        ticks: {
          display: false
        }
      }
    },
    elements: {
      line: {
        tension: 0
      },
    },
    plugins: {
      legend: { display: false },
    },
  };

  override ngOnInit() {
    super.ngOnInit();
    this.chartOptions!.elements!.line!.tension = this.config?.lineTension;
    this.lastMessageSubscription = this.lastMessageEmitter.subscribe(lastMessage => {
      let i = 0;
      lastMessage.data.forEach((data) => {
        const hardwareId = data.left;
        const measurement = data.middle;
        const value = data.right;

        if (!this.chartData?.datasets[i]) {
          this.chartData.datasets[i] = {
            data: [],
            label: hardwareId + "," + measurement,
            yAxisID: measurement,
            fill: 'origin',
          }
        }
        this.chartData.datasets[i].data.push(Number(value));
        // Slice data, if necessary.
        if (this.chartData.datasets[i].data.length > this.config!.totalPoints) {
          this.chartData.datasets[i].data.shift();
        }
        i++;
      });
      this.chartData.labels!.push(new Date());

      // Slice data, if necessary.
      if (this.chartData.labels!.length > this.config!.totalPoints) {
        this.chartData.labels!.shift();
      }

      this.chart?.update("resize");
    })
  }

  override ngOnDestroy() {
    super.ngOnDestroy();
    if (this.lastMessageSubscription) {
      this.lastMessageSubscription.unsubscribe();
    }
  }

}
