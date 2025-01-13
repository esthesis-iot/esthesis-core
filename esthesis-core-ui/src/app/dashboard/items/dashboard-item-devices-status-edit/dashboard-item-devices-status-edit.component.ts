import {Component, Inject, OnInit} from "@angular/core";
import {SecurityBaseComponent} from "../../../shared/components/security-base-component";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {DashboardItemDto} from "../../dto/dashboard-item-dto";
import {AppConstants} from "../../../app.constants";
import {
  DashboardItemDevicesStatusConfigurationDto
} from "../../dto/configuration/dashboard-item-devices-status-configuration-dto";

@Component({
  selector: 'app-dashboard-item-devices-status-edit',
  templateUrl: './dashboard-item-devices-status-edit.component.html'
})
export class DashboardItemDevicesStatusEditComponent extends SecurityBaseComponent implements OnInit {
  form!: FormGroup;

  constructor(@Inject(MAT_DIALOG_DATA) public readonly incomingDi: DashboardItemDto,
    readonly fb: FormBuilder, public dialogRef: MatDialogRef<DashboardItemDevicesStatusEditComponent>) {
    super(AppConstants.SECURITY.CATEGORY.DASHBOARD);
  }

  ngOnInit(): void {
    // Parse configuration to DTO.
    let configuration;
    if (this.incomingDi.configuration != null) {
      configuration = JSON.parse(this.incomingDi.configuration) as DashboardItemDevicesStatusConfigurationDto;
    }

    this.form = this.fb.group({
      id: [],
      title: [this.incomingDi.title, [Validators.minLength(3), Validators.maxLength(255), Validators.required]],
      subtitle: [this.incomingDi.subtitle, [Validators.maxLength(2048)]],
      columns: [this.incomingDi.columns, [Validators.required]],
      configuration_orientation: [configuration?.orientation, [Validators.required]],
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
      enabled: this.incomingDi.enabled,
      configuration: JSON.stringify({
        orientation: this.form.get("configuration_orientation")!.value
      } as DashboardItemDevicesStatusConfigurationDto)
    };
    this.dialogRef.close(di);
  }

  cancel() {
    this.dialogRef.close();
  }
}

