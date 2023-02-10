import {NgModule} from "@angular/core";
import {CommonModule} from "@angular/common";

import {StoresRoutingModule} from "./stores-routing.module";
import {StoresListComponent} from "./stores-list/stores-list.component";
import {MatSortModule} from "@angular/material/sort";
import {StoreEditComponent} from "./store-edit/store-edit.component";
import {ReactiveFormsModule} from "@angular/forms";
import {QFormsModule} from "@qlack/forms";
import {DisplayModule} from "../shared/component/display/display.module";
import {DateSupportModule} from "../shared/module/date-support.module";
import {MatCardModule} from "@angular/material/card";
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatTableModule} from "@angular/material/table";
import {MatCheckboxModule} from "@angular/material/checkbox";
import {MatSelectModule} from "@angular/material/select";
import {MatPaginatorModule} from "@angular/material/paginator";
import {MatListModule} from "@angular/material/list";
import {MatInputModule} from "@angular/material/input";
import {MatButtonModule} from "@angular/material/button";


@NgModule({
  declarations: [StoresListComponent, StoreEditComponent],
  imports: [
    CommonModule,
    StoresRoutingModule,
    MatCardModule,
    MatPaginatorModule,
    MatTableModule,
    MatSortModule,
    MatButtonModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    QFormsModule,
    DisplayModule,
    MatListModule,
    MatCheckboxModule,
    MatSelectModule,
    DateSupportModule
  ]
})
export class StoresModule {
}
