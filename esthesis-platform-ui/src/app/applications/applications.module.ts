import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { DateAdapter, MAT_DATE_FORMATS, MAT_DATE_LOCALE } from '@angular/material/core';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatMenuModule } from '@angular/material/menu';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatSelectModule } from '@angular/material/select';
import { MatSortModule } from '@angular/material/sort';
import { MatTableModule } from '@angular/material/table';
import { MatTabsModule } from '@angular/material/tabs';
import {ReactiveFormsModule} from '@angular/forms';
import {QFormsModule} from '@qlack/forms';
import {FlexLayoutModule} from '@angular/flex-layout';
import {ApplicationsComponent} from './applications.component';
import {ApplicationEditComponent} from './application-edit/application-edit.component';
import {ApplicationEditDescriptionComponent} from './application-edit/application-edit-description.component';
import {ApplicationEditPermissionsComponent} from './application-edit/application-edit-permissions.component';
import {ApplicationsRoutingModule} from './applications-routing.module';
import {MomentDateAdapter} from '@angular/material-moment-adapter';
import {DisplayModule} from '../shared/component/display/display.module';

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
