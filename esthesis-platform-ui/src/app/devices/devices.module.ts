import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DevicesRoutingModule } from './devices-routing.module';
import {FlexLayoutModule} from '@angular/flex-layout';
import {
  DateAdapter, MAT_DATE_FORMATS, MAT_DATE_LOCALE,
  MatAutocompleteModule,
  MatButtonModule,
  MatCardModule, MatChipsModule,
  MatDatepickerModule, MatDialogModule,
  MatFormFieldModule, MatIconModule,
  MatInputModule, MatMenuModule, MatPaginatorModule, MatSelectModule, MatSortModule, MatTableModule, MatTabsModule
} from '@angular/material';
import {QFormsModule} from '@eurodyn/forms';
import {ReactiveFormsModule} from '@angular/forms';
import {DeviceComponent} from './device/device.component';
import {DevicesComponent} from './devices.component';
import {DeviceEventsComponent} from './device/device-events/device-events.component';
import {DeviceLogsComponent} from './device/device-logs/device-logs.component';
import {DeviceRemotingComponent} from './device/device-remoting/device-remoting.component';
import {DeviceCommandsComponent} from './device/device-commands/device-commands.component';
import {DeviceSettingsComponent} from './device/device-settings/device-settings.component';
import {DeviceMeasurementsComponent} from './device/device-measurements/device-measurements.component';
import {DeviceSensorsComponent} from './device/device-sensors/device-sensors.component';
import {DevicePreregisterComponent} from './device-preregister.component';
import {LeafletModule} from '@asymmetrik/ngx-leaflet';
import {MomentDateAdapter} from '@angular/material-moment-adapter';
import {MomentModule} from 'ngx-moment';
import {ZXingScannerModule} from '@zxing/ngx-scanner';

@NgModule({
  declarations: [
    DeviceComponent,
    DevicesComponent,
    DeviceEventsComponent,
    DeviceLogsComponent,
    DeviceRemotingComponent,
    DeviceCommandsComponent,
    DeviceSettingsComponent,
    DeviceMeasurementsComponent,
    DeviceSensorsComponent,
    DevicePreregisterComponent,
  ],
  imports: [
    CommonModule,
    DevicesRoutingModule,
    FlexLayoutModule,
    MatButtonModule,
    MatCardModule,
    MatDatepickerModule,
    MatFormFieldModule,
    MatInputModule,
    MatMenuModule,
    MatPaginatorModule,
    MatSelectModule,
    MatTableModule,
    QFormsModule,
    ReactiveFormsModule,
    MatSortModule,
    MatIconModule,
    MatTabsModule,
    MatChipsModule,
    LeafletModule.forRoot(),
    MatAutocompleteModule,
    MomentModule,
    ZXingScannerModule,
    MatDialogModule
  ],
  providers: [
    {
      provide: DateAdapter,
      useClass: MomentDateAdapter,
      deps: [MAT_DATE_LOCALE]
    },
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
export class DevicesModule { }
