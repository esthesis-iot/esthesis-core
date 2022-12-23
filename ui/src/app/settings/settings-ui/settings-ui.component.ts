import {Component, OnInit} from "@angular/core";
import {FormBuilder, FormGroup} from "@angular/forms";
import {SettingsService} from "../settings.service";
import {UtilityService} from "../../shared/service/utility.service";
import {BaseComponent} from "../../shared/component/base-component";
import * as _ from "lodash";
import {SettingDto} from "../dto/setting-dto";

@Component({
  selector: "app-settings-ui",
  templateUrl: "./settings-ui.component.html",
  styleUrls: ["./settings-ui.component.scss"]
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
      })).subscribe(onNext => {
      this.utilityService.popupSuccess("Settings saved successfully.");
    });
  }

}
