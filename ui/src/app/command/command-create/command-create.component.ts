import {Component, OnInit} from "@angular/core";
import {BaseComponent} from "../../shared/component/base-component";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {UtilityService} from "../../shared/service/utility.service";
import {Router} from "@angular/router";
import {forkJoin} from "rxjs";
import {debounceTime, distinctUntilChanged} from "rxjs/operators";
import {ProvisioningDto} from "../../dto/provisioning-dto";
import {ProvisioningService} from "../../provisioning/provisioning.service";
import {AppConstants} from "../../app.constants";
import {CommandService} from "../command.service";

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

  constructor(private formBuilder: FormBuilder, private commandService: CommandService,
    private utilityService: UtilityService, private router: Router,
    private provisioningService: ProvisioningService) {
    super();
  }

  ngOnInit() {
    // Step 1 form.
    this.searchDevicesForm = this.formBuilder.group({
      hardwareIds: [""],
      tags: [""],
      matches: [0, [Validators.min(1)]],
      devicesMatchedByHardwareIds: [0],
      devicesMatchedByTags: [0]
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
    this.searchDevicesForm.valueChanges.pipe(
      debounceTime(500),
      distinctUntilChanged()
    ).subscribe(onNext => {
      this.isSearching = true;
      forkJoin([
        this.commandService.findDevicesByHardwareIds(onNext.hardwareIds),
        this.commandService.findDevicesByTags(onNext.tags)]).subscribe({
        next: ([ids, tags]) => {
          this.searchDevicesForm!.patchValue({
            devicesMatchedByHardwareIds: ids,
            devicesMatchedByTags: tags,
            matches: (ids + tags)
          }, {emitEvent: false});
        }, error: (err) => {
          this.utilityService.popupErrorWithTraceId("Could not find matching devices.", err);
        }, complete: () => {
          this.isSearching = false;
        }
      });
    });

    // Get provisioning packages.
    // this.provisioningService.find("state=1&sort=name,packageVersion").subscribe(onNext => {
    //   this.provisioningPackages = onNext.content;
    // });
  }

  save() {
    this.commandService.execute(
      {...this.searchDevicesForm!.value, ...this.commandForm!.value}).subscribe({
      next: (next) => {
        this.utilityService.popupSuccess("Command dispatched successfully.");
        this.router.navigate(["command"]);
      }, error: (error) => {
        this.utilityService.popupErrorWithTraceId("Could not dispatch command.", error);
      }
    });
  }

  canDispatch(): boolean {
    let dispatchOK = true;

    dispatchOK = dispatchOK && this.searchDevicesForm.controls.devicesMatchedByHardwareIds.value > 0;
    dispatchOK = dispatchOK && this.commandForm.valid;

    if ([AppConstants.DEVICE.COMMAND.TYPE.FIRMWARE, AppConstants.DEVICE.COMMAND.TYPE.EXECUTE]
    .includes(this.commandForm.value.commandType)) {
      dispatchOK = dispatchOK && this.commandForm.value.command;
    }

    return dispatchOK;
  }
}
