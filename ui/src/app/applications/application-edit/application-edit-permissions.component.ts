import {Component, Input} from "@angular/core";

@Component({
  selector: "app-application-edit-permissions",
  templateUrl: "./application-edit-permissions.component.html",
  styleUrls: []
})
export class ApplicationEditPermissionsComponent {
  @Input() id!: string | null | undefined;

}
