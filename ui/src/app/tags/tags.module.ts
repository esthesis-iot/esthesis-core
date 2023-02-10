import {NgModule} from "@angular/core";
import {CommonModule} from "@angular/common";

import {TagsRoutingModule} from "./tags-routing.module";
import {TagsListComponent} from "./tags-list/tags-list.component";
import {TagEditComponent} from "./tag-edit/tag-edit.component";
import {MatSortModule} from "@angular/material/sort";
import {QFormsModule} from "@qlack/forms";
import {ReactiveFormsModule} from "@angular/forms";
import {DisplayModule} from "../shared/component/display/display.module";
import {MatIconModule} from "@angular/material/icon";
import {A11yModule} from "@angular/cdk/a11y";
import {MatButtonModule} from "@angular/material/button";
import {MatCardModule} from "@angular/material/card";
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatPaginatorModule} from "@angular/material/paginator";
import {MatTableModule} from "@angular/material/table";
import {MatTooltipModule} from "@angular/material/tooltip";
import {MatInputModule} from "@angular/material/input";
import {MatSelectModule} from "@angular/material/select";

@NgModule({
  declarations: [
    TagsListComponent,
    TagEditComponent
  ],
  imports: [
    CommonModule,
    TagsRoutingModule,
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
