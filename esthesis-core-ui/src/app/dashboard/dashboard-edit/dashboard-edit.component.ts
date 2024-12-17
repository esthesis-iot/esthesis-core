import {Component, OnInit} from "@angular/core";
import {SecurityBaseComponent} from "../../shared/components/security-base-component";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {ActivatedRoute, Router} from "@angular/router";
import {UtilityService} from "../../shared/services/utility.service";
import {MatDialog} from "@angular/material/dialog";
import {QFormValidationEEService} from "../../shared/services/form-validation.service";
import {AppConstants} from "../../app.constants";
import {
  OkCancelModalComponent
} from "../../shared/components/ok-cancel-modal/ok-cancel-modal.component";
import {DashboardService} from "../dashboard.service";
import {DashboardItemDto} from "../dto/view-edit/dashboard-item-dto";
import {DashboardItemNewComponent} from "../dashboard-item-new/dashboard-item-new.component";
import {CdkDragDrop, moveItemInArray} from "@angular/cdk/drag-drop";
import {
  DashboardItemSensorEditComponent
} from "../items/dashboard-item-sensor-edit/dashboard-item-sensor-edit.component";
import {
  DashboardItemAboutEditComponent
} from "../items/dashboard-item-about-edit/dashboard-item-about-edit.component";
import {
  DashboardItemAuditEditComponent
} from "../items/dashboard-item-audit-edit/dashboard-item-audit-edit.component";
import {
  DashboardItemCampaignsEditComponent
} from "../items/dashboard-item-campaigns-edit/dashboard-item-campaigns-edit.component";
import {
  DashboardItemDatetimeEditComponent
} from "../items/dashboard-item-datetime-edit/dashboard-item-datetime-edit.component";
import {
  DashboardItemDeviceMapEditComponent
} from "../items/dashboard-item-device-map-edit/dashboard-item-device-map-edit.component";
import {
  DashboardItemDevicesLatestEditComponent
} from "../items/dashboard-item-devices-latest-edit/dashboard-item-devices-latest-edit.component";
import {
  DashboardItemDevicesLastSeenEditComponent
} from "../items/dashboard-item-devices-last-seen-edit/dashboard-item-devices-last-seen-edit.component";
import {
  DashboardItemDevicesStatusEditComponent
} from "../items/dashboard-item-devices-status-edit/dashboard-item-devices-status-edit.component";
import {
  DashboardItemImageEditComponent
} from "../items/dashboard-item-image-edit/dashboard-item-image-edit.component";
import {
  DashboardItemNotesEditComponent
} from "../items/dashboard-item-notes-edit/dashboard-item-notes-edit.component";
import {
  DashboardItemSecurityStatsEditComponent
} from "../items/dashboard-item-security-stats-edit/dashboard-item-security-stats-edit.component";
import {
  DashboardItemSensorIconEditComponent
} from "../items/dashboard-item-sensor-icon-edit/dashboard-item-sensor-icon-edit.component";
import {
  DashboardItemTitleComponent
} from "../items/dashboard-item-title/dashboard-item-title.component";
import {DashboardDto} from "../dto/view-edit/dashboard-dto";
import {v4} from "uuid";

@Component({
  selector: "app-dashboard-edit",
  templateUrl: "./dashboard-edit.component.html"
})
export class DashboardEditComponent extends SecurityBaseComponent implements OnInit {
  form!: FormGroup;
  id!: string;  // Dashboard ID.
  dashboardItems: DashboardItemDto[] = [];

  constructor(private fb: FormBuilder, private dashboardService: DashboardService,
    private route: ActivatedRoute, private router: Router,
    private utilityService: UtilityService, private dialog: MatDialog,
    private qFormValidation: QFormValidationEEService) {
    super(AppConstants.SECURITY.CATEGORY.DASHBOARD, route.snapshot.paramMap.get("id"));
  }

  ngOnInit() {
    this.id = this.route.snapshot.paramMap.get("id")!;
    // Set up the form.
    this.form = this.fb.group({
      id: [],
      name: [null, [Validators.minLength(3), Validators.maxLength(255), Validators.required]],
      description: [null, [Validators.maxLength(2048)]],
      updateInterval: [5, [Validators.min(1), Validators.max(300)]],
      displayLastUpdate: [true],
      shared: [false],
      home: [false],
      items: []
    });

    // Fill-in the form with data if editing an existing item.
    if (this.id !== this.appConstants.NEW_RECORD_ID) {
      this.dashboardService.findById(this.id).subscribe({
        next: (dashboard) => {
          this.form.patchValue(dashboard);
        }, error: (err) => {
          this.utilityService.popupErrorWithTraceId(
            "There was an error trying to retrieve this dashboard.", err);
        }
      });
      // Get dashboard items.
      this.dashboardService.findById(this.id).subscribe({
        next: (response) => {
          this.dashboardItems = response.items;
          this.configureItem(this.dashboardItems[1]);
        }, error: (error) => {
          this.utilityService.popupErrorWithTraceId("Could not fetch dashboard items.", error);
        }
      });
    }

    // Disable the form if the user does not have permission to edit.
    this.isFormDisabled().subscribe(disabled => disabled && this.form.disable());

  }

  save() {
    // Set dashboard items.
    this.form.get("items")!.setValue(this.dashboardItems);
    this.dashboardService.save(this.form.getRawValue() as DashboardDto).subscribe({
      next: () => {
        if (this.id === this.appConstants.NEW_RECORD_ID) {
          this.utilityService.popupSuccess("Dashboard was successfully created.");
        } else {
          this.utilityService.popupSuccess("Dashboard was successfully edited.");
        }
        this.router.navigate(["dashboard"]);
      }, error: (err) => {
        console.log(err);
        if (err.status === 400) {
          const validationErrors = err.error;
          if (validationErrors) {
            this.qFormValidation.applyValidationErrors(this.form, validationErrors.violations);
          }
        } else if (err.status === 401) {
          this.utilityService.popupErrorWithTraceId(err.error.errorMessage, err);
        } else {
          this.utilityService.popupError("There was an error trying to save this dashboard.");
        }
      }
    });
  }

  delete() {
    this.dialog.open(OkCancelModalComponent, {
      data: {
        title: "Delete Dashboard",
        question: "Do you really want to delete this Dashboard?",
        buttons: {
          ok: true, cancel: true, reload: false
        }
      }
    }).afterClosed().subscribe(result => {
      if (result) {
        this.dashboardService.delete(this.id).subscribe({
          next: () => {
            this.utilityService.popupSuccess("Dashboard successfully deleted.");
            this.router.navigate(["dashboard"]);
          }
        });
      }
    });
  }

  addItem() {
    this.dialog.open(DashboardItemNewComponent, {data: {existingItemTypes: this.dashboardItems.map(di => di.type)}})
      .afterClosed().subscribe(result => {
        if (result) {
          // Find default configuration for dashboard item.
          let defaultConfiguration = "{}";
          switch (result.type) {
            case AppConstants.DASHBOARD.ITEM.TYPE.AUDIT:
              defaultConfiguration = JSON.stringify(this.appConstants.DASHBOARD.ITEM.DEFAULTS.AUDIT);
              break;
            case AppConstants.DASHBOARD.ITEM.TYPE.ABOUT:
              defaultConfiguration = JSON.stringify(this.appConstants.DASHBOARD.ITEM.DEFAULTS.ABOUT);
              break;
            case AppConstants.DASHBOARD.ITEM.TYPE.SENSOR:
              defaultConfiguration = JSON.stringify(this.appConstants.DASHBOARD.ITEM.DEFAULTS.SENSOR);
              break;
          }

          // Add selected dashboard item.
          this.dashboardItems.push({
            id: v4(),
            type: result.type,
            columns: this.appConstants.DASHBOARD.ITEM.COLUMNS[result.type.split(".").pop() as keyof typeof this.appConstants.DASHBOARD.ITEM.COLUMNS],
            index: this.dashboardItems.length,
            title: result.name,
            configuration: defaultConfiguration
          });
        }
      });
  }

  dropItem(event: CdkDragDrop<string[]>) {
    moveItemInArray(this.dashboardItems, event.previousIndex, event.currentIndex);
    for (let i = 0; i < this.dashboardItems.length; i++) {
      this.dashboardItems[i].index = i;
    }
  }

  removeItem(di: DashboardItemDto) {
    this.dialog.open(OkCancelModalComponent, {
      data: {
        title: "Remove item",
        question: "Do you really want to remove this item from your dashboard?",
        buttons: {
          ok: true, cancel: true, reload: false
        }
      }
    }).afterClosed().subscribe(result => {
      if (result) {
        this.dashboardItems.splice(di.index, 1);
      }
    });
  }

  configureItem(di: DashboardItemDto) {
    let diEditComponent: any;

    switch (di.type) {
      case AppConstants.DASHBOARD.ITEM.TYPE.ABOUT:
        diEditComponent = DashboardItemAboutEditComponent;
        break;
      case AppConstants.DASHBOARD.ITEM.TYPE.AUDIT:
        diEditComponent = DashboardItemAuditEditComponent;
        break;
      case AppConstants.DASHBOARD.ITEM.TYPE.CAMPAIGNS:
        diEditComponent = DashboardItemCampaignsEditComponent;
        break;
      case AppConstants.DASHBOARD.ITEM.TYPE.DATETIME:
        diEditComponent = DashboardItemDatetimeEditComponent;
        break;
      case AppConstants.DASHBOARD.ITEM.TYPE.DEVICE_MAP:
        diEditComponent = DashboardItemDeviceMapEditComponent;
        break;
      case AppConstants.DASHBOARD.ITEM.TYPE.DEVICES_LAST_SEEN:
        diEditComponent = DashboardItemDevicesLastSeenEditComponent;
        break;
      case AppConstants.DASHBOARD.ITEM.TYPE.DEVICES_LATEST:
        diEditComponent = DashboardItemDevicesLatestEditComponent;
        break;
      case AppConstants.DASHBOARD.ITEM.TYPE.DEVICES_STATUS:
        diEditComponent = DashboardItemDevicesStatusEditComponent;
        break;
      case AppConstants.DASHBOARD.ITEM.TYPE.IMAGE:
        diEditComponent = DashboardItemImageEditComponent;
        break;
      case AppConstants.DASHBOARD.ITEM.TYPE.NOTES:
        diEditComponent = DashboardItemNotesEditComponent;
        break;
      case AppConstants.DASHBOARD.ITEM.TYPE.SECURITY_STATS:
        diEditComponent = DashboardItemSecurityStatsEditComponent;
        break;
      case AppConstants.DASHBOARD.ITEM.TYPE.SENSOR:
        diEditComponent = DashboardItemSensorEditComponent;
        break;
      case AppConstants.DASHBOARD.ITEM.TYPE.SENSOR_ICON:
        diEditComponent = DashboardItemSensorIconEditComponent;
        break;
      case AppConstants.DASHBOARD.ITEM.TYPE.TITLE:
        diEditComponent = DashboardItemTitleComponent;
        break;
    }
    this.dialog.open(diEditComponent, {data: di, width: "60%"}).afterClosed().subscribe(result => {
      if (result) {
        this.dashboardItems[result.index] = result;
      }
    });
  }
}
