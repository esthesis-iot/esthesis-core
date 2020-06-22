import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {DevicesRoutingModule} from './devices-routing.module';
import {FlexLayoutModule} from '@angular/flex-layout';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { DateAdapter, MAT_DATE_FORMATS, MAT_DATE_LOCALE } from '@angular/material/core';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatMenuModule } from '@angular/material/menu';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatSelectModule } from '@angular/material/select';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatSortModule } from '@angular/material/sort';
import { MatTableModule } from '@angular/material/table';
import { MatTabsModule } from '@angular/material/tabs';
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
import {DevicePreregisterCamComponent} from './device-preregister-cam.component';
import {MatListModule} from '@angular/material/list';

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
    DevicePreregisterCamComponent,
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
    LeafletModule,
    MatAutocompleteModule,
    MomentModule,
    ZXingScannerModule,
    MatDialogModule,
    MatSlideToggleModule,
    MatListModule
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
  ],
  entryComponents: [
    DevicePreregisterCamComponent
  ]
})
export class DevicesModule {
}
