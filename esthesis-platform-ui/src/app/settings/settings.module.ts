import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { SettingsRoutingModule } from './settings-routing.module';
import {SettingsComponent} from './settings.component';
import {SettingsNetworkingComponent} from './settings-networking/settings-networking.component';
import {SettingsOperatorComponent} from './settings-operator/settings-operator.component';
import {SettingsDevregComponent} from './settings-devreg/settings-devreg.component';
import {SettingsSecurityComponent} from './settings-security/settings-security.component';
import {
  MatButtonModule,
  MatCardModule,
  MatDatepickerModule,
  MatFormFieldModule,
  MatIconModule,
  MatInputModule,
  MatMenuModule,
  MatPaginatorModule,
  MatRadioModule,
  MatSelectModule,
  MatSortModule,
  MatTableModule,
  MatTabsModule
} from '@angular/material';
import {QFormsModule} from '@eurodyn/forms';
import {ReactiveFormsModule} from '@angular/forms';
import {FlexModule} from '@angular/flex-layout';
import {SettingsProvisioningComponent} from './settings-provisioning/settings-provisioning.component';

@NgModule({
  declarations: [
    SettingsComponent,
    SettingsProvisioningComponent,
    SettingsNetworkingComponent,
    SettingsOperatorComponent,
    SettingsDevregComponent,
    SettingsSecurityComponent,
  ],
  imports: [
    CommonModule,
    SettingsRoutingModule,
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
    MatRadioModule,
    FlexModule
  ]
})
export class SettingsModule { }
