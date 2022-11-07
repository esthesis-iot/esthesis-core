import {Component, OnInit} from "@angular/core";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {SettingsService} from "../settings.service";
import * as _ from "lodash";
import {BaseComponent} from "../../shared/component/base-component";
import {UtilityService} from "../../shared/service/utility.service";
import {CasService} from "../../cas/cas.service";
import {CaDto} from "../../dto/ca-dto";
import {SettingDto} from "../../dto/setting-dto";

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
    this.form = this.fb.group({
      DEVICE_REGISTRATION_MODE: ["", [Validators.required]],
      DEVICE_TAGS_ALGORITHM: ["", [Validators.required]],
      DEVICE_ROOT_CA: ["", []]
    });

    // Fetch settings.
    this.settingsService.findByNames(
      "DEVICE_REGISTRATION_MODE,DEVICE_TAGS_ALGORITHM,DEVICE_ROOT_CA"
    ).subscribe(onNext => {
      onNext.forEach(setting => {
        if (setting != null) {
          this.form.controls[setting.name].patchValue(setting.value);
        }
      });
    });

    // Fetch lookup values.
    this.casService.find("sort=cn,asc").subscribe(onNext => {
      if (onNext.content && onNext.content.length > 0) {
        onNext.content.unshift(new CaDto(null!, ""));
        this.cas = onNext.content;
      }
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
