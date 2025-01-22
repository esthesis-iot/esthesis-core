import {Component, Input} from "@angular/core";
import {FormGroup} from "@angular/forms";

@Component({
  selector: 'app-dashboard-item-common-config',
  templateUrl: './dashboard-item-common-config.component.html'
})
export class DashboardItemCommonConfigComponent {
  @Input() parentForm!: FormGroup; // The parent form passed in.
}
