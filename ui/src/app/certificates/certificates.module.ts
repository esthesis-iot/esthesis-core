import {NgModule} from "@angular/core";
import {CommonModule} from "@angular/common";
import {CertificatesListComponent} from "./certificates-list/certificates-list.component";
import {CertificateEditComponent} from "./certificate-edit/certificate-edit.component";
import {CertificateImportComponent} from "./certificate-import/certificate-import.component";
import {MatSortModule} from "@angular/material/sort";
import {ReactiveFormsModule} from "@angular/forms";
import {QFormsModule} from "@qlack/forms";
import {CertificatesRoutingModule} from "./certificates-routing.module";
import {MatMenuModule} from "@angular/material/menu";
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatSelectModule} from "@angular/material/select";
import {MatTableModule} from "@angular/material/table";
import {MatPaginatorModule} from "@angular/material/paginator";
import {MatCardModule} from "@angular/material/card";
import {MatInputModule} from "@angular/material/input";
import {MatButtonModule} from "@angular/material/button";
import {ComponentsModule} from "../shared/components/components.module";
import {DateSupportModule} from "../shared/modules/date-support.module";

@NgModule({
  declarations: [
    CertificatesListComponent,
    CertificateEditComponent,
    CertificateImportComponent,
  ],
  imports: [
    CertificatesRoutingModule,
    CommonModule,
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
    ComponentsModule,
    DateSupportModule
  ]
})
export class CertificatesModule {
}
