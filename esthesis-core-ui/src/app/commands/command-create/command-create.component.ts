import {Component, OnInit} from "@angular/core";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {Router} from "@angular/router";
import {debounceTime, distinctUntilChanged} from "rxjs/operators";
import {ProvisioningService} from "../../provisioning/provisioning.service";
import {CommandsService} from "../commands.service";
import {TagDto} from "../../tags/dto/tag-dto";
import {TagsService} from "../../tags/tags.service";
import {DeviceDto} from "../../devices/dto/device-dto";
import {DevicesService} from "../../devices/devices.service";
import {AppConstants} from "../../app.constants";
import {ProvisioningDto} from "../../provisioning/dto/provisioning-dto";
import {UtilityService} from "../../shared/services/utility.service";
import {SecurityBaseComponent} from "../../shared/components/security-base-component";

@Component({
  selector: "app-command-create",
  templateUrl: "./command-create.component.html"
})
export class CommandCreateComponent extends SecurityBaseComponent implements OnInit {
  searchDevicesForm!: FormGroup;
  commandForm!: FormGroup;
  // The list of currently active provisioning packages.
  provisioningPackages?: ProvisioningDto[];
  isSearching = false;
  availableTags: TagDto[] | undefined;
  searchDevices?: DeviceDto[];
  selectedHardwareIds: string[] = [];

  constructor(private formBuilder: FormBuilder, private commandService: CommandsService,
    private utilityService: UtilityService, private router: Router, private tagService: TagsService,
    private provisioningService: ProvisioningService, private deviceService: DevicesService) {
    super(AppConstants.SECURITY.CATEGORY.COMMAND);
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
      executionType: [this.appConstants.DEVICE.COMMAND.EXECUTION.ASYNCHRONOUS],
      command: [""],
      arguments: [""],
      description: [""]
    });

    // Watch changes on the hardware id to find matching devices.
    this.searchDevicesForm.valueChanges.pipe(debounceTime(500), distinctUntilChanged()
    ).subscribe({
      next: (next) => {
        if (next && next.hardwareId) {
          this.commandService.findDevicesByHardwareId(next.hardwareId).subscribe({
            next: (hardwareIds) => {
              if (hardwareIds && hardwareIds.length > 0) {
                this.searchDevices = hardwareIds;
              } else {
                this.searchDevices = [];
              }
            }, error: (error) => {
              this.utilityService.popupErrorWithTraceId("Could not search for devices.", error);
            }
          });
        } else {
          this.searchDevices = [];
        }
      }
    });

    // Get available tags.
    this.tagService.find("sort=name,asc").subscribe(onNext => {
      this.availableTags = onNext.content;
    });

    // Get provisioning packages.
    this.provisioningService.find("available=true&sort=version,desc").subscribe({
      next: (provisioningPackages) => {
        this.provisioningPackages = provisioningPackages.content;
      }, error: (error) => {
        this.utilityService.popupErrorWithTraceId("Could not search for provisioning packages.", error);
      }
    });
  }

  save() {
    // Save the command.
    this.commandService.execute(
      {
        ...{
          hardwareIds: this.selectedHardwareIds.join(","),
          tags: this.searchDevicesForm.value.tags ? this.searchDevicesForm.value.tags.join(",") : ""
        },
        ...this.commandForm.value
      }).subscribe({
      next: () => {
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

    // Check that for Execute & Firmware type of commands, there is a command and execution mode
    // provided.
    if ([AppConstants.DEVICE.COMMAND.TYPE.EXECUTE, AppConstants.DEVICE.COMMAND.TYPE.FIRMWARE]
    .includes(this.commandForm.value.commandType)) {
      dispatchOK = dispatchOK && this.commandForm.value.command
        && this.commandForm.value.executionType;
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
