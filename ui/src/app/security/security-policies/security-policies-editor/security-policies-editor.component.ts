import {Component, Input, OnInit, Optional} from "@angular/core";
import {BaseComponent} from "../../../shared/components/base-component";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {Router} from "@angular/router";
import {QFormsService} from "@qlack/forms";
import {MatDialogRef} from "@angular/material/dialog";
import * as _ from "lodash";
import {RuleDto} from "../../dto/rule-dto";

@Component({
  selector: "app-security-policies-editor",
  templateUrl: "./security-policies-editor.component.html"
})
export class SecurityPoliciesEditorComponent extends BaseComponent implements OnInit {
  @Input() existingErn?: string;
  // Search filter.
  form: FormGroup;

  constructor(private fb: FormBuilder, private router: Router,
    private qForms: QFormsService,
    @Optional() private dialogRef: MatDialogRef<SecurityPoliciesEditorComponent>) {
    super();
    this.form = this.fb.group({
      root: ["ern", [Validators.required]],
      system: ["esthesis", [Validators.required]],
      subsystem: ["core", [Validators.required]],
      objectType: ["", [Validators.required]],
      objectId: ["", [Validators.required]],
      operation: ["", [Validators.required]],
      permission: ["", [Validators.required]],
    });
  }

  ngOnInit(): void {
    // If an existing ern is edited, fill-in the form with the existing values.
    if (this.existingErn) {
      const ern = RuleDto.deserialize(this.existingErn);
      this.form.patchValue(ern);
    }
  }

  save() {
    this.dialogRef.close(this.getErnAsString());
  }

  getErnAsString(): string {
    return RuleDto.serialize(this.getErn());
  }

  getErn(): RuleDto {
    return _.cloneDeep(this.form.value) as RuleDto;
  }

  close() {
    this.dialogRef.close();
  }
}
