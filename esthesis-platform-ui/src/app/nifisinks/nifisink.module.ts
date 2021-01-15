import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {NiFiSinkComponent} from './nifi-sink.component';
import {NiFiSinkEditComponent} from './nifi-sink-edit.component';
import {FlexLayoutModule} from '@angular/flex-layout';
import {MatButtonModule} from '@angular/material/button';
import {MatCardModule} from '@angular/material/card';
import {MatCheckboxModule} from '@angular/material/checkbox';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatIconModule} from '@angular/material/icon';
import {MatInputModule} from '@angular/material/input';
import {MatMenuModule} from '@angular/material/menu';
import {MatPaginatorModule} from '@angular/material/paginator';
import {MatSelectModule} from '@angular/material/select';
import {MatSlideToggleModule} from '@angular/material/slide-toggle';
import {MatSortModule} from '@angular/material/sort';
import {MatTableModule} from '@angular/material/table';
import {MatTooltipModule} from '@angular/material/tooltip';
import {QFormsModule} from '@qlack/forms';
import {ReactiveFormsModule} from '@angular/forms';
import {NiFiSinkRoutingModule} from './nifi-sink-routing.module';
import {DisplayModule} from '../shared/component/display/display.module';

@NgModule({
  declarations: [NiFiSinkComponent, NiFiSinkEditComponent],
  imports: [
    CommonModule,
    NiFiSinkRoutingModule,
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
    MatCheckboxModule,
    MatIconModule,
    MatTooltipModule,
    MatSlideToggleModule
  ]
})
export class NifisinkModule {
}
