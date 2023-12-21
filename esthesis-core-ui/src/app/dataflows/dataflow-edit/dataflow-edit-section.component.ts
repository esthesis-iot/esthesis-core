// TODO: Is this still used?
import {FieldWrapper} from "@ngx-formly/core";
import {Component} from "@angular/core";

@Component({
  selector: "app-dataflow-edit-section",
  template: `
    <div class="mb-5">
      <div class="font-bold text-xl">{{ props.label }}</div>
      <div class="pl-5 border-l-2 border-l-primary/50 rounded-xl">
        <ng-container #fieldComponent></ng-container>
      </div>
    </div>
  `,
})
export class DataflowEditSectionComponent extends FieldWrapper {
}
