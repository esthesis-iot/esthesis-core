import {Component, OnInit} from "@angular/core";
import {BaseComponent} from "../../shared/component/base-component";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {UtilityService} from "../../shared/service/utility.service";
import {Router} from "@angular/router";
import {debounceTime, distinctUntilChanged} from "rxjs/operators";
import {ProvisioningService} from "../../provisioning/provisioning.service";
import {CommandService} from "../command.service";
import {TagDto} from "../../dto/tag-dto";
import {TagService} from "../../tags/tag.service";
import {DeviceDto} from "../../dto/device-dto";
import {DevicesService} from "../../devices/devices.service";
import {AppConstants} from "../../app.constants";
import {ProvisioningDto} from "../../provisioning/dto/provisioning-dto";

@Component({
  selector: "app-command-create",
  templateUrl: "./command-create.component.html",
  styleUrls: ["./command-create.component.scss"]
})
export class CommandCreateComponent extends BaseComponent implements OnInit {
  searchDevicesForm!: FormGroup;
  commandForm!: FormGroup;
  // The list of currently active provisioning packages.
  provisioningPackages?: ProvisioningDto[];
  isSearching = false;
  availableTags: TagDto[] | undefined;
  searchDevices?: DeviceDto[];
  selectedHardwareIds: string[] = [];

  constructor(private formBuilder: FormBuilder, private commandService: CommandService,
    private utilityService: UtilityService, private router: Router, private tagService: TagService,
    private provisioningService: ProvisioningService, private deviceService: DevicesService) {
    super();
  }

  ngOnInit() {
    // Step 1 form.
    this.searchDevicesForm = this.formBuilder.group({
      hardwareId: [""],
      tags: [""],
    });

    // Step 2 form.
    this.commandForm = this.formBuilder.group({
      commandType: ["", [Validators.required]],
      executionType: ["", [Validators.required]],
      command: [""],
      arguments: [""],
      description: [""]
    });

    // Watch changes on the hardware / tags.
    this.searchDevicesForm.valueChanges.pipe(debounceTime(500), distinctUntilChanged()
    ).subscribe(onNext => {
      if (onNext && onNext.hardwareId) {
        this.commandService.findDevicesByHardwareId(onNext.hardwareId).subscribe(
          onNext => {
            if (onNext && onNext.length > 0) {
              this.searchDevices = onNext;
            } else {
              this.searchDevices = [];
            }
          }
        );
      } else {
        this.searchDevices = [];
      }
    });

    // Get available tags.
    this.tagService.find("sort=name,asc").subscribe(onNext => {
      this.availableTags = onNext.content;
    });

    // Get provisioning packages.
    // this.provisioningService.find("state=1&sort=name,packageVersion").subscribe(onNext => {
    //   this.provisioningPackages = onNext.content;
    // });
  }

  save() {
    this.commandService.execute(
      {
        ...{
          hardwareIds: this.selectedHardwareIds.join(","),
          tags: this.searchDevicesForm.value.tags ? this.searchDevicesForm.value.tags.join(",") : ""
        },
        ...this.commandForm!.value
      }).subscribe({
      next: (next) => {
        this.utilityService.popupSuccess("Command dispatched successfully.");
        this.router.navigate(["command"]);
      }, error: (error) => {
        this.utilityService.popupErrorWithTraceId("Could not dispatch command.", error);
      }
    });
  }

  /**
   * Check if this command can be executed.
   */
  canDispatch(): boolean {
    // Check at least one device or a tag is selected.
    let dispatchOK = (this.selectedHardwareIds.length > 0 || this.searchDevicesForm.value.tags)
      && this.commandForm.valid;

    // Check that for Firmware and Execute type of comamnds, there is a command provided.
    if ([AppConstants.DEVICE.COMMAND.TYPE.FIRMWARE, AppConstants.DEVICE.COMMAND.TYPE.EXECUTE]
    .includes(this.commandForm.value.commandType)) {
      dispatchOK = dispatchOK && this.commandForm.value.command;
    }

    return dispatchOK;
  }

  removeChip(id: string) {
    this.selectedHardwareIds = this.selectedHardwareIds.filter(hardwareId => hardwareId !== id);
  }

  selectHardwareId(hardwareId: string) {
    if (!this.selectedHardwareIds.find(p => p === hardwareId)) {
      this.selectedHardwareIds.push(hardwareId);
      this.searchDevicesForm.patchValue({
        hardwareId: ""
      });
      this.searchDevices = [];
    }
  }
}
