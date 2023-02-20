import {NgModule} from "@angular/core";
import {CommonModule} from "@angular/common";

import {UsersRoutingModule} from "./users-routing.module";
import {UsersListComponent} from "./users-list/users-list.component";
import {UserEditComponent} from "./user-edit/user-edit.component";
import {MatIconModule} from "@angular/material/icon";
import {MatSortModule} from "@angular/material/sort";
import {QFormsModule} from "@qlack/forms";
import {ReactiveFormsModule} from "@angular/forms";
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatSelectModule} from "@angular/material/select";
import {MatTableModule} from "@angular/material/table";
import {MatPaginatorModule} from "@angular/material/paginator";
import {MatCardModule} from "@angular/material/card";
import {MatInputModule} from "@angular/material/input";
import {MatButtonModule} from "@angular/material/button";
import {ComponentsModule} from "../shared/components/components.module";

@NgModule({
  declarations: [
    UsersListComponent,
    UserEditComponent,
  ],
  imports: [
    CommonModule,
    UsersRoutingModule,
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
    ComponentsModule
  ]
})
export class UsersModule {
}
