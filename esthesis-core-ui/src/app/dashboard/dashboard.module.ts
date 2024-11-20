import {NgModule, NO_ERRORS_SCHEMA} from "@angular/core";
import {CommonModule} from "@angular/common";

import {DashboardRoutingModule} from "./dashboard-routing.module";
import {DashboardViewComponent} from "./dashboard-view/dashboard-view.component";
import {MAT_DATE_FORMATS} from "@angular/material/core";
import {MatIconModule} from "@angular/material/icon";
import {ReactiveFormsModule} from "@angular/forms";
import {MatCardModule} from "@angular/material/card";
import {MatTableModule} from "@angular/material/table";
import {MatButtonModule} from "@angular/material/button";
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatSelectModule} from "@angular/material/select";
import {MatAutocompleteModule} from "@angular/material/autocomplete";
import {MatInputModule} from "@angular/material/input";
import {MatCheckboxModule} from "@angular/material/checkbox";
import {MatSliderModule} from "@angular/material/slider";
import {FontAwesomeModule} from "@fortawesome/angular-fontawesome";
import {NgxMasonryModule} from "ngx-masonry";
import {MatTooltip} from "@angular/material/tooltip";
import {
  DashboardItemSensorComponent
} from "./dashboard-item-sensor/dashboard-item-sensor.component";
import {
  DashboardItemGenericComponent
} from "./dashboard-item-generic/dashboard-item-generic.component";
import {BaseChartDirective} from "ng2-charts";
import { DashboardItemSecurityStatsComponent } from './dashboard-item-security-stats/dashboard-item-security-stats.component';
import { DashboardItemDevicesStatusComponent } from './dashboard-item-devices-status/dashboard-item-devices-status.component';
import { DashboardItemDevicesLatestComponent } from './dashboard-item-devices-latest/dashboard-item-devices-latest.component';
import { DashboardItemAuditComponent } from './dashboard-item-audit/dashboard-item-audit.component';
import { DashboardItemCampaignsComponent } from './dashboard-item-campaigns/dashboard-item-campaigns.component';
import { DashboardItemAboutComponent } from './dashboard-item-about/dashboard-item-about.component';
import { DashboardItemTitleComponent } from './dashboard-item-title/dashboard-item-title.component';
import { DashboardItemNotesComponent } from './dashboard-item-notes/dashboard-item-notes.component';
import { DashboardItemDevicesLastSeenComponent } from './dashboard-item-devices-last-seen/dashboard-item-devices-last-seen.component';
import { DashboardItemDeviceMapComponent } from './dashboard-item-device-map/dashboard-item-device-map.component';
import {AboutModule} from "../about/about.module";
import {ComponentsModule} from "../shared/components/components.module";
import { DashboardItemSensorIconComponent } from './dashboard-item-sensor-icon/dashboard-item-sensor-icon.component';

// @ts-ignore
@NgModule({
  declarations: [
    DashboardViewComponent,
    DashboardItemSensorComponent,
    DashboardItemSecurityStatsComponent,
    DashboardItemDevicesStatusComponent,
    DashboardItemDevicesLatestComponent,
    DashboardItemAuditComponent,
    DashboardItemCampaignsComponent,
    DashboardItemAboutComponent,
    DashboardItemTitleComponent,
    DashboardItemNotesComponent,
    DashboardItemDevicesLastSeenComponent,
    DashboardItemDeviceMapComponent,
    DashboardItemSensorIconComponent
  ],
  imports: [
    CommonModule,
    DashboardRoutingModule,
    MatCardModule,
    MatTableModule,
    MatIconModule,
    MatButtonModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatSelectModule,
    MatInputModule,
    MatAutocompleteModule,
    MatSliderModule,
    MatCheckboxModule,
    FontAwesomeModule,
    NgxMasonryModule,
    MatTooltip,
    BaseChartDirective,
    AboutModule,
    ComponentsModule
  ],
  schemas: [NO_ERRORS_SCHEMA],
  providers: [
    {
      provide: MAT_DATE_FORMATS,
      useValue: {
        parse: {
          dateInput: "LL",
        },
        display: {
          dateInput: "YYYY-MM-DD",
          monthYearLabel: "MMM YYYY",
          dateA11yLabel: "LL",
          monthYearA11yLabel: "MMMM YYYY",
        },
      }
    }
  ]
})
export class DashboardModule {
}
