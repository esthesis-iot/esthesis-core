import {Component, Inject, OnInit, Optional} from "@angular/core";
import {SecurityBaseComponent} from "../../shared/components/security-base-component";
import {ActivatedRoute} from "@angular/router";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {AppConstants} from "../../app.constants";

@Component({
  selector: "app-dashboard-item-new",
  templateUrl: "./dashboard-item-new.component.html"
})
export class DashboardItemNewComponent extends SecurityBaseComponent implements OnInit {
  // Dashboard ID.
  dashboardId!: string;

  // List of dashboard items.
  itemsList: {
    name: string,
    description: string,
    icon: string,
    type: string,
    available: boolean;
  }[] = [];

  constructor(private readonly route: ActivatedRoute,
    @Optional() private readonly dialogRef: MatDialogRef<DashboardItemNewComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any) {
    super(AppConstants.SECURITY.CATEGORY.DASHBOARD, route.snapshot.paramMap.get("id"));
  }

  /**
   * Check for single-instance items in the dashboard that should not be added more than once.
   * @param itemType The type of the item to be added.
   */
  private itemAvailable(itemType: string): boolean {
    if (AppConstants.DASHBOARD.SINGLE_INSTANCE_ITEMS.indexOf(itemType) === -1) {
      return true;
    } else {
      return this.data.existingItemTypes.indexOf(itemType) === -1;
    }
  }

  ngOnInit(): void {
    this.itemsList = [
      {
        name: "About", description: "Displays information about the esthesis CORE installation, " +
          "similar to the About page.", icon: "about.png",
        type: this.appConstants.DASHBOARD.ITEM.TYPE.ABOUT,
        available: this.itemAvailable(this.appConstants.DASHBOARD.ITEM.TYPE.ABOUT)
      },
      {
        name: "Audit", description: "Displays latest audit entries.", icon: "about.png",
        type: this.appConstants.DASHBOARD.ITEM.TYPE.AUDIT,
        available: this.itemAvailable(this.appConstants.DASHBOARD.ITEM.TYPE.AUDIT)
      },
      {
        name: "Campaigns", description: "Displays campaign statistics.", icon: "about.png"
        , type: this.appConstants.DASHBOARD.ITEM.TYPE.CAMPAIGNS,
        available: this.itemAvailable(this.appConstants.DASHBOARD.ITEM.TYPE.CAMPAIGNS)
      },
      {
        name: "Chart", description: "Display device measurements on a chart.", icon: "about.png"
        , type: this.appConstants.DASHBOARD.ITEM.TYPE.CHART,
        available: this.itemAvailable(this.appConstants.DASHBOARD.ITEM.TYPE.CHART)
      },
      {
        name: "Date/Time", description: "Displays date and time.", icon: "about.png"
        , type: this.appConstants.DASHBOARD.ITEM.TYPE.DATETIME,
        available: this.itemAvailable(this.appConstants.DASHBOARD.ITEM.TYPE.DATETIME)
      },
      {
        name: "Icon", description: "Displays a device sensor with an icon that changes based on sensor value.",
        icon: "about.png", type: this.appConstants.DASHBOARD.ITEM.TYPE.SENSOR_ICON,
        available: this.itemAvailable(this.appConstants.DASHBOARD.ITEM.TYPE.SENSOR_ICON)
      },
      {
        name: "Image", description: "Displays an image.", icon: "about.png",
        type: this.appConstants.DASHBOARD.ITEM.TYPE.IMAGE,
        available: this.itemAvailable(this.appConstants.DASHBOARD.ITEM.TYPE.IMAGE)
      },
      {
        name: "Last seen", description: "Displays statistics on when devices were last seen.",
        icon: "about.png", type: this.appConstants.DASHBOARD.ITEM.TYPE.DEVICES_LAST_SEEN,
        available: this.itemAvailable(this.appConstants.DASHBOARD.ITEM.TYPE.DEVICES_LAST_SEEN)
      },
      {
        name: "Latest", description: "Displays a list of devices recently registered.",
        icon: "about.png", type: this.appConstants.DASHBOARD.ITEM.TYPE.DEVICES_LATEST,
        available: this.itemAvailable(this.appConstants.DASHBOARD.ITEM.TYPE.DEVICES_LATEST)
      },
      {
        name: "Map", description: "Displays a map with device coordinates.", icon: "about.png",
        type: this.appConstants.DASHBOARD.ITEM.TYPE.DEVICE_MAP,
        available: this.itemAvailable(this.appConstants.DASHBOARD.ITEM.TYPE.DEVICE_MAP)
      },
      {
        name: "Notes", description: "Displays user notes.", icon: "about.png",
        type: this.appConstants.DASHBOARD.ITEM.TYPE.NOTES,
        available: this.itemAvailable(this.appConstants.DASHBOARD.ITEM.TYPE.NOTES)
      },
      {
        name: "Status", description: "Displays statistics on devices status.", icon: "about.png",
        type: this.appConstants.DASHBOARD.ITEM.TYPE.DEVICES_STATUS,
        available: this.itemAvailable(this.appConstants.DASHBOARD.ITEM.TYPE.DEVICES_STATUS)
      },
      {
        name: "Security", description: "Displays statistics on security.", icon: "about.png",
        type: this.appConstants.DASHBOARD.ITEM.TYPE.SECURITY_STATS,
        available: this.itemAvailable(this.appConstants.DASHBOARD.ITEM.TYPE.SECURITY_STATS)
      },
      {
        name: "Sensor", description: "Displays a device sensor.", icon: "about.png",
        type: this.appConstants.DASHBOARD.ITEM.TYPE.SENSOR,
        available: this.itemAvailable(this.appConstants.DASHBOARD.ITEM.TYPE.SENSOR)
      },
      {
        name: "Title", description: "Displays a text title.", icon: "about.png",
        type: this.appConstants.DASHBOARD.ITEM.TYPE.TITLE,
        available: this.itemAvailable(this.appConstants.DASHBOARD.ITEM.TYPE.TITLE)
      }
    ];

    // Sort items list by name.
    this.itemsList.sort((a, b) => a.name.localeCompare(b.name));

    // Get the ID of the currently displayed dashboard.
    this.dashboardId = this.route.snapshot.paramMap.get("id")!;
  }

  selectItem(w: { name: string; description: string; icon: string }) {
    this.dialogRef.close(w);
  }

  close() {
    this.dialogRef.close();
  }
}
