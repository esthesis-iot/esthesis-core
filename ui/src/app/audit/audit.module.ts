import {NgModule} from "@angular/core";
import {CommonModule} from "@angular/common";
import {AuditRoutingModule} from "./audit-routing.module";
import {FlexLayoutModule} from "@angular/flex-layout";
import {MatButtonModule} from "@angular/material/button";
import {MatCardModule} from "@angular/material/card";
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatIconModule} from "@angular/material/icon";
import {MatInputModule} from "@angular/material/input";
import {MatPaginatorModule} from "@angular/material/paginator";
import {MatSelectModule} from "@angular/material/select";
import {MatSortModule} from "@angular/material/sort";
import {MatTableModule} from "@angular/material/table";
import {ReactiveFormsModule} from "@angular/forms";
import {AuditListComponent} from "./audit-list/audit-list.component";
import {QFormsModule} from "@qlack/forms";
import {DateSupportModule} from "../shared/module/date-support.module";

@NgModule({
  declarations: [
    AuditListComponent
  ],
  imports: [
    CommonModule,
    AuditRoutingModule,
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
    DateSupportModule
  ]
})
export class AuditModule {
}
