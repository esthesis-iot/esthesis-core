import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {CertificatesComponent} from './certificates.component';
import {CertificateEditComponent} from './certificate-edit.component';
import {CertificateImportComponent} from './certificate-import.component';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { DateAdapter, MAT_DATE_FORMATS, MAT_DATE_LOCALE } from '@angular/material/core';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatMenuModule } from '@angular/material/menu';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatSelectModule } from '@angular/material/select';
import { MatSortModule } from '@angular/material/sort';
import { MatTableModule } from '@angular/material/table';
import {ReactiveFormsModule} from '@angular/forms';
import {QFormsModule} from '@qlack/forms';
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
