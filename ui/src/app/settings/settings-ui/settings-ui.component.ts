import {Component, OnInit} from "@angular/core";
import {FormBuilder, FormGroup} from "@angular/forms";
import {SettingsService} from "../settings.service";
import {BaseComponent} from "../../shared/components/base-component";
import * as _ from "lodash";
import {SettingDto} from "../dto/setting-dto";
import {UtilityService} from "../../shared/services/utility.service";

@Component({
  selector: "app-settings-ui",
  templateUrl: "./settings-ui.component.html"
})
export class SettingsUiComponent extends BaseComponent implements OnInit {
  form!: FormGroup;

  constructor(private fb: FormBuilder, private settingsService: SettingsService,
    private utilityService: UtilityService) {
    super();
  }

  ngOnInit(): void {
    // Define the form.
    this.form = this.fb.group({});

    // Fetch settings.
    // this.settingsService.findByNames([]).subscribe(onNext => {
    //   onNext.forEach(setting => {
    //     if (setting != null) {
    //       this.form.controls[setting.name].patchValue(setting.value);
    //     }
    //   });
    // });
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
