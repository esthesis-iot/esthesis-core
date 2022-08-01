import {Component, OnInit} from "@angular/core";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {SettingsService} from "../settings.service";
import {UtilityService} from "../../shared/service/utility.service";
import {BaseComponent} from "../../shared/component/base-component";
import {AppSettings} from "../../app.settings";
import * as _ from "lodash";
import {RegistryEntryDto} from "../../dto/registry-entry-dto";

@Component({
  selector: "app-settings-ui",
  templateUrl: "./settings-ui.component.html",
  styleUrls: ["./settings-ui.component.scss"]
})
export class SettingsUiComponent extends BaseComponent implements OnInit {
  form!: FormGroup;
  // Expose application constants.
  settings = AppSettings;

  constructor(private fb: FormBuilder, private settingsService: SettingsService,
    private utilityService: UtilityService) {
    super();
  }

  ngOnInit(): void {
    // Define the form.
    this.form = this.fb.group({
      pixabay_category: ["", [Validators.required]],
      pixabay_key: ["", [Validators.required]],
      pixabay_enabled: ["", [Validators.required]],
    });

    // Fetch settings.
    this.settingsService.findByNames(
      AppSettings.SETTING.UI.PIXABAY.CATEGORY,
      AppSettings.SETTING.UI.PIXABAY.KEY,
      AppSettings.SETTING.UI.PIXABAY.ENABLED,
    ).subscribe(onNext => {
      onNext.forEach(registryEntryDto => {
        this.form.controls[registryEntryDto.name].patchValue(registryEntryDto.value);
      });
    });
  }

  save() {
    this.settingsService.save(
      _.map(Object.keys(this.form.controls), (fc) => {
        return new RegistryEntryDto(fc, this.form.get(fc)!.value);
      })).subscribe(onNext => {
      this.utilityService.popupSuccess("Settings saved successfully.");
    });
  }

}
