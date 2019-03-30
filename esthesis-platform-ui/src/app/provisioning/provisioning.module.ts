import {NgModule, NO_ERRORS_SCHEMA} from '@angular/core';
import {CommonModule} from '@angular/common';

import {ProvisioningRoutingModule} from './provisioning-routing.module';
import {ProvisioningComponent} from './provisioning.component';
import {ProvisioningEditComponent} from './provisioning-edit.component';
import {FlexLayoutModule} from '@angular/flex-layout';
import {
  MatButtonModule,
  MatCardModule, MatCheckboxModule,
  MatDatepickerModule,
  MatFormFieldModule,
  MatIconModule,
  MatInputModule,
  MatPaginatorModule,
  MatSelectModule,
  MatSortModule,
  MatTableModule
} from '@angular/material';
import {QFormsModule} from '@eurodyn/forms';
import {ReactiveFormsModule} from '@angular/forms';
import {FileSizeModule} from 'ngx-filesize';
import {DisplayModule} from '../shared/display/display.module';

@NgModule({
  declarations: [
    ProvisioningComponent,
    ProvisioningEditComponent,
  ],
  imports: [
    CommonModule,
    ProvisioningRoutingModule,
    FlexLayoutModule,
    MatButtonModule,
    MatCardModule,
    MatDatepickerModule,
    MatFormFieldModule,
    MatInputModule,
    MatPaginatorModule,
    MatSelectModule,
    MatTableModule,
    QFormsModule,
    ReactiveFormsModule,
    MatSortModule,
    MatIconModule,
    FileSizeModule,
    MatCheckboxModule,
    DisplayModule
  ],
  schemas: [NO_ERRORS_SCHEMA],
})
export class ProvisioningModule {
}
