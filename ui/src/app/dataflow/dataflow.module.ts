import {NgModule} from "@angular/core";
import {CommonModule} from "@angular/common";

import {DataflowRoutingModule} from "./dataflow-routing.module";
import {DataflowComponent} from "./dataflow.component";
import {MatCardModule} from "@angular/material/card";
import {FlexLayoutModule} from "@angular/flex-layout";
import {MatTableModule} from "@angular/material/table";
import {MatSortModule} from "@angular/material/sort";
import {MatPaginatorModule} from "@angular/material/paginator";
import {MatButtonModule} from "@angular/material/button";
import {ReactiveFormsModule} from "@angular/forms";
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatInputModule} from "@angular/material/input";
import {A11yModule} from "@angular/cdk/a11y";
import {MatIconModule} from "@angular/material/icon";
import {MatTooltipModule} from "@angular/material/tooltip";
import {MatSelectModule} from "@angular/material/select";
import {MatSliderModule} from "@angular/material/slider";
import {MatSlideToggleModule} from "@angular/material/slide-toggle";
import {DataflowEditComponent} from "./dataflow-edit/dataflow-edit.component";
import {FormlyModule} from "@ngx-formly/core";
import {FormlyMaterialModule} from "@ngx-formly/material";
import {DataflowEditSectionComponent} from "./dataflow-edit/dataflow-edit-section.component";
import {DataflowNewComponent} from "./dataflow-new/dataflow-new.component";
import {FormlyMatSliderModule} from "@ngx-formly/material/slider";
import {FormlyMatToggleModule} from "@ngx-formly/material/toggle";
import {DisplayModule} from "../shared/component/display/display.module";

@NgModule({
  declarations: [
    DataflowComponent,
    DataflowNewComponent,
    DataflowEditComponent,
    DataflowEditSectionComponent
  ],
  imports: [
    CommonModule,
    DataflowRoutingModule,
    MatCardModule,
    FlexLayoutModule,
    MatTableModule,
    MatSortModule,
    MatPaginatorModule,
    MatButtonModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    A11yModule,
    MatIconModule,
    MatTooltipModule,
    MatSelectModule,
    MatSliderModule,
    MatSlideToggleModule,
    FormlyModule.forChild({
      wrappers: [{name: "section", component: DataflowEditSectionComponent}],
    }),
    FormlyMaterialModule,
    FormlyMatSliderModule,
    FormlyMatToggleModule,
    DisplayModule
  ]
})
export class DataflowModule {
}
