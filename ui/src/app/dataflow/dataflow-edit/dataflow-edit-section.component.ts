import {FieldWrapper} from "@ngx-formly/core";
import {Component} from "@angular/core";

@Component({
  selector: "app-dataflow-edit-section",
  template: `
    <div class="inline-subheader">{{ props.label }}</div>
    <div class="settings-section" fxLayout="column">
      <ng-container #fieldComponent></ng-container>
    </div>
  `,
})
export class DataflowEditSectionComponent extends FieldWrapper {
}
