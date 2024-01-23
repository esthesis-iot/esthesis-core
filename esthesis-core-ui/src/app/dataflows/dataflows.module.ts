import {NgModule} from "@angular/core";
import {CommonModule} from "@angular/common";

import {DataflowsRoutingModule} from "./dataflows-routing.module";
import {DataflowsListComponent} from "./dataflows-list/dataflows-list.component";
import {MatSortModule} from "@angular/material/sort";
import {ReactiveFormsModule} from "@angular/forms";
import {A11yModule} from "@angular/cdk/a11y";
import {MatIconModule} from "@angular/material/icon";
import {DataflowEditComponent} from "./dataflow-edit/dataflow-edit.component";
import {FormlyModule} from "@ngx-formly/core";
import {FormlyMaterialModule} from "@ngx-formly/material";
import {DataflowEditSectionComponent} from "./dataflow-edit/dataflow-edit-section.component";
import {DataflowNewComponent} from "./dataflow-new/dataflow-new.component";
import {FormlyMatSliderModule} from "@ngx-formly/material/slider";
import {FormlyMatToggleModule} from "@ngx-formly/material/toggle";
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatTableModule} from "@angular/material/table";
import {MatSelectModule} from "@angular/material/select";
import {MatSlideToggleModule} from "@angular/material/slide-toggle";
import {MatCardModule} from "@angular/material/card";
import {MatInputModule} from "@angular/material/input";
import {MatTooltipModule} from "@angular/material/tooltip";
import {MatSliderModule} from "@angular/material/slider";
import {MatPaginatorModule} from "@angular/material/paginator";
import {MatButtonModule} from "@angular/material/button";
import {ComponentsModule} from "../shared/components/components.module";
import {FontAwesomeModule} from "@fortawesome/angular-fontawesome";
import {CdkTableModule} from "@angular/cdk/table";
import {
  DataflowWizardsListComponent
} from "./dataflow-wizards/dataflow-wizards-list/dataflow-wizards-list.component";
import {
  DataflowWizardStandardComponent
} from "./dataflow-wizards/dataflow-wizard-standard/dataflow-wizard-standard.component";
import {RepeatSecretTypeComponent} from "./_custom-types/formly-secrets-section";

@NgModule({
  declarations: [
    DataflowsListComponent,
    DataflowNewComponent,
    DataflowEditComponent,
    DataflowEditSectionComponent,
    DataflowWizardsListComponent,
    DataflowWizardStandardComponent,
    RepeatSecretTypeComponent
  ],
  imports: [
    CommonModule,
    DataflowsRoutingModule,
    MatCardModule,
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
      types: [{name: "repeat-secret", component: RepeatSecretTypeComponent}],
      validationMessages: [
        {name: "required", message: "This field is required"}
      ],
    }),
    FormlyMaterialModule,
    FormlyMatSliderModule,
    FormlyMatToggleModule,
    ComponentsModule,
    FontAwesomeModule,
    CdkTableModule
  ]
})
export class DataflowsModule {
}
