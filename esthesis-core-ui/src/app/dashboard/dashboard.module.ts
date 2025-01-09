import {NgModule, NO_ERRORS_SCHEMA} from "@angular/core";
import {CommonModule} from "@angular/common";

import {DashboardRoutingModule} from "./dashboard-routing.module";
import {DashboardViewComponent} from "./dashboard-view/dashboard-view.component";
import {MAT_DATE_FORMATS} from "@angular/material/core";
import {MatIconModule} from "@angular/material/icon";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
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
} from "./items/dashboard-item-sensor/dashboard-item-sensor.component";
import {BaseChartDirective} from "ng2-charts";
import {
  DashboardItemSecurityStatsComponent
} from "./items/dashboard-item-security-stats/dashboard-item-security-stats.component";
import {
  DashboardItemDevicesStatusComponent
} from "./items/dashboard-item-devices-status/dashboard-item-devices-status.component";
import {
  DashboardItemDevicesLatestComponent
} from "./items/dashboard-item-devices-latest/dashboard-item-devices-latest.component";
import {
  DashboardItemAuditComponent
} from "./items/dashboard-item-audit/dashboard-item-audit.component";
import {
  DashboardItemCampaignsComponent
} from "./items/dashboard-item-campaigns/dashboard-item-campaigns.component";
import {
  DashboardItemAboutComponent
} from "./items/dashboard-item-about/dashboard-item-about.component";
import {
  DashboardItemTitleComponent
} from "./items/dashboard-item-title/dashboard-item-title.component";
import {
  DashboardItemNotesComponent
} from "./items/dashboard-item-notes/dashboard-item-notes.component";
import {
  DashboardItemDevicesLastSeenComponent
} from "./items/dashboard-item-devices-last-seen/dashboard-item-devices-last-seen.component";
import {
  DashboardItemDeviceMapComponent
} from "./items/dashboard-item-device-map/dashboard-item-device-map.component";
import {AboutModule} from "../about/about.module";
import {ComponentsModule} from "../shared/components/components.module";
import {
  DashboardItemSensorIconComponent
} from "./items/dashboard-item-sensor-icon/dashboard-item-sensor-icon.component";
import {MatMenu, MatMenuTrigger} from "@angular/material/menu";
import {DashboardEditComponent} from "./dashboard-edit/dashboard-edit.component";
import {DashboardItemNewComponent} from "./dashboard-item-new/dashboard-item-new.component";
import {CdkDrag, CdkDragHandle, CdkDropList} from "@angular/cdk/drag-drop";
import {
  DashboardItemSensorEditComponent
} from "./items/dashboard-item-sensor-edit/dashboard-item-sensor-edit.component";
import {
  DashboardItemAboutEditComponent
} from "./items/dashboard-item-about-edit/dashboard-item-about-edit.component";
import {
  DashboardItemAuditEditComponent
} from "./items/dashboard-item-audit-edit/dashboard-item-audit-edit.component";
import {
  DashboardItemCampaignsEditComponent
} from "./items/dashboard-item-campaigns-edit/dashboard-item-campaigns-edit.component";
import {
  DashboardItemDeviceMapEditComponent
} from "./items/dashboard-item-device-map-edit/dashboard-item-device-map-edit.component";
import {
  DashboardItemDevicesLatestEditComponent
} from "./items/dashboard-item-devices-latest-edit/dashboard-item-devices-latest-edit.component";
import {
  DashboardItemDevicesStatusEditComponent
} from "./items/dashboard-item-devices-status-edit/dashboard-item-devices-status-edit.component";
import {
  DashboardItemNotesEditComponent
} from "./items/dashboard-item-notes-edit/dashboard-item-notes-edit.component";
import {
  DashboardItemSecurityStatsEditComponent
} from "./items/dashboard-item-security-stats-edit/dashboard-item-security-stats-edit.component";
import {
  DashboardItemSensorIconEditComponent
} from "./items/dashboard-item-sensor-icon-edit/dashboard-item-sensor-icon-edit.component";
import {DashboardItemUrlComponent} from "./items/dashboard-item-url/dashboard-item-url.component";
import {
  DashboardItemImageComponent
} from "./items/dashboard-item-image/dashboard-item-image.component";
import {
  DashboardItemDatetimeComponent
} from "./items/dashboard-item-datetime/dashboard-item-datetime.component";
import {
  DashboardItemDatetimeEditComponent
} from "./items/dashboard-item-datetime-edit/dashboard-item-datetime-edit.component";
import {
  DashboardItemImageEditComponent
} from "./items/dashboard-item-image-edit/dashboard-item-image-edit.component";
import {
  DashboardItemUrlEditComponent
} from "./items/dashboard-item-url-edit/dashboard-item-url-edit.component";
import {
  DashboardItemDevicesLastSeenEditComponent
} from "./items/dashboard-item-devices-last-seen-edit/dashboard-item-devices-last-seen-edit.component";
import {MomentModule} from "ngx-moment";
import {NgxColorsModule} from "ngx-colors";
import {FilterPipeModule} from "ngx-filter-pipe";
import {
  CdkCell,
  CdkCellDef,
  CdkColumnDef,
  CdkHeaderCell,
  CdkHeaderCellDef,
  CdkHeaderRow,
  CdkHeaderRowDef,
  CdkRow,
  CdkRowDef,
  CdkTable
} from "@angular/cdk/table";
import {MatSort, MatSortHeader} from "@angular/material/sort";
import {LeafletModule} from "@bluehalo/ngx-leaflet";

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
    DashboardItemSensorIconComponent,
    DashboardEditComponent,
    DashboardItemNewComponent,
    DashboardItemSensorEditComponent,
    DashboardItemAboutEditComponent,
    DashboardItemAuditEditComponent,
    DashboardItemCampaignsEditComponent,
    DashboardItemDeviceMapEditComponent,
    DashboardItemDevicesLatestEditComponent,
    DashboardItemDevicesStatusEditComponent,
    DashboardItemNotesEditComponent,
    DashboardItemSecurityStatsEditComponent,
    DashboardItemSensorIconEditComponent,
    DashboardItemUrlComponent,
    DashboardItemImageComponent,
    DashboardItemDatetimeComponent,
    DashboardItemDatetimeEditComponent,
    DashboardItemImageEditComponent,
    DashboardItemUrlEditComponent,
    DashboardItemDevicesLastSeenEditComponent,
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
    ComponentsModule,
    MatMenu,
    MatMenuTrigger,
    CdkDropList,
    CdkDragHandle,
    CdkDrag,
    MomentModule,
    NgxColorsModule,
    FilterPipeModule,
    CdkCell,
    CdkCellDef,
    CdkColumnDef,
    CdkHeaderCell,
    CdkHeaderRow,
    CdkHeaderRowDef,
    CdkRow,
    CdkRowDef,
    CdkTable,
    MatSort,
    MatSortHeader,
    CdkHeaderCellDef,
    FormsModule,
    LeafletModule
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
