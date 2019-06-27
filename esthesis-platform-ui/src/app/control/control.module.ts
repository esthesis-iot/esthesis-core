import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ControlComponent } from './control.component';
import {ControlRoutingModule} from './control-routing.module';
import {
  DateAdapter, MAT_DATE_FORMATS, MAT_DATE_LOCALE,
  MatButtonModule,
  MatCardModule,
  MatDatepickerModule,
  MatFormFieldModule, MatIconModule, MatInputModule, MatPaginatorModule,
  MatSelectModule, MatSortModule, MatTableModule
} from '@angular/material';
import {ReactiveFormsModule} from '@angular/forms';
import {DisplayModule} from '../shared/component/display/display.module';
import {FlexLayoutModule} from '@angular/flex-layout';
import {QFormsModule} from '@eurodyn/forms';
import {MomentModule} from 'ngx-moment';
import {MatMomentDateModule, MomentDateAdapter} from '@angular/material-moment-adapter';
import {AuditRoutingModule} from '../audit/audit-routing.module';
import {CommandsModule} from '../shared/component/commands/commands.module';

@NgModule({
  declarations: [ControlComponent],
  imports: [
    CommonModule,
    ControlRoutingModule,
    FlexLayoutModule,
    MatButtonModule,
    MatCardModule,
    MatDatepickerModule,
    MatFormFieldModule,
    MatInputModule,
    MatPaginatorModule,
    MatSelectModule,
    MatTableModule,
    QFormsModule,
    ReactiveFormsModule,
    MatSortModule,
    MatIconModule,
    MomentModule,
    MatMomentDateModule,
    CommandsModule
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
export class ControlModule { }
