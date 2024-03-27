import {Component} from "@angular/core";
import {FieldArrayType} from "@ngx-formly/core";

@Component({
  selector: "repeat-secret-type-component",
  template: `
    <div>
      <div class="flex flex-row">
        <div *ngFor="let field of field.fieldGroup; let i = index">
          <formly-field [field]="field" class="flex flex-row"></formly-field>
          <div>
            <button class="btn btn-sm" type="button" (click)="remove(i)">REMOVE SECRET</button>
          </div>
        </div>
      </div>
      <div style="margin:30px 0;">
        <button class="btn btn-ghost btn-sm" type="button" (click)="add()">{{ props['addText'] }}</button>
      </div>
    </div>
  `,
})
export class RepeatSecretTypeComponent extends FieldArrayType {}
