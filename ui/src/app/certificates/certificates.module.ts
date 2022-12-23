import {NgModule} from "@angular/core";
import {CommonModule} from "@angular/common";
import {CertificatesListComponent} from "./certificates-list/certificates-list.component";
import {CertificateEditComponent} from "./certificate-edit/certificate-edit.component";
import {CertificateImportComponent} from "./certificate-import/certificate-import.component";
import {MatButtonModule} from "@angular/material/button";
import {MatCardModule} from "@angular/material/card";
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatInputModule} from "@angular/material/input";
import {MatMenuModule} from "@angular/material/menu";
import {MatPaginatorModule} from "@angular/material/paginator";
import {MatSelectModule} from "@angular/material/select";
import {MatSortModule} from "@angular/material/sort";
import {MatTableModule} from "@angular/material/table";
import {ReactiveFormsModule} from "@angular/forms";
import {QFormsModule} from "@qlack/forms";
import {CertificatesRoutingModule} from "./certificates-routing.module";
import {FlexLayoutModule} from "@angular/flex-layout";
import {DisplayModule} from "../shared/component/display/display.module";
import {DateSupportModule} from "../shared/module/date-support.module";

@NgModule({
  declarations: [
    CertificatesListComponent,
    CertificateEditComponent,
    CertificateImportComponent,
  ],
  imports: [
    CertificatesRoutingModule,
    CommonModule,
    FlexLayoutModule,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatMenuModule,
    MatPaginatorModule,
    MatSelectModule,
    MatTableModule,
    QFormsModule,
    ReactiveFormsModule,
    MatSortModule,
    DisplayModule,
    DateSupportModule
  ]
})
export class CertificatesModule {
}
