import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {
  DateAdapter,
  MAT_DATE_FORMATS, MAT_DATE_LOCALE,
  MatButtonModule,
  MatCardModule,
  MatDatepickerModule,
  MatFormFieldModule, MatIconModule,
  MatInputModule,
  MatMenuModule,
  MatPaginatorModule,
  MatSelectModule,
  MatSortModule,
  MatTableModule, MatTabsModule
} from '@angular/material';
import {ReactiveFormsModule} from '@angular/forms';
import {QFormsModule} from '@eurodyn/forms';
import {FlexLayoutModule} from '@angular/flex-layout';
import {ApplicationsComponent} from './applications.component';
import {ApplicationEditComponent} from './application-edit/application-edit.component';
import {ApplicationEditDescriptionComponent} from './application-edit/application-edit-description.component';
import {ApplicationEditPermissionsComponent} from './application-edit/application-edit-permissions.component';
import {ApplicationsRoutingModule} from './applications-routing.module';
import {MomentDateAdapter} from '@angular/material-moment-adapter';
import {DisplayModule} from '../shared/display/display.module';

@NgModule({
  declarations: [
    ApplicationsComponent,
    ApplicationEditComponent,
    ApplicationEditDescriptionComponent,
    ApplicationEditPermissionsComponent,
  ],
  imports: [
    ApplicationsRoutingModule,
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
    MatIconModule,
    MatTabsModule,
    DisplayModule
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
export class ApplicationsModule {
}
