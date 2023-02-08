import {NgModule} from "@angular/core";
import {CommonModule} from "@angular/common";

import {SettingsRoutingModule} from "./settings-routing.module";
import {SettingsComponent} from "./settings.component";
import {SettingsDevregComponent} from "./settings-devreg/settings-devreg.component";
import {SettingsSecurityComponent} from "./settings-security/settings-security.component";
import {MatIconModule} from "@angular/material/icon";
import {MatSortModule} from "@angular/material/sort";
import {QFormsModule} from "@qlack/forms";
import {ReactiveFormsModule} from "@angular/forms";
import {FlexModule} from "@angular/flex-layout";
import {
  SettingsProvisioningComponent
} from "./settings-provisioning/settings-provisioning.component";
import {SettingsDevicePageComponent} from "./settings-device-page/settings-device-page.component";
import {SettingsUiComponent} from "./settings-ui/settings-ui.component";
import {SettingsMessagingComponent} from "./settings-messaging/settings-messaging.component";
import {MatMenuModule} from "@angular/material/menu";
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatSelectModule} from "@angular/material/select";
import {MatTableModule} from "@angular/material/table";
import {MatCheckboxModule} from "@angular/material/checkbox";
import {MatPaginatorModule} from "@angular/material/paginator";
import {MatCardModule} from "@angular/material/card";
import {MatInputModule} from "@angular/material/input";
import {MatTabsModule} from "@angular/material/tabs";
import {MatButtonModule} from "@angular/material/button";
import {MatRadioModule} from "@angular/material/radio";
import {MatAutocompleteModule} from "@angular/material/autocomplete";
import {MatTooltipModule} from "@angular/material/tooltip";
import {MatProgressSpinnerModule} from "@angular/material/progress-spinner";

@NgModule({
  declarations: [
    SettingsComponent,
    SettingsProvisioningComponent,
    SettingsDevicePageComponent,
    SettingsDevregComponent,
    SettingsSecurityComponent,
    SettingsUiComponent,
    SettingsMessagingComponent,
  ],
  imports: [
    CommonModule,
    SettingsRoutingModule,
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
    MatRadioModule,
    FlexModule,
    MatCheckboxModule,
    MatTooltipModule,
    MatProgressSpinnerModule,
    MatAutocompleteModule
  ]
})
export class SettingsModule {
}
