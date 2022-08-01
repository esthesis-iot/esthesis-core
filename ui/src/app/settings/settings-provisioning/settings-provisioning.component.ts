import {Component, OnInit} from "@angular/core";
import {FormBuilder, FormGroup} from "@angular/forms";
import {SettingsService} from "../settings.service";
import {AppSettings} from "../../app.settings";
import * as _ from "lodash";
import {BaseComponent} from "../../shared/component/base-component";
import {UtilityService} from "../../shared/service/utility.service";
import {RegistryEntryDto} from "../../dto/registry-entry-dto";

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
      provisioningUrl: ["", []]
    });


    // Fetch settings.
    this.settingsService.findByNames(
      AppSettings.SETTING.PROVISIONING.PROVISIONING_URL
    ).subscribe(onNext => {
      onNext.forEach(registryEntryDTO => {
        this.form.controls[registryEntryDTO.name].patchValue(registryEntryDTO.value);
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
