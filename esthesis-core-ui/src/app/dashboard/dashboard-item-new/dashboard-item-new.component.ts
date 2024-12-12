import {Component, OnInit, Optional} from "@angular/core";
import {SecurityBaseComponent} from "../../shared/components/security-base-component";
import {FormBuilder} from "@angular/forms";
import {DashboardService} from "../dashboard.service";
import {ActivatedRoute, Router} from "@angular/router";
import {UtilityService} from "../../shared/services/utility.service";
import {MatDialog, MatDialogRef} from "@angular/material/dialog";
import {QFormValidationEEService} from "../../shared/services/form-validation.service";
import {AppConstants} from "../../app.constants";

@Component({
  selector: "app-dashboard-item-new",
  templateUrl: "./dashboard-item-new.component.html"
})
export class DashboardItemNewComponent extends SecurityBaseComponent implements OnInit {
  dashboardId!: string;  // Dashboard ID.
  itemsList: {
    name: string,
    description: string,
    icon: string,
    type: string
  }[] = [
    {
      name: "About", description: "Displays information about the esthesis CORE installation, " +
        "similar to the About page.", icon: "about.png",
      type: this.appConstants.DASHBOARD.ITEM.TYPE.ABOUT
    },
    {
      name: "Audit", description: "Displays latest audit entries.", icon: "about.png",
      type: this.appConstants.DASHBOARD.ITEM.TYPE.AUDIT
    },
    {
      name: "Campaigns", description: "Displays campaign statistics.", icon: "about.png"
      , type: this.appConstants.DASHBOARD.ITEM.TYPE.CAMPAIGNS
    },
    {
      name: "Map", description: "Displays a map with device coordinates.", icon: "about.png",
      type: this.appConstants.DASHBOARD.ITEM.TYPE.DEVICE_MAP
    },
    {
      name: "Last seen", description: "Displays statistics on when devices were last seen.",
      icon: "about.png", type: this.appConstants.DASHBOARD.ITEM.TYPE.DEVICES_LAST_SEEN
    },
    {
      name: "Latest", description: "Displays a list of devices recently registered.",
      icon: "about.png", type: this.appConstants.DASHBOARD.ITEM.TYPE.DEVICES_LATEST
    },
    {
      name: "Status", description: "Displays statistics on devices status.", icon: "about.png",
      type: this.appConstants.DASHBOARD.ITEM.TYPE.DEVICES_STATUS
    },
    {
      name: "Notes", description: "Displays user notes.", icon: "about.png",
      type: this.appConstants.DASHBOARD.ITEM.TYPE.NOTES
    },
    {
      name: "Security", description: "Displays statistics on security.", icon: "about.png",
      type: this.appConstants.DASHBOARD.ITEM.TYPE.SECURITY_STATS
    },
    {
      name: "Sensor", description: "Displays a device sensor.", icon: "about.png",
      type: this.appConstants.DASHBOARD.ITEM.TYPE.SENSOR
    },
    {
      name: "Icon", description: "Displays a device sensor with an icon that changes based on sensor value.",
      icon: "about.png", type: this.appConstants.DASHBOARD.ITEM.TYPE.SENSOR_ICON
    },
    {
      name: "Title", description: "Displays a text title.", icon: "about.png",
      type: this.appConstants.DASHBOARD.ITEM.TYPE.TITLE
    },
  ];

  constructor(private fb: FormBuilder, private dashboardService: DashboardService,
    private route: ActivatedRoute, private router: Router,
    private utilityService: UtilityService, private dialog: MatDialog,
    @Optional() private dialogRef: MatDialogRef<DashboardItemNewComponent>,
    private qFormValidation: QFormValidationEEService) {
    super(AppConstants.SECURITY.CATEGORY.DASHBOARD, route.snapshot.paramMap.get("id"));
  }

  ngOnInit(): void {
    // Sort items list by name.
    this.itemsList.sort((a, b) => a.name.localeCompare(b.name));

    // Get the ID of the currently displayed dashboard.
    this.dashboardId = this.route.snapshot.paramMap.get("id")!;
  }

  selectItem(w: { name: string; description: string; icon: string }) {
    this.dialogRef.close(w);
  }
}
