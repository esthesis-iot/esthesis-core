import {NgModule} from "@angular/core";
import {CommonModule} from "@angular/common";
import {AuditRoutingModule} from "./audit-routing.module";
import {FlexLayoutModule} from "@angular/flex-layout";
import {MatIconModule} from "@angular/material/icon";
import {MatSortModule} from "@angular/material/sort";
import {ReactiveFormsModule} from "@angular/forms";
import {AuditListComponent} from "./audit-list/audit-list.component";
import {QFormsModule} from "@qlack/forms";
import {DateSupportModule} from "../shared/module/date-support.module";
import {AuditViewComponent} from "./audit-view/audit-view.component";
import {NgxJsonViewerModule} from "ngx-json-viewer";
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatSelectModule} from "@angular/material/select";
import {MatTableModule} from "@angular/material/table";
import {MatPaginatorModule} from "@angular/material/paginator";
import {MatCardModule} from "@angular/material/card";
import {MatInputModule} from "@angular/material/input";
import {MatButtonModule} from "@angular/material/button";

@NgModule({
  declarations: [
    AuditListComponent,
    AuditViewComponent
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
    DateSupportModule,
    NgxJsonViewerModule
  ]
})
export class AuditModule {
}
