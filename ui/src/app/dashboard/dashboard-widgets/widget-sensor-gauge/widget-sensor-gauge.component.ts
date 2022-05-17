import {Component, Input, OnDestroy, OnInit} from '@angular/core';
import {DashboardWidgetDto} from "../../../dto/dashboard-widget-dto";
import {Color} from "@angular-material-components/color-picker";
import {MatDialog} from "@angular/material/dialog";
import {DashboardService} from "../../dashboard.service";
import {FormatterService} from "../../../shared/service/formatter.service";
import {WidgetSensorGaugeConf} from "./widget-sensor-gauge-conf";
import {WidgetSensorGaugeSetupComponent} from "./widget-sensor-gauge-setup.component";

@Component({
  selector: 'app-widget-sensor-gauge',
  templateUrl: './widget-sensor-gauge.component.html',
  styleUrls: ['./widget-sensor-gauge.component.scss', '../common-widget-styling.scss']
})
export class WidgetSensorGaugeComponent implements OnInit, OnDestroy {
  // the Id of the widget.
  @Input() id!: number
  // The Id of the dashboard this widget belongs to.
  @Input() dashboard!: number
  // The details of the widget.
  dashboardWidget!: DashboardWidgetDto;
  // A snapshot of widget's configuration. The snapshot is updated once when the widget is loaded.
  configuration!: WidgetSensorGaugeConf;
  // Default colors.
  bgColor = new Color(64, 199, 247, 1);
  fgColor = new Color(255, 255, 255, 1);
  // The current/latest value for this widget.
  lastValue?: any;
  // Value updates subscription.
  lastValueSubscription?: any;

  constructor(private dialog: MatDialog, private dashboardService: DashboardService,
              private formatterService: FormatterService) {
  }

  getValue() {
    // Get the current/latest value for this widget.
    this.dashboardService.getWidgetValue(this.id).subscribe(
      onNext => {
        this.lastValue = [{
          "name": this.configuration.title,
          "value": Number(onNext)
        }];
      }
    );
  }

  ngOnInit(): void {
    // Get the widget to display.
    this.dashboardService.getWidget(this.id).subscribe(onNext => {
      this.dashboardWidget = onNext;
      this.configuration = WidgetSensorGaugeConf.deserialise(onNext.configuration);
      this.bgColor = this.formatterService.rgbaStringToColor(this.configuration.bgColor);
      this.fgColor = this.formatterService.rgbaStringToColor(this.configuration.fgColor);

      // Get current value.
      this.getValue();

      // Setup updates for widget's value.
      this.lastValueSubscription = setInterval(() => {
        this.getValue();
      }, this.dashboardWidget.updateEvery * 1000);
    });
  }

  ngOnDestroy() {
    clearInterval(this.lastValueSubscription);
  }

  setup() {
    this.dialog.open(WidgetSensorGaugeSetupComponent, {
      width: '35%',
      data: {
        id: this.id,
        dashboard: this.dashboard
      }
    });
  }
}
