import {RouterModule, Routes} from "@angular/router";
import {NgModule} from "@angular/core";
import {CertificatesListComponent} from "./certificates-list/certificates-list.component";
import {CertificateImportComponent} from "./certificate-import/certificate-import.component";
import {CertificateEditComponent} from "./certificate-edit/certificate-edit.component";

const routes: Routes = [
  {path: "", component: CertificatesListComponent},
  {path: "import", component: CertificateImportComponent},
  {path: ":id", component: CertificateEditComponent},
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class CertificatesRoutingModule {
}
