import {Component, Input, OnInit} from '@angular/core';
import {MatDialog} from "@angular/material/dialog";
import {WidgetSensorValueSetupComponent} from "./widget-sensor-value-setup.component";
import {AppConstants} from "../../../app.constants";

@Component({
  selector: 'app-widget-sensor-value',
  templateUrl: './widget-sensor-value.component.html',
  styleUrls: ['./widget-sensor-value.component.scss']
})
export class WidgetSensorValueComponent implements OnInit {
  @Input() id!: number
  @Input() dashboard!: number

  constructor(private dialog: MatDialog) {
  }

  ngOnInit(): void {
  }

  setup() {
    this.dialog.open(WidgetSensorValueSetupComponent, {
      width: '40%',
      data: {
        id: this.id,
        dashboard: this.dashboard
      }
    }).afterClosed().subscribe(result => {
      if (result == AppConstants.DIALOG_RESULT.DELETE) {

      }
    });
  }
}
