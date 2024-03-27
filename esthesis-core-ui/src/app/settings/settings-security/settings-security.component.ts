import {Component, OnInit} from "@angular/core";
import {FormBuilder, FormGroup} from "@angular/forms";
import {SettingsService} from "../settings.service";
import {CertificateDto} from "../../certificates/dto/certificate-dto";
import * as _ from "lodash-es";
import {SettingDto} from "../dto/setting-dto";
import {AppConstants} from "../../app.constants";
import {UtilityService} from "../../shared/services/utility.service";
import {SecurityBaseComponent} from "../../shared/components/security-base-component";
import {CaDto} from "../../cas/dto/ca-dto";
import {CasService} from "../../cas/cas.service";

@Component({
  selector: "app-settings-security",
  templateUrl: "./settings-security.component.html",
  styleUrls: []
})
export class SettingsSecurityComponent extends SecurityBaseComponent implements OnInit {
  form!: FormGroup;
  certificates: CertificateDto[] | undefined;
  cas: CaDto[] | undefined;

  constructor(private fb: FormBuilder, private settingsService: SettingsService,
    private utilityService: UtilityService, private casService: CasService) {
    super(AppConstants.SECURITY.CATEGORY.SETTINGS);
  }

  ngOnInit() {
    // Define the form.
    // TODO to be fully dynamic, the name of the form control should be derived from the name
    //  of the respective setting. Create an empty form group and then add the form controls
    //  programmatically.
    this.form = this.fb.group({
      PLATFORM_CERTIFICATE: ["", []],
      DEVICE_ROOT_CA: ["", []]
    });

    // Fetch settings.
    this.settingsService.findByNames([
      AppConstants.NAMED_SETTING.DEVICE_ROOT_CA,
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
        onNext.content.unshift(new CaDto("", "", ""));
        this.cas = onNext.content;
      }
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
