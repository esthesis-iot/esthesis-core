import {Component, OnInit} from "@angular/core";
import {FormBuilder, FormGroup} from "@angular/forms";
import {SettingsService} from "../settings.service";
import * as _ from "lodash";
import {SettingDto} from "../dto/setting-dto";
import {AppConstants} from "../../app.constants";
import {BaseComponent} from "../../shared/components/base-component";
import {UtilityService} from "../../shared/services/utility.service";

@Component({
  selector: "app-settings-messaging",
  templateUrl: "./settings-messaging.component.html"
})
export class SettingsMessagingComponent extends BaseComponent implements OnInit {

  form!: FormGroup;

  constructor(private fb: FormBuilder, private settingsService: SettingsService,
    private utilityService: UtilityService) {
    super();
  }

  ngOnInit() {
    // Define the form.
    this.form = this.fb.group({
      KAFKA_TOPIC_COMMAND_REQUEST: [null, []]
    });


    // Fetch settings.
    this.settingsService.findByNames([AppConstants.NAMED_SETTING.KAFKA_TOPIC_COMMAND_REQUEST]).subscribe(onNext => {
      onNext.forEach(setting => {
        this.form.controls[setting.name].patchValue(setting.value);
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
