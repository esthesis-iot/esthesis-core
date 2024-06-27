import {Component, OnInit} from "@angular/core";
import {FormBuilder, FormGroup} from "@angular/forms";
import {SettingsService} from "../settings.service";
import * as _ from "lodash-es";
import {SettingDto} from "../dto/setting-dto";
import {AppConstants} from "../../app.constants";
import {UtilityService} from "../../shared/services/utility.service";
import {SecurityBaseComponent} from "../../shared/components/security-base-component";

@Component({
  selector: "app-settings-provisioning",
  templateUrl: "./settings-provisioning.component.html",
  styleUrls: []
})
export class SettingsProvisioningComponent extends SecurityBaseComponent implements OnInit {

  form!: FormGroup;

  constructor(private fb: FormBuilder, private settingsService: SettingsService,
    private utilityService: UtilityService) {
    super(AppConstants.SECURITY.CATEGORY.SETTINGS);
  }

  ngOnInit() {
    // Define the form.
    this.form = this.fb.group({
      DEVICE_PROVISIONING_URL: [],
      DEVICE_PROVISIONING_SECURE: [],
      DEVICE_PROVISIONING_CACHE_TIME: [],
      DEVICE_PROVISIONING_SEMVER: [],
    });


    // Fetch settings.
    this.settingsService.findByNames([
      AppConstants.NAMED_SETTING.DEVICE_PROVISIONING_URL,
      AppConstants.NAMED_SETTING.DEVICE_PROVISIONING_SECURE,
      AppConstants.NAMED_SETTING.DEVICE_PROVISIONING_CACHE_TIME,
      AppConstants.NAMED_SETTING.DEVICE_PROVISIONING_SEMVER,
    ]).subscribe(onNext => {
      onNext.forEach(setting => {
        this.form.controls[setting.name].patchValue(setting.value);
      });
    });
  }

  save() {
    this.settingsService.save(
      _.map(Object.keys(this.form.controls), (fc) => {
        return new SettingDto(fc, this.form.get(fc)!.value);
      })).subscribe({
      next: () => {
        this.utilityService.popupSuccess("Settings saved successfully.");
      }
    });
  }

}
