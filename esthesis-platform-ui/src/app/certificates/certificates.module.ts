import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {CertificatesComponent} from './certificates.component';
import {CertificateEditComponent} from './certificate-edit.component';
import {CertificateImportComponent} from './certificate-import.component';
import {
  DateAdapter,
  MAT_DATE_FORMATS,
  MAT_DATE_LOCALE,
  MatButtonModule,
  MatCardModule,
  MatDatepickerModule,
  MatFormFieldModule,
  MatInputModule,
  MatMenuModule,
  MatPaginatorModule,
  MatSelectModule,
  MatSortModule,
  MatTableModule
} from '@angular/material';
import {ReactiveFormsModule} from '@angular/forms';
import {QFormsModule} from '@eurodyn/forms';
import {CertificatesRoutingModule} from './certificates-routing.module';
import {FlexLayoutModule} from '@angular/flex-layout';
import {MatMomentDateModule, MomentDateAdapter} from '@angular/material-moment-adapter';
import {DisplayModule} from '../shared/component/display/display.module';

@NgModule({
  declarations: [
    CertificatesComponent,
    CertificateEditComponent,
    CertificateImportComponent,
  ],
  imports: [
    CertificatesRoutingModule,
    CommonModule,
    FlexLayoutModule,
    MatButtonModule,
    MatCardModule,
    MatDatepickerModule,
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
    MatMomentDateModule
  ],
  providers: [
    {
      provide: DateAdapter,
      useClass: MomentDateAdapter,
      deps: [MAT_DATE_LOCALE]
    },
    {
      provide: MAT_DATE_FORMATS,
      useValue: {
        parse: {
          dateInput: 'LL',
        },
        display: {
          dateInput: 'YYYY-MM-DD',
          monthYearLabel: 'MMM YYYY',
          dateA11yLabel: 'LL',
          monthYearA11yLabel: 'MMMM YYYY',
        },
      }
    }
  ]
})
export class CertificatesModule {
}
