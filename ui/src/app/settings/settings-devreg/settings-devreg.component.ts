import {Component, OnInit} from "@angular/core";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {SettingsService} from "../settings.service";
import * as _ from "lodash";
import {BaseComponent} from "../../shared/component/base-component";
import {UtilityService} from "../../shared/service/utility.service";
import {CasService} from "../../cas/cas.service";
import {CaDto} from "../../cas/dto/ca-dto";
import {SettingDto} from "../dto/setting-dto";
import {AppConstants} from "../../app.constants";

@Component({
  selector: "app-settings-devreg",
  templateUrl: "./settings-devreg.component.html",
  styleUrls: []
})
export class SettingsDevregComponent extends BaseComponent implements OnInit {
  form!: FormGroup;
  cas: CaDto[] | undefined;

  constructor(private fb: FormBuilder, private settingsService: SettingsService,
    private utilityService: UtilityService, private casService: CasService) {
    super();
  }

  ngOnInit() {
    // Define the form.
    // TODO to be fully dynamic, the name of the form control should be derived from the name
    //  of the respective setting. Create an empty form group and then add the form controls
    //  programmatically.
    this.form = this.fb.group({
      DEVICE_REGISTRATION_MODE: [null, [Validators.required]],
      DEVICE_ROOT_CA: [null, []],
      DEVICE_PUSHED_TAGS: [null, []]
    });

    // Fetch settings.
    this.settingsService.findByNames([
      AppConstants.NAMED_SETTING.DEVICE_REGISTRATION_MODE,
      AppConstants.NAMED_SETTING.DEVICE_ROOT_CA,
      AppConstants.NAMED_SETTING.DEVICE_PUSHED_TAGS
    ]).subscribe(onNext => {
      onNext.forEach(setting => {
        if (setting != null) {
          this.form.controls[setting.name].patchValue(setting.value);
        }
      });
    });

    // Fetch lookup values.
    this.casService.find("sort=cn,asc").subscribe(onNext => {
      if (onNext.content && onNext.content.length > 0) {
        onNext.content.unshift(new CaDto(null!, "", ""));
        this.cas = onNext.content;
      }
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
