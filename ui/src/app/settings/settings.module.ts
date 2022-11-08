import {NgModule} from "@angular/core";
import {CommonModule} from "@angular/common";

import {SettingsRoutingModule} from "./settings-routing.module";
import {SettingsComponent} from "./settings.component";
import {SettingsDevregComponent} from "./settings-devreg/settings-devreg.component";
import {SettingsSecurityComponent} from "./settings-security/settings-security.component";
import {MatButtonModule} from "@angular/material/button";
import {MatCardModule} from "@angular/material/card";
import {MatCheckboxModule} from "@angular/material/checkbox";
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatIconModule} from "@angular/material/icon";
import {MatInputModule} from "@angular/material/input";
import {MatMenuModule} from "@angular/material/menu";
import {MatPaginatorModule} from "@angular/material/paginator";
import {MatRadioModule} from "@angular/material/radio";
import {MatSelectModule} from "@angular/material/select";
import {MatSortModule} from "@angular/material/sort";
import {MatTableModule} from "@angular/material/table";
import {MatTabsModule} from "@angular/material/tabs";
import {QFormsModule} from "@qlack/forms";
import {ReactiveFormsModule} from "@angular/forms";
import {FlexModule} from "@angular/flex-layout";
import {
  SettingsProvisioningComponent
} from "./settings-provisioning/settings-provisioning.component";
import {SettingsDevicePageComponent} from "./settings-device-page/settings-device-page.component";
import {MatTooltipModule} from "@angular/material/tooltip";
import {SettingsUiComponent} from "./settings-ui/settings-ui.component";
import {SettingsKubernetesComponent} from "./settings-kubernetes/settings-kubernetes.component";
import {MatProgressSpinnerModule} from "@angular/material/progress-spinner";
import {MatAutocompleteModule} from "@angular/material/autocomplete";

@NgModule({
  declarations: [
    SettingsComponent,
    SettingsProvisioningComponent,
    SettingsDevicePageComponent,
    SettingsDevregComponent,
    SettingsSecurityComponent,
    SettingsUiComponent,
    SettingsKubernetesComponent,
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
