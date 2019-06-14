import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';

import {TagsRoutingModule} from './tags-routing.module';
import {TagsComponent} from './tags.component';
import {TagEditComponent} from './tag-edit.component';
import {
  MatButtonModule,
  MatCardModule,
  MatFormFieldModule,
  MatInputModule,
  MatPaginatorModule,
  MatSelectModule,
  MatSortModule,
  MatTableModule
} from '@angular/material';
import {QFormsModule} from '@eurodyn/forms';
import {ReactiveFormsModule} from '@angular/forms';
import {FlexLayoutModule} from '@angular/flex-layout';
import {DisplayModule} from '../shared/component/display/display.module';

@NgModule({
  declarations: [
    TagsComponent,
    TagEditComponent
  ],
  imports: [
    CommonModule,
    TagsRoutingModule,
    FlexLayoutModule,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatPaginatorModule,
    MatSelectModule,
    MatTableModule,
    QFormsModule,
    ReactiveFormsModule,
    MatSortModule,
    DisplayModule
  ]
})
export class TagsModule {
}
