import {Component, Inject, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from "@angular/material/dialog";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {AppConstants} from "../../../app.constants";
import {DashboardService} from "../../dashboard.service";
import {DashboardWidgetDto} from "../../../dto/dashboard-widget-dto";
import {OkCancelModalComponent} from "../../../shared/component/display/ok-cancel-modal/ok-cancel-modal.component";
import {UtilityService} from "../../../shared/service/utility.service";
import {DevicesService} from "../../../devices/devices.service";
import {DeviceDto} from "../../../dto/device-dto";
import "rxjs-compat/add/operator/debounceTime";
import {BaseComponent} from "../../../shared/component/base-component";
import {AppExtendedConstants} from "../../../app.extended-constants";
import {WidgetSensorValueConf} from "./widget-sensor-value-conf";
import {FormatterService} from "../../../shared/service/formatter.service";

@Component({
  selector: 'app-widget-sensor-value-setup',
  templateUrl: './widget-sensor-value-setup.component.html',
  styleUrls: ['./widget-sensor-value-setup.component.scss']
})
export class WidgetSensorValueSetupComponent extends BaseComponent implements OnInit {
  form!: FormGroup;
  devices!: DeviceDto[];
  extendedConstants = AppExtendedConstants;

  constructor(private fb: FormBuilder, private dialog: MatDialog,
              public dialogRef: MatDialogRef<WidgetSensorValueSetupComponent>,
              @Inject(MAT_DIALOG_DATA) public data: any, private dashboardService: DashboardService,
              private utilityService: UtilityService, private devicesService: DevicesService,
              private formatterService: FormatterService) {
    super();
  }

  ngOnInit(): void {
    // Setup the form.
    this.form = this.fb.group({
      gridCols: [1, [Validators.required]],
      gridRows: [1, [Validators.required]],
      gridX: [0, [Validators.required]],
      gridY: [0, [Validators.required]],
      icon: [],
      title: [],
      hardwareId: ['', [Validators.required]],
      measurement: [''],
      bgColor: [],
      fgColor: [],
      updateEvery: [60, [Validators.required]]
    });

    // If editing an existing widget, fetch widget configuration.
    if (this.data.id !== 0) {
      this.dashboardService.getWidget(this.data.id).subscribe(onNext => {
        this.form.patchValue(onNext);
        const conf: WidgetSensorValueConf = JSON.parse(onNext.configuration);
        this.form.patchValue(conf);
        this.form.patchValue({
          fgColor: this.formatterService.rgbaStringToColor(conf.fgColor),
          bgColor: this.formatterService.rgbaStringToColor(conf.bgColor)
        });
      })
    }

    // Monitor devices search autocomplete.
    this.form.get("hardwareId")!.valueChanges.debounceTime(500).subscribe(onNext => {
      if (onNext && onNext.trim() !== "") {
        this.devicesService.findDeviceByPartialHardwareId(onNext).subscribe(
          onNext => {
            if (onNext && onNext.length > 0) {
              this.devices = onNext;
            } else {
              this.devices = [];
            }
          }
        );
      } else {
        this.devices = [];
      }
    });
  }

  delete() {
    this.dialog.open(OkCancelModalComponent, {
      data: {
        title: 'Delete widget',
        question: 'Do you really want to delete this widget?',
        buttons: {
          ok: true, cancel: true, reload: false
        }
      }
    }).afterClosed().subscribe(result => {
      if (result) {
        this.dashboardService.deleteWidget(this.data.id).subscribe(onNext => {
          this.utilityService.popupSuccess('Widget successfully deleted.');
          this.dialogRef.close(AppConstants.DIALOG_RESULT.DELETE);
          this.dashboardService.refreshDashboard(this.data.id);
        });
      }
    });
  }

  save() {
    // Serialise configuration.
    const widget: DashboardWidgetDto = {
      id: this.data.id,
      type: AppConstants.DASHBOARD.WIDGETS.SENSOR_VALUE,
      gridCols: this.form.value['gridCols'],
      gridRows: this.form.value['gridRows'],
      gridX: this.form.value['gridX'],
      gridY: this.form.value['gridY'],
      updateEvery: this.form.value['updateEvery'],
      configuration:
        new WidgetSensorValueConf(
          this.form.value['title'],
          this.form.value['icon'],
          this.form.value['hardwareId'],
          this.form.value['measurement'],
          this.formatterService.colorToRgbaString(this.form.value['bgColor']),
          this.formatterService.colorToRgbaString(this.form.value['fgColor']),
        ).serialise(),
      dashboard: this.data.dashboard
    }

    // Save widget.
    this.dashboardService.saveWidget(widget).subscribe(
      onNext => {
        this.dialogRef.close(AppConstants.DIALOG_RESULT.SAVE);
        this.utilityService.popupSuccess("Widget saved successfully.");
        this.dashboardService.refreshDashboard(widget.id!);
      }
    )
  }

  close() {
    this.dialogRef.close(AppConstants.DIALOG_RESULT.CANCEL);
  }
}
