import {Component, Inject, OnInit} from '@angular/core';
import {BaseComponent} from "../../../shared/component/base-component";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {DeviceDto} from "../../../dto/device-dto";
import {AppExtendedConstants} from "../../../app.extended-constants";
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from "@angular/material/dialog";
import {DashboardService} from "../../dashboard.service";
import {UtilityService} from "../../../shared/service/utility.service";
import {DevicesService} from "../../../devices/devices.service";
import {FormatterService} from "../../../shared/service/formatter.service";
import {
  OkCancelModalComponent
} from "../../../shared/component/display/ok-cancel-modal/ok-cancel-modal.component";
import {AppConstants} from "../../../app.constants";
import {DashboardWidgetDto} from "../../../dto/dashboard-widget-dto";
import {WidgetMapConf} from "./widget-map-conf";
import {TagService} from "../../../tags/tag.service";
import {TagDto} from "../../../dto/tag-dto";
import {Color} from "@angular-material-components/color-picker";
import {debounceTime, distinctUntilChanged} from 'rxjs/operators';

@Component({
  selector: 'app-widget-map-setup',
  templateUrl: './widget-map-setup.component.html',
  styleUrls: ['./widget-map-setup.component.scss']
})
export class WidgetMapSetupComponent extends BaseComponent implements OnInit {
  form!: FormGroup;
  devices!: DeviceDto[];
  extendedConstants = AppExtendedConstants;
  // A helper auto-complete container for devices matching the user's search input.
  searchDevices?: DeviceDto[];
  // The list of currently available tags.
  availableTags: TagDto[] | undefined;

  constructor(private fb: FormBuilder, private dialog: MatDialog,
              public dialogRef: MatDialogRef<WidgetMapSetupComponent>,
              @Inject(MAT_DIALOG_DATA) public data: any, private dashboardService: DashboardService,
              private utilityService: UtilityService, private devicesService: DevicesService,
              private formatterService: FormatterService, private tagService: TagService) {
    super();
  }

  ngOnInit(): void {
    // Setup the form.
    this.form = this.fb.group({
      gridCols: [1, [Validators.required]],
      gridRows: [1, [Validators.required]],
      gridX: [0, [Validators.required]],
      gridY: [0, [Validators.required]],
      title: [],
      bgColor: [new Color(64, 199, 247, 1)],
      fgColor: [new Color(255, 255, 255, 1)],
      updateEvery: [60, [Validators.required]],
      hardwareIds: ['', []],
      tags: ['', []],
    });

    // If editing an existing widget, fetch widget configuration.
    if (this.data.id !== 0) {
      this.dashboardService.getWidget(this.data.id).subscribe(onNext => {
        this.form.patchValue(onNext);
        const conf: WidgetMapConf = JSON.parse(onNext.configuration);
        this.form.patchValue(conf);
        this.form.patchValue({
          fgColor: this.formatterService.rgbaStringToColor(conf.fgColor),
          bgColor: this.formatterService.rgbaStringToColor(conf.bgColor)
        });
      })
    }

    // Monitor for changes in search by hardware Id input.
    this.form.get("hardwareIds")!.valueChanges.pipe(
      debounceTime(500),
      distinctUntilChanged()
    ).subscribe(onNext => {
      if (onNext && onNext.trim() !== "") {
        // Find the last hardware ID in the comma-separated list.
        let hardwareIdToSearch: string;
        if (onNext.indexOf(",") > -1) {
          hardwareIdToSearch = onNext.substring(onNext.lastIndexOf(",") + 1);
        } else {
          hardwareIdToSearch = onNext;
        }
        console.log("ID=" + hardwareIdToSearch);
        this.devicesService.findDeviceByPartialHardwareId(hardwareIdToSearch).subscribe(
          onNext => {
            if (onNext && onNext.length > 0) {
              this.searchDevices = onNext;
            } else {
              this.searchDevices = [];
            }
          }
        );
      } else {
        this.searchDevices = [];
      }
    });

    // Get available tags.
    this.tagService.find("sort=name,asc").subscribe(onNext => {
      this.availableTags = onNext.content;
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
      type: AppConstants.DASHBOARD.WIDGETS.MAP,
      gridCols: this.form.value['gridCols'],
      gridRows: this.form.value['gridRows'],
      gridX: this.form.value['gridX'],
      gridY: this.form.value['gridY'],
      updateEvery: this.form.value['updateEvery'],
      configuration:
        new WidgetMapConf(
          this.form.value['title'],
          this.formatterService.colorToRgbaString(this.form.value['bgColor']),
          this.formatterService.colorToRgbaString(this.form.value['fgColor']),
          this.form.value['hardwareIds'],
          this.form.value['tags']
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

  getCurrentDevices() {
    let currentValue = this.form.value['hardwareIds'];
    if (currentValue.indexOf(",") > -1) {
      return currentValue.substring(0, currentValue.lastIndexOf(",") + 1);
    } else {
      return "";
    }
  }
}
