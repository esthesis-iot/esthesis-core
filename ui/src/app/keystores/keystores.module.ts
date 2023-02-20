import {NgModule} from "@angular/core";
import {CommonModule} from "@angular/common";

import {KeystoresRoutingModule} from "./keystores-routing.module";
import {MatSortModule} from "@angular/material/sort";
import {ReactiveFormsModule} from "@angular/forms";
import {QFormsModule} from "@qlack/forms";
import {MatCardModule} from "@angular/material/card";
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatTableModule} from "@angular/material/table";
import {MatCheckboxModule} from "@angular/material/checkbox";
import {MatSelectModule} from "@angular/material/select";
import {MatPaginatorModule} from "@angular/material/paginator";
import {MatListModule} from "@angular/material/list";
import {MatInputModule} from "@angular/material/input";
import {MatButtonModule} from "@angular/material/button";
import {DateSupportModule} from "../shared/modules/date-support.module";
import {ComponentsModule} from "../shared/components/components.module";
import {FontAwesomeModule} from "@fortawesome/angular-fontawesome";
import {CdkTableModule} from "@angular/cdk/table";
import {KeystoresListComponent} from "./keystores-list/keystores-list.component";
import {KeystoreEditComponent} from "./keystore-edit/keystore-edit.component";


@NgModule({
  declarations: [KeystoresListComponent, KeystoreEditComponent],
  imports: [
    CommonModule,
    KeystoresRoutingModule,
    MatCardModule,
    MatPaginatorModule,
    MatTableModule,
    MatSortModule,
    MatButtonModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    QFormsModule,
    ComponentsModule,
    MatListModule,
    MatCheckboxModule,
    MatSelectModule,
    DateSupportModule,
    FontAwesomeModule,
    CdkTableModule
  ]
})
export class KeystoresModule {
}
