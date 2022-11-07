import {Component, OnInit} from "@angular/core";
import {FormBuilder, FormGroup} from "@angular/forms";
import {SettingsService} from "../settings.service";
import * as _ from "lodash";
import {BaseComponent} from "../../shared/component/base-component";
import {UtilityService} from "../../shared/service/utility.service";
import {SettingDto} from "../../dto/setting-dto";

@Component({
  selector: "app-settings-provisioning",
  templateUrl: "./settings-provisioning.component.html",
  styleUrls: []
})
export class SettingsProvisioningComponent extends BaseComponent implements OnInit {

  form!: FormGroup;

  constructor(private fb: FormBuilder, private settingsService: SettingsService,
    private utilityService: UtilityService) {
    super();
  }

  ngOnInit() {
    // Define the form.
    this.form = this.fb.group({
      DEVICE_PROVISIONING_URL: ["", []]
    });


    // Fetch settings.
    this.settingsService.findByNames("DEVICE_PROVISIONING_URL").subscribe(onNext => {
      onNext.forEach(setting => {
        this.form.controls[setting.name].patchValue(setting.value);
      });
    });
  }

  save() {
    this.settingsService.save(
      _.map(Object.keys(this.form.controls), (fc) => {
        return new SettingDto(fc, this.form.get(fc)!.value);
      })).subscribe(onNext => {
      this.utilityService.popupSuccess("Settings saved successfully.");
    });
  }

}
