import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {DatasinksComponent} from './datasinks.component';
import {DatasinksEditComponent} from './datasinks-edit.component';
import {FlexLayoutModule} from '@angular/flex-layout';
import {
  MAT_DATE_FORMATS,
  MatButtonModule,
  MatCardModule,
  MatCheckboxModule,
  MatDatepickerModule,
  MatFormFieldModule,
  MatIconModule,
  MatInputModule,
  MatMenuModule,
  MatPaginatorModule,
  MatSelectModule,
  MatSlideToggleModule,
  MatSortModule,
  MatTableModule,
  MatTooltipModule
} from '@angular/material';
import {QFormsModule} from '@eurodyn/forms';
import {ReactiveFormsModule} from '@angular/forms';
import {MatMomentDateModule} from '@angular/material-moment-adapter';
import {DatasinksRoutingModule} from './datasinks-routing.module';
import {DisplayModule} from '../shared/component/display/display.module';

@NgModule({
  declarations: [DatasinksComponent, DatasinksEditComponent],
  imports: [
    CommonModule,
    DatasinksRoutingModule,
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
    MatMomentDateModule,
    MatCheckboxModule,
    MatIconModule,
    MatTooltipModule,
    MatSlideToggleModule
  ],
  providers: [
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
export class DatasinksModule {
}
