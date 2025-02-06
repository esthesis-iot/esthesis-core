import {Component, OnInit} from "@angular/core";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {Router} from "@angular/router";
import {debounceTime, distinctUntilChanged} from "rxjs/operators";
import {CommandsService} from "../commands.service";
import {DeviceDto} from "../../devices/dto/device-dto";
import {AppConstants} from "../../app.constants";
import {UtilityService} from "../../shared/services/utility.service";
import {SecurityBaseComponent} from "../../shared/components/security-base-component";
import {CommandExecuteRequestDto} from "../dto/command-execute-request-dto";

@Component({
  selector: "app-command-create",
  templateUrl: "./command-create.component.html"
})
export class CommandCreateComponent extends SecurityBaseComponent implements OnInit {
  searchDevicesForm!: FormGroup;
  commandForm!: FormGroup;
  isSearching = false;
  searchDevices?: DeviceDto[];
  selectedHardwareIds: string[] = [];

  constructor(private readonly formBuilder: FormBuilder,
    private readonly commandService: CommandsService,
    private readonly utilityService: UtilityService, private readonly router: Router) {
    super(AppConstants.SECURITY.CATEGORY.COMMAND);
  }

  ngOnInit() {
    // Step 1 form.
    this.searchDevicesForm = this.formBuilder.group({
      hardwareId: [],
      tags: [],
    });

    // Step 2 form.
    this.commandForm = this.formBuilder.group({
      commandType: [null, [Validators.required]],
      executionType: [this.appConstants.DEVICE.COMMAND.EXECUTION.ASYNCHRONOUS],
      command: [null],
      arguments: [null],
      description: [null]
    });

    // Watch changes on the hardware id to find matching devices.
    this.searchDevicesForm.valueChanges.pipe(debounceTime(500), distinctUntilChanged()
    ).subscribe({
      next: (next) => {
        if (next?.hardwareId) {
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
  }

  /**
   * Save and dispatch the command.
   */
  save() {
    let commandExecuteRequestDto: CommandExecuteRequestDto;
    commandExecuteRequestDto = {
      hardwareIds: this.selectedHardwareIds.join(","),
      tags: this.searchDevicesForm.value.tags ? this.searchDevicesForm.value.tags.join(",") : "",
      commandType: this.commandForm.value.commandType,
      executionType: this.commandForm.value.executionType,
      command: this.commandForm.value.command,
      arguments: this.commandForm.value.arguments,
      description: this.commandForm.value.description
    };
    this.commandService.execute(commandExecuteRequestDto).subscribe({
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
