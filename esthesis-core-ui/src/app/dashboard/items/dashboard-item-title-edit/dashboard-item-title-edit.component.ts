import {Component, Inject, OnInit} from "@angular/core";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {DashboardItemDto} from "../../dto/dashboard-item-dto";
import {BaseComponent} from "../../../shared/components/base-component";
import {
  DashboardItemTitleConfigurationDto
} from "../../dto/configuration/dashboard-item-title-configuration-dto";

@Component({
  selector: 'app-dashboard-item-title-edit',
  templateUrl: './dashboard-item-title-edit.component.html'
})
export class DashboardItemTitleEditComponent extends BaseComponent implements OnInit {
  form!: FormGroup;

  constructor(@Inject(MAT_DIALOG_DATA) public readonly incomingDi: DashboardItemDto,
    readonly fb: FormBuilder, public dialogRef: MatDialogRef<DashboardItemTitleEditComponent>) {
    super();
  }

  ngOnInit(): void {
    // Parse configuration to DTO.
    let configuration;
    if (this.incomingDi.configuration != null) {
      configuration = JSON.parse(this.incomingDi.configuration) as DashboardItemTitleConfigurationDto;
    }

    this.form = this.fb.group({
      id: [],
      title: [this.incomingDi.title, [Validators.minLength(3), Validators.maxLength(255), Validators.required]],
      subtitle: [this.incomingDi.subtitle, [Validators.maxLength(2048)]],
      columns: [this.incomingDi.columns, [Validators.required]],
      configuration_title: [configuration?.title, [Validators.required, Validators.maxLength(255)]],
    });
  }

  save() {
    let di: DashboardItemDto = {
      id: this.incomingDi.id,
      type: this.incomingDi.type,
      title: this.form.get("title")!.value,
      subtitle: this.form.get("subtitle")!.value,
      columns: this.form.get("columns")!.value,
      index: this.incomingDi.index,
      enabled: this.incomingDi.enabled,
      configuration: JSON.stringify({
        title: this.form.get("configuration_title")!.value
      } as DashboardItemTitleConfigurationDto)
    };
    this.dialogRef.close(di);
  }

  cancel() {
    this.dialogRef.close();
  }
}

