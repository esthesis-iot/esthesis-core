import {NgModule} from "@angular/core";
import {CommonModule} from "@angular/common";
import {DevicesRoutingModule} from "./devices-routing.module";
import {MatIconModule} from "@angular/material/icon";
import {MatSortModule} from "@angular/material/sort";
import {QFormsModule} from "@qlack/forms";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {DeviceComponent} from "./device/device.component";
import {DevicesComponent} from "./devices-list/devices.component";
import {DevicePreregisterComponent} from "./device-preregister/device-preregister.component";
import {MomentModule} from "ngx-moment";
import {ZXingScannerModule} from "@zxing/ngx-scanner";
import {DevicePreregisterCamComponent} from "./device-preregister/device-preregister-cam.component";
import {DeviceTerminalComponent} from "./device/device-terminal/device-terminal.component";
import {NgTerminalModule} from "ng-terminal";
import {A11yModule} from "@angular/cdk/a11y";
import {DeviceDataComponent} from "./device/device-data/device-data.component";
import {DeviceProfileComponent} from "./device/device-profile/device-profile.component";
import {FormlyModule} from "@ngx-formly/core";
import {FormlyMaterialModule} from "@ngx-formly/material";
import {ClipboardModule} from "@angular/cdk/clipboard";
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatSelectModule} from "@angular/material/select";
import {MatSlideToggleModule} from "@angular/material/slide-toggle";
import {MatPaginatorModule} from "@angular/material/paginator";
import {MatListModule} from "@angular/material/list";
import {MatInputModule} from "@angular/material/input";
import {MatProgressSpinnerModule} from "@angular/material/progress-spinner";
import {MatChipsModule} from "@angular/material/chips";
import {MatMenuModule} from "@angular/material/menu";
import {MatTableModule} from "@angular/material/table";
import {MatAutocompleteModule} from "@angular/material/autocomplete";
import {MatDialogModule} from "@angular/material/dialog";
import {MatCardModule} from "@angular/material/card";
import {MatTabsModule} from "@angular/material/tabs";
import {MatTooltipModule} from "@angular/material/tooltip";
import {MatButtonModule} from "@angular/material/button";
import {FontAwesomeModule} from "@fortawesome/angular-fontawesome";
import {CdkTableModule} from "@angular/cdk/table";
import {CountdownComponent} from "ngx-countdown";
import {ComponentsModule} from "../shared/components/components.module";
import {LeafletModule} from "@bluehalo/ngx-leaflet";

@NgModule({
  declarations: [
    DeviceComponent,
    DevicesComponent,
    DevicePreregisterComponent,
    DevicePreregisterCamComponent,
    DeviceTerminalComponent,
    DeviceDataComponent,
    DeviceProfileComponent,
  ],
  imports: [
    CommonModule,
    DevicesRoutingModule,
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
    MatAutocompleteModule,
    MomentModule,
    ZXingScannerModule,
    MatDialogModule,
    MatSlideToggleModule,
    MatListModule,
    MatProgressSpinnerModule,
    MatTooltipModule,
    NgTerminalModule,
    A11yModule,
    FormlyModule.forChild(),
    FormlyMaterialModule,
    ClipboardModule,
    ComponentsModule,
    FontAwesomeModule,
    CdkTableModule,
    CountdownComponent,
    FormsModule,
    LeafletModule
  ]
})
export class DevicesModule {
}
