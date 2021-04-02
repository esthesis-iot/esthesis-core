import {Component, OnInit} from '@angular/core';
import {MatDialogRef} from "@angular/material/dialog";
import {BaseComponent} from "../shared/component/base-component";
import {AppConstants} from "../app.constants";

@Component({
  selector: 'app-dashboard-add-widget',
  templateUrl: './dashboard-add-widget.component.html',
  styleUrls: ['./dashboard-add-widget.component.scss']
})
export class DashboardAddWidgetComponent extends BaseComponent implements OnInit {
  // Expose application constants.
  constants = AppConstants;

  constructor(public selfDialogRef: MatDialogRef<DashboardAddWidgetComponent>) {
    super();
  }

  ngOnInit(): void {
  }

  selectWidget(widgetType: string) {
    this.selfDialogRef.close(widgetType);
  }
}
