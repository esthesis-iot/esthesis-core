import {Component, Inject, OnInit} from "@angular/core";
import {SecurityBaseComponent} from "../../../shared/components/security-base-component";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {DashboardItemDto} from "../../dto/dashboard-item-dto";
import {AppConstants} from "../../../app.constants";
import {
  DashboardItemDevicesLatestConfigurationDto
} from "../../dto/configuration/dashboard-item-devices-latest-configuration-dto";

@Component({
  selector: 'app-dashboard-item-devices-latest-edit',
  templateUrl: './dashboard-item-devices-latest-edit.component.html'
})
export class DashboardItemDevicesLatestEditComponent extends SecurityBaseComponent implements OnInit {
  form!: FormGroup;

  constructor(@Inject(MAT_DIALOG_DATA) public readonly incomingDi: DashboardItemDto,
    readonly fb: FormBuilder, public dialogRef: MatDialogRef<DashboardItemDevicesLatestEditComponent>) {
    super(AppConstants.SECURITY.CATEGORY.DEVICE);
  }

  ngOnInit(): void {
    // Parse configuration to DTO.
    let configuration;
    if (this.incomingDi.configuration != null) {
      configuration = JSON.parse(this.incomingDi.configuration) as DashboardItemDevicesLatestConfigurationDto;
    }

    this.form = this.fb.group({
      id: [],
      title: [this.incomingDi.title, [Validators.minLength(3), Validators.maxLength(255), Validators.required]],
      subtitle: [this.incomingDi.subtitle, [Validators.maxLength(2048)]],
      columns: [this.incomingDi.columns, [Validators.required]],
      configuration_entries: [configuration?.entries, [Validators.required, Validators.pattern('^[0-9]+$')]],
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
        entries: this.form.get("configuration_entries")!.value
      } as DashboardItemDevicesLatestConfigurationDto)
    };
    this.dialogRef.close(di);
  }

  cancel() {
    this.dialogRef.close();
  }
}


