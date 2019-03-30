import { NgModule, NO_ERRORS_SCHEMA } from '@angular/core';
import { CommonModule } from '@angular/common';

import { DashboardRoutingModule } from './dashboard-routing.module';
import {DashboardComponent} from './dashboard.component';
import {DashboardInfoBoxesComponent} from './dashboard-info-boxes/dashboard-info-boxes.component';
import {DashboardMapComponent} from './dashboard-map/dashboard-map.component';
import {DashboardChartsComponent} from './dashboard-charts/dashboard-charts.component';
import {DashboardInactiveDevicesComponent} from './dashboard-inactive-devices/dashboard-inactive-devices.component';
import {DashboardErrorLogComponent} from './dashboard-error-log/dashboard-error-log.component';
import {DashboardNewRegistrationsComponent} from './dashboard-new-registrations/dashboard-new-registrations.component';
import {LeafletModule} from '@asymmetrik/ngx-leaflet';
import {FlexLayoutModule} from '@angular/flex-layout';
import {MatCardModule, MatIconModule, MatTableModule} from '@angular/material';
import {InfoBoxIconValueLegendComponent} from './dashboard-widgets/info-box-icon-value-legend/info-box-icon-value-legend.component';
import {NgxChartsModule} from '@swimlane/ngx-charts';

@NgModule({
  declarations: [
    DashboardComponent,
    DashboardInfoBoxesComponent,
    DashboardMapComponent,
    DashboardChartsComponent,
    DashboardInactiveDevicesComponent,
    DashboardErrorLogComponent,
    DashboardNewRegistrationsComponent,
    InfoBoxIconValueLegendComponent
  ],
  imports: [
    CommonModule,
    DashboardRoutingModule,
    FlexLayoutModule,
    LeafletModule,
    MatCardModule,
    MatTableModule,
    MatIconModule,
    NgxChartsModule
  ],
  schemas: [NO_ERRORS_SCHEMA],
})
export class DashboardModule { }
