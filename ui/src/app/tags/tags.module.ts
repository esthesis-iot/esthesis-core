import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';

import {TagsRoutingModule} from './tags-routing.module';
import {TagsListComponent} from './tags-list/tags-list.component';
import {TagEditComponent} from './tag-edit/tag-edit.component';
import {MatButtonModule} from '@angular/material/button';
import {MatCardModule} from '@angular/material/card';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {MatPaginatorModule} from '@angular/material/paginator';
import {MatSelectModule} from '@angular/material/select';
import {MatSortModule} from '@angular/material/sort';
import {MatTableModule} from '@angular/material/table';
import {QFormsModule} from '@qlack/forms';
import {ReactiveFormsModule} from '@angular/forms';
import {FlexLayoutModule} from '@angular/flex-layout';
import {DisplayModule} from '../shared/component/display/display.module';
import {MatIconModule} from '@angular/material/icon';
import {MatTooltipModule} from '@angular/material/tooltip';
import {A11yModule} from "@angular/cdk/a11y";

@NgModule({
  declarations: [
    TagsListComponent,
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
    DisplayModule,
    MatIconModule,
    MatTooltipModule,
    A11yModule
  ]
})
export class TagsModule {
}
