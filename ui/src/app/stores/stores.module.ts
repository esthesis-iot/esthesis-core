import {NgModule} from "@angular/core";
import {CommonModule} from "@angular/common";

import {StoresRoutingModule} from "./stores-routing.module";
import {StoresListComponent} from "./stores-list/stores-list.component";
import {MatSortModule} from "@angular/material/sort";
import {StoreEditComponent} from "./store-edit/store-edit.component";
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
    ComponentsModule,
    MatListModule,
    MatCheckboxModule,
    MatSelectModule,
    DateSupportModule
  ]
})
export class StoresModule {
}
