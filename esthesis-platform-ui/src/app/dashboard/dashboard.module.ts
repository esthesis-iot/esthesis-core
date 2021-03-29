import {NgModule, NO_ERRORS_SCHEMA} from '@angular/core';
import {CommonModule} from '@angular/common';

import {DashboardRoutingModule} from './dashboard-routing.module';
import {DashboardComponent} from './dashboard.component';
import {DashboardMapComponent} from './dashboard-map/dashboard-map.component';
import {DashboardChartsComponent} from './dashboard-charts/dashboard-charts.component';
import {LeafletModule} from '@asymmetrik/ngx-leaflet';
import {FlexLayoutModule} from '@angular/flex-layout';
import {MatCardModule} from '@angular/material/card';
import {MAT_DATE_FORMATS} from '@angular/material/core';
import {MatIconModule} from '@angular/material/icon';
import {MatTableModule} from '@angular/material/table';
import {NgxChartsModule} from '@swimlane/ngx-charts';
import {GridsterModule} from "angular-gridster2";
import {MatButtonModule} from "@angular/material/button";
import {DashboardAddWidgetComponent} from './dashboard-add-widget.component';
import {WidgetSensorValueComponent} from './dashboard-widgets/widget-sensor-value/widget-sensor-value.component';
import {WidgetSensorValueSetupComponent} from './dashboard-widgets/widget-sensor-value/widget-sensor-value-setup.component';
import {ReactiveFormsModule} from "@angular/forms";
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatSelectModule} from "@angular/material/select";
import {MatInputModule} from "@angular/material/input";
import {MatAutocompleteModule} from "@angular/material/autocomplete";
import {MAT_COLOR_FORMATS, NgxMatColorPickerModule, NGX_MAT_COLOR_FORMATS} from "@angular-material-components/color-picker";

// @ts-ignore
@NgModule({
  declarations: [
    DashboardComponent,
    DashboardMapComponent,
    DashboardChartsComponent,
    DashboardAddWidgetComponent,
    WidgetSensorValueComponent,
    WidgetSensorValueSetupComponent,
  ],
  imports: [
    CommonModule,
    DashboardRoutingModule,
    FlexLayoutModule,
    LeafletModule,
    MatCardModule,
    MatTableModule,
    MatIconModule,
    NgxChartsModule,
    GridsterModule,
    MatButtonModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatSelectModule,
    MatInputModule,
    MatAutocompleteModule,
    NgxMatColorPickerModule
  ],
  schemas: [NO_ERRORS_SCHEMA],
  providers: [
    { provide: MAT_COLOR_FORMATS, useValue: NGX_MAT_COLOR_FORMATS },
    {
      provide: MAT_DATE_FORMATS,
      useValue: {
        parse: {
          dateInput: 'LL',
        },
        display: {
          dateInput: 'YYYY-MM-DD',
          monthYearLabel: 'MMM YYYY',
          dateA11yLabel: 'LL',
          monthYearA11yLabel: 'MMMM YYYY',
        },
      }
    }
  ]
})
export class DashboardModule {
}
