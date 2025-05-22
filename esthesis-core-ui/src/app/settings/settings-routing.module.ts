import {NgModule} from "@angular/core";
import {RouterModule, Routes} from "@angular/router";
import {SettingsComponent} from "./settings.component";
import {SettingsMessagingComponent} from "./settings-messaging/settings-messaging.component";
import {SettingsDevicePageComponent} from "./settings-device-page/settings-device-page.component";
import {SettingsDevregComponent} from "./settings-devreg/settings-devreg.component";
import {SettingsSecurityComponent} from "./settings-security/settings-security.component";
import {
  SettingsProvisioningComponent
} from "./settings-provisioning/settings-provisioning.component";

const routes: Routes = [
  {path: "", component: SettingsComponent, data: {breadcrumb: ""},
    children: [
      {path: "device-registration", component: SettingsDevregComponent, data: {breadcrumb: ""}},
      {path: "device-page", component: SettingsDevicePageComponent, data: {breadcrumb: ""}},
      {path: "security", component: SettingsSecurityComponent, data: {breadcrumb: ""}},
      {path: "provisioning", component: SettingsProvisioningComponent, data: {breadcrumb: ""}},
      {path: "messaging", component: SettingsMessagingComponent, data: {breadcrumb: ""}},
      {path: "", redirectTo: "device-registration", pathMatch: "full"},
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class SettingsRoutingModule {
}
