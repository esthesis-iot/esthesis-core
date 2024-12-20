import {Component, Inject, OnInit} from "@angular/core";
import {SecurityBaseComponent} from "../../../shared/components/security-base-component";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {DashboardItemDto} from "../../dto/view-edit/dashboard-item-dto";
import {AppConstants} from "../../../app.constants";

@Component({
  selector: 'app-dashboard-item-campaigns-edit',
  templateUrl: './dashboard-item-campaigns-edit.component.html'
})
export class DashboardItemCampaignsEditComponent extends SecurityBaseComponent implements OnInit {
  form!: FormGroup;

  constructor(@Inject(MAT_DIALOG_DATA) public readonly incomingDi: DashboardItemDto,
    readonly fb: FormBuilder, public dialogRef: MatDialogRef<DashboardItemCampaignsEditComponent>) {
    super(AppConstants.SECURITY.CATEGORY.DASHBOARD);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      id: [],
      title: [this.incomingDi.title, [Validators.minLength(3), Validators.maxLength(255), Validators.required]],
      subtitle: [this.incomingDi.subtitle, [Validators.maxLength(2048)]],
      columns: [this.incomingDi.columns, [Validators.required]]
    });

    this.isFormDisabled().subscribe(disabled => disabled && this.form.disable());
  }

  save() {
    let di: DashboardItemDto = {
      id: this.incomingDi.id,
      type: this.incomingDi.type,
      title: this.form.get("title")!.value,
      subtitle: this.form.get("subtitle")!.value,
      columns: this.form.get("columns")!.value,
      index: this.incomingDi.index,
      enabled: this.incomingDi.enabled
    };
    this.dialogRef.close(di);
  }

  cancel() {
    this.dialogRef.close();
  }
}
