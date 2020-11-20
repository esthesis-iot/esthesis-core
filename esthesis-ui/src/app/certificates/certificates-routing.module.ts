import {RouterModule, Routes} from '@angular/router';
import {NgModule} from '@angular/core';
import {CertificatesComponent} from './certificates.component';
import {CertificateImportComponent} from './certificate-import.component';
import {CertificateEditComponent} from './certificate-edit.component';

const routes: Routes = [
  {path: '', component: CertificatesComponent},
  {path: 'import', component: CertificateImportComponent},
  {path: ':id', component: CertificateEditComponent},
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class CertificatesRoutingModule {
}
