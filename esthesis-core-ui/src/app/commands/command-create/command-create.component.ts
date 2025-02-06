import {Component, OnInit} from "@angular/core";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {Router} from "@angular/router";
import {CommandsService} from "../commands.service";
import {AppConstants} from "../../app.constants";
import {UtilityService} from "../../shared/services/utility.service";
import {SecurityBaseComponent} from "../../shared/components/security-base-component";
import {CommandExecuteRequestDto} from "../dto/command-execute-request-dto";
import {TagsService} from "../../tags/tags.service";
import {DevicesService} from "../../devices/devices.service";
import {ProvisioningService} from "../../provisioning/provisioning.service";

@Component({
  selector: "app-command-create",
  templateUrl: "./command-create.component.html"
})
export class CommandCreateComponent extends SecurityBaseComponent implements OnInit {
  searchDevicesForm!: FormGroup;
  commandForm!: FormGroup;
  isSearching = false;

  constructor(private readonly formBuilder: FormBuilder,
    private readonly commandService: CommandsService, public readonly tagsService: TagsService,
    private readonly utilityService: UtilityService, private readonly router: Router,
    protected readonly devicesService: DevicesService,
    protected readonly provisioningService: ProvisioningService) {
    super(AppConstants.SECURITY.CATEGORY.COMMAND);
  }

  ngOnInit() {
    // Step 1 form.
    this.searchDevicesForm = this.formBuilder.group({
      hardwareIds: [],
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
  }

  /**
   * Save and dispatch the command.
   */
  save() {
    let commandExecuteRequestDto: CommandExecuteRequestDto;
    commandExecuteRequestDto = {
      hardwareIds: this.searchDevicesForm.value.hardwareIds ? this.searchDevicesForm.value.hardwareIds.join(",") : "",
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
    let dispatchOK = (this.searchDevicesForm.getRawValue()["hardwareIds"] &&  this.searchDevicesForm.getRawValue()["hardwareIds"].length > 0 || this.searchDevicesForm.value.tags)
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

}
