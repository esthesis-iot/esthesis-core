import {NgModule, NO_ERRORS_SCHEMA} from "@angular/core";
import {CommonModule} from "@angular/common";

import {ProvisioningRoutingModule} from "./provisioning-routing.module";
import {MatIconModule} from "@angular/material/icon";
import {MatSortModule} from "@angular/material/sort";
import {QFormsModule} from "@qlack/forms";
import {ReactiveFormsModule} from "@angular/forms";
import {NgxFilesizeModule} from "ngx-filesize";
import {ProvisioningEditComponent} from "./provisioning-edit/provisioning-edit.component";
import {ProvisioningListComponent} from "./provisioning-list/provisioning-list.component";
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatSelectModule} from "@angular/material/select";
import {MatTableModule} from "@angular/material/table";
import {MatCheckboxModule} from "@angular/material/checkbox";
import {MatProgressBarModule} from "@angular/material/progress-bar";
import {MatPaginatorModule} from "@angular/material/paginator";
import {MatCardModule} from "@angular/material/card";
import {MatInputModule} from "@angular/material/input";
import {MatButtonModule} from "@angular/material/button";
import {FontAwesomeModule} from "@fortawesome/angular-fontawesome";
import {CdkTableModule} from "@angular/cdk/table";
import {ComponentsModule} from "../shared/components/components.module";

@NgModule({
  declarations: [
    ProvisioningListComponent,
    ProvisioningEditComponent,
  ],
  imports: [
    CommonModule,
    ProvisioningRoutingModule,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatPaginatorModule,
    MatSelectModule,
    MatTableModule,
    QFormsModule,
    ReactiveFormsModule,
    MatSortModule,
    MatIconModule,
    NgxFilesizeModule,
    MatCheckboxModule,
    ComponentsModule,
    MatProgressBarModule,
    FontAwesomeModule,
    CdkTableModule
  ],
  schemas: [NO_ERRORS_SCHEMA],
})
export class ProvisioningModule {
}
