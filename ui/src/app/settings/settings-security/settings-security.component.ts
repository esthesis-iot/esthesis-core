import {Component, OnInit} from "@angular/core";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {SettingsService} from "../settings.service";
import {CertificatesService} from "../../certificates/certificates.service";
import {CertificateDto} from "../../dto/certificate-dto";
import * as _ from "lodash";
import {BaseComponent} from "../../shared/component/base-component";
import {UtilityService} from "../../shared/service/utility.service";
import {RegistryEntryDto} from "../../dto/registry-entry-dto";

@Component({
  selector: "app-settings-security",
  templateUrl: "./settings-security.component.html",
  styleUrls: []
})
export class SettingsSecurityComponent extends BaseComponent implements OnInit {
  form!: FormGroup;
  certificates: CertificateDto[] | undefined;

  constructor(private fb: FormBuilder, private settingsService: SettingsService,
    private certificatesService: CertificatesService, private utilityService: UtilityService) {
    super();
  }

  ngOnInit() {
    // Define the form.
    this.form = this.fb.group({
      PLATFORM_CERTIFICATE: ["", [Validators.required]],
    });

    // Fetch settings.
    this.settingsService.findByNames("PLATFORM_CERTIFICATE").subscribe(onNext => {
      onNext.forEach(registryEntryDTO => {
        if (registryEntryDTO != null) {
          this.form.controls[registryEntryDTO.name].patchValue(registryEntryDTO.value);
        }
      });
    });

    // Fetch lookup values.
    this.certificatesService.find("sort=cn,asc").subscribe(onNext => {
      if (onNext.content && onNext.content.length > 0) {
        onNext.content.unshift(new CertificateDto(null!, ""));
        this.certificates = onNext.content;
      }
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
