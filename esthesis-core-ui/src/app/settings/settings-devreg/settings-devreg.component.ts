import {Component, OnInit} from "@angular/core";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {SettingsService} from "../settings.service";
import * as _ from "lodash-es";
import {SettingDto} from "../dto/setting-dto";
import {AppConstants} from "../../app.constants";
import {UtilityService} from "../../shared/services/utility.service";
import {SecurityBaseComponent} from "../../shared/components/security-base-component";

@Component({
  selector: "app-settings-devreg",
  templateUrl: "./settings-devreg.component.html"
})
export class SettingsDevregComponent extends SecurityBaseComponent implements OnInit {
  form!: FormGroup;

  constructor(private readonly fb: FormBuilder, private readonly settingsService: SettingsService,
    private readonly utilityService: UtilityService,) {
    super(AppConstants.SECURITY.CATEGORY.SETTINGS);
  }

  ngOnInit() {
    // Define the form.
    this.form = this.fb.group({
      DEVICE_REGISTRATION_MODE: [null, [Validators.required]],
      DEVICE_REGISTRATION_SECRET: [null, []],
      DEVICE_PUSHED_TAGS: [null, []]
    });

    // Fetch settings.
    this.settingsService.findByNames([
      AppConstants.NAMED_SETTING.DEVICE_REGISTRATION_MODE,
      AppConstants.NAMED_SETTING.DEVICE_REGISTRATION_SECRET,
      AppConstants.NAMED_SETTING.DEVICE_PUSHED_TAGS
    ]).subscribe(onNext => {
      onNext.forEach(setting => {
        if (setting != null) {
          this.form.controls[setting.name].patchValue(setting.value);
        }
      });
    });
  }

  save() {
    this.settingsService.save(
      _.map(Object.keys(this.form.controls), (fc) => {
        return new SettingDto(fc, this.form.get(fc)!.value);
      })).subscribe(() => {
      this.utilityService.popupSuccess("Settings saved successfully.");
    });
  }

}
