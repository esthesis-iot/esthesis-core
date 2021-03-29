import {Component, Input, OnInit} from '@angular/core';
import {MatDialog} from "@angular/material/dialog";
import {WidgetSensorValueSetupComponent} from "./widget-sensor-value-setup.component";
import {DashboardService} from "../../dashboard.service";
import {DashboardWidgetDto} from "../../../dto/dashboard-widget-dto";
import {WidgetSensorValueConf} from "./widget-sensor-value-conf";

@Component({
  selector: 'app-widget-sensor-value',
  templateUrl: './widget-sensor-value.component.html',
  styleUrls: ['./widget-sensor-value.component.scss', '../common-widget-styling.scss']
})
export class WidgetSensorValueComponent implements OnInit {
  @Input() id!: number
  @Input() dashboard!: number
  dashboardWidget!: DashboardWidgetDto;
  configuration!: WidgetSensorValueConf;

  constructor(private dialog: MatDialog, private dashboardService: DashboardService) {
  }

  ngOnInit(): void {
    this.dashboardService.getWidget(this.id).subscribe(onNext => {
      this.dashboardWidget = onNext;
      this.configuration = WidgetSensorValueConf.deserialise(onNext.configuration);
    })
  }

  setup() {
    this.dialog.open(WidgetSensorValueSetupComponent, {
      width: '40%',
      data: {
        id: this.id,
        dashboard: this.dashboard
      }
    });
  }
}
