import {Component, OnInit, ViewChild} from "@angular/core";
import {
  DashboardItemGenericComponent
} from "../dashboard-item-generic/dashboard-item-generic.component";
import {ChartConfiguration, ChartData} from "chart.js";
import {BaseChartDirective} from "ng2-charts";
import {UtilityService} from "../../../shared/services/utility.service";

@Component({
  selector: "app-dashboard-item-sensor",
  templateUrl: "./dashboard-item-sensor.component.html"
})
export class DashboardItemSensorComponent extends DashboardItemGenericComponent implements OnInit{
  sensorValueHistory: string[] = [];
  sensorValue?: string;
  @ViewChild(BaseChartDirective) chart: BaseChartDirective<'bar'> | undefined;

  constructor(private utilityService: UtilityService) {
    super();
  }

  ngOnInit(): void {
    let rndVal = (Math.random() * 100) + "";
    // if (!isNaN(Number(rndVal))) {
    //   this.sensorValue = Number(rndVal).toFixed(this.item.precision);
    // } else {
    //   this.sensorValue = rndVal.substring(0, this.item.precision);
    // }
  }

  public barChartOptions: ChartConfiguration<'line'>['options'] = {
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

  public barChartData: ChartData<'line'> = {
    labels: ['2006', '2007', '2008', '2009', '2010', '2011', '2012'],
    datasets: [
      {
        data: [65, 59, 80, 81, 56, 55, 40],
        label: 'Series A',
        borderColor: [this.utilityService.getTailwindColor('text-primary')],
        backgroundColor: [this.utilityService.getTailwindColor('text-base-content/50')],
      }
    ],
  };

}
