import {Component, OnInit, ViewChild} from "@angular/core";
import {
  DashboardItemGenericComponent
} from "../dashboard-item-generic/dashboard-item-generic.component";
import {BaseChartDirective} from "ng2-charts";
import {UtilityService} from "../../../shared/services/utility.service";
import {ChartConfiguration, ChartData} from "chart.js";

@Component({
  selector: 'app-dashboard-item-devices-status',
  templateUrl: './dashboard-item-devices-status.component.html'
})
export class DashboardItemDevicesStatusComponent extends DashboardItemGenericComponent implements OnInit{
  sensorValue?: string;
  @ViewChild(BaseChartDirective) chart: BaseChartDirective<'pie'> | undefined;

  constructor(private utilityService: UtilityService) {
    super();
  }

  ngOnInit(): void {

  }

  public chartOptions: ChartConfiguration<'pie'>['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    // events: [],
    layout: {
      padding: 0
    },
    plugins: {
      legend: {
        display: false,
      }
    },
  };

  public chartData: ChartData<'pie'> = {
    labels: ['Disabled', 'Preregistered', 'Registered', 'Approval'],
    datasets: [
      {
        data: [65, 59, 80, 81],
        backgroundColor: [
          this.utilityService.getTailwindColor('text-accent'),
          this.utilityService.getTailwindColor('text-secondary'),
          this.utilityService.getTailwindColor('text-primary'),
          this.utilityService.getTailwindColor('text-base-content/20'),
        ],
      }
    ],
  };

}
