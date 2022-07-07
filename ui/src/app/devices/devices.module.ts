import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {DevicesRoutingModule} from './devices-routing.module';
import {FlexLayoutModule} from '@angular/flex-layout';
import {MatAutocompleteModule} from '@angular/material/autocomplete';
import {MatButtonModule} from '@angular/material/button';
import {MatCardModule} from '@angular/material/card';
import {MatChipsModule} from '@angular/material/chips';
import {MatDialogModule} from '@angular/material/dialog';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatIconModule} from '@angular/material/icon';
import {MatInputModule} from '@angular/material/input';
import {MatMenuModule} from '@angular/material/menu';
import {MatPaginatorModule} from '@angular/material/paginator';
import {MatSelectModule} from '@angular/material/select';
import {MatSlideToggleModule} from '@angular/material/slide-toggle';
import {MatSortModule} from '@angular/material/sort';
import {MatTableModule} from '@angular/material/table';
import {MatTabsModule} from '@angular/material/tabs';
import {QFormsModule} from '@qlack/forms';
import {ReactiveFormsModule} from '@angular/forms';
import {DeviceComponent} from './device/device.component';
import {DevicesComponent} from './devices.component';
import {DevicePreregisterComponent} from './device-preregister.component';
import {LeafletModule} from '@asymmetrik/ngx-leaflet';
import {MomentModule} from 'ngx-moment';
import {ZXingScannerModule} from '@zxing/ngx-scanner';
import {DevicePreregisterCamComponent} from './device-preregister-cam.component';
import {MatListModule} from '@angular/material/list';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {MatTooltipModule} from '@angular/material/tooltip';
import {DeviceProfileComponent} from './device/device-profile/device-profile.component';
import {DeviceTerminalComponent} from './device/device-terminal/device-terminal.component';
import {NgTerminalModule} from 'ng-terminal';
import {A11yModule} from "@angular/cdk/a11y";

@NgModule({
  declarations: [
    DeviceComponent,
    DevicesComponent,
    DevicePreregisterComponent,
    DevicePreregisterCamComponent,
    DeviceProfileComponent,
    DeviceTerminalComponent,
  ],
  imports: [
    CommonModule,
    DevicesRoutingModule,
    FlexLayoutModule,
    MatButtonModule,
    MatCardModule,
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
    MatListModule,
    MatProgressSpinnerModule,
    MatTooltipModule,
    NgTerminalModule,
    A11yModule
  ]
})
export class DevicesModule {
}
