import {NgModule} from "@angular/core";
import {CommonModule} from "@angular/common";

import {StoresRoutingModule} from "./stores-routing.module";
import {StoresListComponent} from "./stores-list/stores-list.component";
import {MatCardModule} from "@angular/material/card";
import {MatPaginatorModule} from "@angular/material/paginator";
import {MatTableModule} from "@angular/material/table";
import {MatSortModule} from "@angular/material/sort";
import {MatButtonModule} from "@angular/material/button";
import {StoreEditComponent} from "./store-edit/store-edit.component";
import {ReactiveFormsModule} from "@angular/forms";
import {MatFormFieldModule} from "@angular/material/form-field";
import {FlexLayoutModule} from "@angular/flex-layout";
import {MatInputModule} from "@angular/material/input";
import {QFormsModule} from "@qlack/forms";
import {DisplayModule} from "../shared/component/display/display.module";
import {MatListModule} from "@angular/material/list";
import {MatCheckboxModule} from "@angular/material/checkbox";
import {MatSelectModule} from "@angular/material/select";
import {DateSupportModule} from "../shared/module/date-support.module";


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
    FlexLayoutModule,
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
