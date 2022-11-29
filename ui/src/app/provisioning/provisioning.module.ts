import {NgModule, NO_ERRORS_SCHEMA} from "@angular/core";
import {CommonModule} from "@angular/common";

import {ProvisioningRoutingModule} from "./provisioning-routing.module";
import {FlexLayoutModule} from "@angular/flex-layout";
import {MatButtonModule} from "@angular/material/button";
import {MatCardModule} from "@angular/material/card";
import {MatCheckboxModule} from "@angular/material/checkbox";
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatIconModule} from "@angular/material/icon";
import {MatInputModule} from "@angular/material/input";
import {MatPaginatorModule} from "@angular/material/paginator";
import {MatSelectModule} from "@angular/material/select";
import {MatSortModule} from "@angular/material/sort";
import {MatTableModule} from "@angular/material/table";
import {QFormsModule} from "@qlack/forms";
import {ReactiveFormsModule} from "@angular/forms";
import {NgxFilesizeModule} from "ngx-filesize";
import {DisplayModule} from "../shared/component/display/display.module";
import {ProvisioningEditComponent} from "./provisioning-edit/provisioning-edit.component";
import {ProvisioningListComponent} from "./provisioning-list/provisioning-list.component";
import {MatProgressBarModule} from "@angular/material/progress-bar";

@NgModule({
  declarations: [
    ProvisioningListComponent,
    ProvisioningEditComponent,
  ],
  imports: [
    CommonModule,
    ProvisioningRoutingModule,
    FlexLayoutModule,
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
    DisplayModule,
    MatProgressBarModule
  ],
  schemas: [NO_ERRORS_SCHEMA],
})
export class ProvisioningModule {
}
