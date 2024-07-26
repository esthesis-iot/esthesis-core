import {Component, isDevMode, OnInit} from "@angular/core";
import {FormArray, FormBuilder, FormGroup, Validators} from "@angular/forms";
import {QFormsService} from "@qlack/forms";
import {ActivatedRoute, Router} from "@angular/router";
import {ProvisioningService} from "../../provisioning/provisioning.service";
import {CampaignMemberDto} from "../dto/campaign-member-dto";
import {DevicesService} from "../../devices/devices.service";
import {DeviceDto} from "../../devices/dto/device-dto";
import {debounceTime, distinctUntilChanged} from "rxjs/operators";
import {TagDto} from "../../tags/dto/tag-dto";
import {TagsService} from "../../tags/tags.service";
import {CampaignsService} from "../campaigns.service";
import {CampaignDto} from "../dto/campaign-dto";
import {CampaignConditionDto} from "../dto/campaign-condition-dto";
import * as _ from "lodash-es";
import {groupBy, values} from "lodash-es";
import {
  OkCancelModalComponent
} from "../../shared/components/ok-cancel-modal/ok-cancel-modal.component";
import {CampaignStatsDto} from "../dto/campaign-stats-dto";
import {ProvisioningDto} from "../../provisioning/dto/provisioning-dto";
import {AppConstants} from "../../app.constants";
import {GroupProgressDto} from "../dto/group-progress-dto";
import {v4 as uuidv4} from "uuid";
import {MatDialog} from "@angular/material/dialog";
import {
  faCalendar,
  faFlag,
  faLayerGroup,
  faListCheck,
  faPause,
  faQuestion
} from "@fortawesome/free-solid-svg-icons";
import {IconProp} from "@fortawesome/fontawesome-svg-core";
import {UtilityService} from "../../shared/services/utility.service";
import {SecurityBaseComponent} from "../../shared/components/security-base-component";
import {ConstraintViolationDto} from "../../shared/dto/constraint-violation-dto";

@Component({
  selector: "app-campaign-edit",
  templateUrl: "./campaign-edit.component.html"
})
export class CampaignEditComponent extends SecurityBaseComponent implements OnInit {
  // Campaign details form.
  form!: FormGroup;
  // The id of the form currently being processed.
  id!: string | null;
  // The list of currently active provisioning packages.
  provisioningPackages?: ProvisioningDto[];
  // A helper auto-complete container for devices matching the user's search input.
  searchDevices?: DeviceDto[];
  // The list of currently available tags.
  availableTags: TagDto[] | undefined;
  memberGroups: any;
  // Validation errors.
  errorsMain?: string[];
  errorsDevices?: string[];
  errorsConditions?: string[];
  // An object containing the full details of the campaign as returned by the back-end. This is to
  // facilitate displaying information on the UI that might not necessarily be part of the
  // underlying form object representing the campaign. For example, the date a campaign started
  // (needs to be displayed but is not exchanged with the campaign form object).
  campaign?: CampaignDto;
  now = new Date();
  // The statistics of this campaign.
  campaignStats?: CampaignStatsDto;
  // Flags for the various buttons of this component. The status of each button is calculated when
  // the component is initialized.
  isButtonStartEnabled = false;
  isButtonStopEnabled = false;
  isButtonResumeEnabled = false;
  isButtonCancelEnabled = false;
  isButtonSaveEnabled = false;
  isButtonDeleteEnabled = false;
  isButtonReplayEnabled = false;
  isStatisticsEnabled = false;
  isLiveEnabled = false;
  constraintViolations?: ConstraintViolationDto[];

  constructor(private fb: FormBuilder, public utilityService: UtilityService,
    private qForms: QFormsService, private provisioningService: ProvisioningService,
    private route: ActivatedRoute, private router: Router,
    private deviceService: DevicesService,
    private tagService: TagsService, private campaignService: CampaignsService,
    private dialog: MatDialog) {
    super(AppConstants.SECURITY.CATEGORY.CAMPAIGN, route.snapshot.paramMap.get("id"));
  }

  initData(): void {
    // Fill-in the form with data if editing an existing item.
    if (this.id && this.id !== this.appConstants.NEW_RECORD_ID) {
      this.campaignService.findById(this.id).subscribe({
        next: onNext => {
          // Keep a copy of the complete campaign data for UI references.
          this.campaign = onNext;
          // Fill-in the campaign form object.
          this.form.patchValue(onNext);
          // Update device membership into this campaign.
          this.memberGroups = this.getMemberGroups();
          this.form.patchValue({conditions: []});
          if (onNext.conditions) {
            onNext.conditions.forEach(c => {
              // @ts-ignore
              this.form.controls.conditions.push(this.createCondition(c));
            });
          }

          // If the campaign is already running, disable the form and get campaign statistics.
          if (this.campaign.state !== AppConstants.CAMPAIGN.STATE.CREATED) {
            this.form.disable();
            this.getCampaignStats();
          }

          // Calculate the state of the various buttons.
          this.isButtonStartEnabled =
            this.id !== AppConstants.NEW_RECORD_ID &&
            this.campaign.state === AppConstants.CAMPAIGN.STATE.CREATED;

          this.isButtonStopEnabled =
            ![AppConstants.CAMPAIGN.STATE.TERMINATED_BY_WORKFLOW,
              AppConstants.CAMPAIGN.STATE.TERMINATED_BY_USER,
              AppConstants.CAMPAIGN.STATE.CREATED].includes(this.campaign.state);

          this.isButtonResumeEnabled =
            [AppConstants.CAMPAIGN.STATE.PAUSED_BY_WORKFLOW,
              AppConstants.CAMPAIGN.STATE.PAUSED_BY_USER].includes(this.campaign.state);

          this.isButtonCancelEnabled = true;

          this.isButtonSaveEnabled =
            this.id === AppConstants.NEW_RECORD_ID ||
            this.campaign.state === AppConstants.CAMPAIGN.STATE.CREATED;

          this.isButtonDeleteEnabled =
            this.id !== AppConstants.NEW_RECORD_ID;

          this.isButtonReplayEnabled =
            this.id !== AppConstants.NEW_RECORD_ID;

          this.isStatisticsEnabled =
            AppConstants.CAMPAIGN.STATE.CREATED !== this.campaign.state;

          this.isLiveEnabled = AppConstants.CAMPAIGN.STATE.RUNNING === this.campaign.state;
        }
      });
    } else {
      // Calculate the state of the various buttons when creating/editing a new campaign.
      this.isButtonCancelEnabled = true;
      this.isButtonSaveEnabled = true;
      this.isButtonDeleteEnabled = true;
      this.isButtonReplayEnabled = false;
      this.isStatisticsEnabled = false;
    }
  }

  ngOnInit(): void {
    // Check if an edit is performed and fetch data.
    this.id = this.route.snapshot.paramMap.get("id");

    // Set up the form.
    this.form = this.fb.group({
      id: [],
      // state: [],
      name: [null, [Validators.required]],
      description: [],
      scheduleDate: [],
      scheduleHour: [],
      scheduleMinute: [],
      type: [null, [Validators.required]],
      commandName: [],
      commandArguments: [],
      commandExecutionType: [],
      provisioningPackageId: [],
      conditions: this.fb.array([]),
      members: [[]],
      searchByHardwareId: [],
      searchByTags: [],
      advancedDateTimeRecheckTimer: [isDevMode() ? "PT1S" : "PT1M", [Validators.required]],
      advancedPropertyRecheckTimer: [isDevMode() ? "PT1S" : "PT1M", [Validators.required]],
      advancedUpdateRepliesTimer: [isDevMode() ? "PT1S" : "PT1M", [Validators.required]],
      advancedUpdateRepliesFinalTimer: [isDevMode() ? "PT1S" : "PT1M", [Validators.required]],
    });
    this.isFormDisabled().subscribe(disabled => disabled && this.form.disable());

    // Conditional validators.
    this.form.valueChanges.subscribe(val => {
      if (val['type'] == this.appConstants.CAMPAIGN.TYPE.EXECUTE_COMMAND) {
        this.form.controls['commandName'].addValidators(Validators.required);
      } else {
        this.form.controls['commandName'].removeValidators(Validators.required);
      }
      if (val['type'] == this.appConstants.CAMPAIGN.TYPE.PROVISIONING) {
        this.form.controls['provisioningPackageId'].addValidators(Validators.required);
      } else {
        this.form.controls['provisioningPackageId'].removeValidators(Validators.required);
      }
    });

    // Monitor campaign type to fetch provisioning packages.
    this.form.get("type")!.valueChanges.subscribe(onNext => {
      if (onNext === this.appConstants.CAMPAIGN.TYPE.PROVISIONING) {
        this.getProvisioningPackages();
      }
    });

    // Monitor for changes in search by hardware id input.
    this.form.get("searchByHardwareId")!.valueChanges.pipe(
      debounceTime(500),
      distinctUntilChanged()
    ).subscribe({
      next: (searchVal: string) => {
        if (searchVal && searchVal.trim() !== "") {
          this.deviceService.findDeviceByPartialHardwareId(searchVal).subscribe({
            next: (devices: DeviceDto[]) => {
              if (devices && devices.length > 0) {
                this.searchDevices = devices;
              } else {
                this.searchDevices = [];
              }
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

    this.initData();
  }

  // @ts-ignore
  addCondition(type: AppConstants.CAMPAIGN.CONDITION.TYPE) {
    const condition = new CampaignConditionDto(type);
    condition.id = uuidv4();

    if (type === this.appConstants.CAMPAIGN.CONDITION.TYPE.SUCCESS) {
      condition.stage = this.appConstants.CAMPAIGN.CONDITION.STAGE.EXIT;
    }
    if (type === this.appConstants.CAMPAIGN.CONDITION.TYPE.BATCH) {
      condition.stage = this.appConstants.CAMPAIGN.CONDITION.STAGE.INSIDE;
    }

    // @ts-ignore
    this.form.controls.conditions.push(this.createCondition(condition));
  }

  getConditions() {
    // @ts-ignore
    return this.form.get("conditions").controls;
  }

  getIcon(type: string): IconProp {
    switch (type) {
      case this.appConstants.CAMPAIGN.CONDITION.TYPE.DATETIME:
        return faCalendar;
      case this.appConstants.CAMPAIGN.CONDITION.TYPE.PAUSE:
        return faPause;
      case this.appConstants.CAMPAIGN.CONDITION.TYPE.PROPERTY:
        return faListCheck;
      case this.appConstants.CAMPAIGN.CONDITION.TYPE.SUCCESS:
        return faFlag;
      case this.appConstants.CAMPAIGN.CONDITION.TYPE.BATCH:
        return faLayerGroup;
      default:
        return faQuestion;
    }
  }

  getDescription(type: string): string {
    switch (type) {
      case this.appConstants.CAMPAIGN.CONDITION.TYPE.DATETIME:
        return "Date/Time";
      case this.appConstants.CAMPAIGN.CONDITION.TYPE.PAUSE:
        return "Pause";
      case this.appConstants.CAMPAIGN.CONDITION.TYPE.PROPERTY:
        return "Property check";
      case this.appConstants.CAMPAIGN.CONDITION.TYPE.SUCCESS:
        return "Success rate";
      case this.appConstants.CAMPAIGN.CONDITION.TYPE.BATCH:
        return "Batch size";
      default:
        return "Unknown condition";
    }
  }

  getProvisioningPackages() {
    this.provisioningService.find("available=true&sort=name,asc,packageVersion,asc").subscribe(onNext => {
      this.provisioningPackages = onNext.content;
    });
  }

  currentGroup(): number {
    let groupNo;
    if (this.form.get("members")?.value.length === 0) {
      groupNo = 1;
    } else {
      groupNo = (_.maxBy(this.form.get("members")?.value, (o: CampaignMemberDto) => o.group) as CampaignMemberDto).group;
    }

    return groupNo;
  }

  getMemberGroups() {
    return values(groupBy(this.form.get("members")?.value, 'group'));
  }

  /**
   * Adds a device or tag to the campaign.
   * @param groupNumber
   *          undefined: The entry is added on the last group. If no groups exist, a new group is
   *                     automatically created.
   *         number > 0: The entry is added in the specified group.
   *                  0: The entry is added in a new group.
   */
  addDeviceOrTag(groupNumber?: number) {
    console.log("Adding to group: " + groupNumber);
    let groupNo: number;
    if (groupNumber === undefined) {
      groupNo = this.currentGroup();
    } else {
      if (groupNumber === 0) {
        groupNo = this.currentGroup() + 1;
      } else {
        groupNo = groupNumber;
      }
    }

    // Add hardware Ids.
    let hardwareId = this.form.get("searchByHardwareId")?.value;
    if (hardwareId && hardwareId !== "") {
      hardwareId = hardwareId.trim();
      this.form.get("members")?.value.push(
        new CampaignMemberDto(
          uuidv4(),
          this.appConstants.CAMPAIGN.MEMBER_TYPE.DEVICE,
          hardwareId, groupNo));
    }

    // Add tags.
    const tags = this.form.get("searchByTags")!.value;
    if (tags && tags.length > 0) {
      const tagString = tags.join(", ");
      this.form.get("members")?.value.push(
        new CampaignMemberDto(uuidv4(),
          this.appConstants.CAMPAIGN.MEMBER_TYPE.TAG,
          tagString, groupNo));
    }

    // Clear search boxes.
    this.form.patchValue({
      searchByHardwareId: "",
      searchByTags: ""
    });

    this.memberGroups = this.getMemberGroups();
  }

  removeMember(identifier: string) {
    _.remove(this.form.get("members")!.value, (o: CampaignMemberDto) => o.identifier === identifier);
    this.memberGroups = this.getMemberGroups();
  }

  removeGroup(groupOrder: number) {
    groupOrder++;
    // Remove group members.
    _.remove(this.form.get("members")!.value, (o: CampaignMemberDto) => o.group === groupOrder);

    // Rearrange groups.
    _.map(this.form.get("members")!.value, (o: CampaignMemberDto) => {
      if (o.group > groupOrder) {
        o.group = o.group - 1;
      }
      return o;
    });

    this.memberGroups = this.getMemberGroups();
  }

  start() {
    this.dialog.open(OkCancelModalComponent, {
      data: {
        title: "Starting campaign",
        question: "Once a campaign is started it can not be further edited. Are you sure you" +
          " want to proceed?",
        buttons: {
          ok: true, cancel: true, reload: false
        }
      }
    }).afterClosed().subscribe(result => {
      if (result) {
        this.save(true);
      }
    });
  }

  save(startCampaign: boolean) {
    // Clear unused entries and errors.
    this.form.get("searchByHardwareId")?.setValue("");
    this.form.get("searchByTags")?.setValue("");

    this.campaignService.save(this.form.getRawValue() as CampaignDto).subscribe({
      next: () => {
        this.errorsMain = undefined;
        this.errorsDevices = undefined;
        this.errorsConditions = undefined;
        if (startCampaign) {
          this.campaignService.startCampaign(this.id!).subscribe({
            next: () => {
              this.utilityService.popupSuccess("Campaign successfully started.");
              this.router.navigate(["campaigns"]);
            }, error: () => {
              this.utilityService.popupError("Campaign could not be started.");
            }
          });
        } else {
          this.utilityService.popupSuccess("Campaign successfully saved.");
          this.router.navigate(["campaigns"]);
        }
      }, error: (error) => {
        this.errorsMain = error.error.main;
        this.errorsDevices = error.error.devices;
        this.errorsConditions = error.error.conditions;
        if (startCampaign) {
          this.utilityService.popupError("Campaign could not be started.");
        } else {
          this.utilityService.popupError("Campaign can not be saved.");
        }
        if (error.status === 400) {
          this.constraintViolations = error.error.violations;
        }
      }
    });
  }

  removeCondition(i: number) {
    (this.form.controls['conditions'] as FormArray).removeAt(i);
  }

  delete() {
    this.dialog.open(OkCancelModalComponent, {
      data: {
        title: "Delete campaign",
        question: "Do you really want to delete this campaign?",
        buttons: {
          ok: true, cancel: true, reload: false
        }
      }
    }).afterClosed().subscribe(result => {
      if (result) {
        this.campaignService.delete(this.id).subscribe({
          next: () => {
            this.utilityService.popupSuccess("Campaign successfully deleted.");
            this.router.navigate(["campaigns"]);
          }, error: err => {
            this.utilityService.popupErrorWithTraceId("Could not delete campaign.", err);
          }
        });
      }
    });
  }

  stop() {
    this.dialog.open(OkCancelModalComponent, {
      data: {
        title: "Terminate campaign",
        question: "Terminating a campaign is a permanent action and you will not be able to resume this campaign later. " +
          "Do you want to terminate this campaign?",
        buttons: {
          ok: true, cancel: true, reload: false
        }
      }
    }).afterClosed().subscribe(result => {
      if (result) {
        this.campaignService.stopCampaign(this.id!).subscribe({
          next: () => {
            this.utilityService.popupSuccess("Campaign successfully terminated.");
            this.router.navigate(["campaigns"]);
          }, error: err => {
            this.utilityService.popupErrorWithTraceId("Could not terminate campaign.", err);
          }
        });
      }
    });
  }

  resume() {
    this.dialog.open(OkCancelModalComponent, {
      data: {
        title: "Resume campaign",
        question: "Do you really want to resume this campaign?",
        buttons: {
          ok: true, cancel: true, reload: false
        }
      }
    }).afterClosed().subscribe(result => {
      if (result) {
        this.campaignService.resumeCampaign(this.id!).subscribe({
          next: () => {
            this.utilityService.popupSuccess("Campaign successfully resumed.");
            this.router.navigate(["campaigns"]);
          }, error: err => {
            this.utilityService.popupErrorWithTraceId("Could not resume campaign.", err);
          }
        });
      }
    });
  }

  replay() {
    this.dialog.open(OkCancelModalComponent, {
      data: {
        title: "Replay campaign",
        question: "A new campaign will be created to replay this campaign, proceed?",
        buttons: {
          ok: true, cancel: true, reload: false
        }
      }
    }).afterClosed().subscribe(result => {
      if (result) {
        this.campaignService.replayCampaign(this.id!).subscribe({
          next: (campaign) => {
            this.utilityService.popupSuccess("Campaign successfully replayed.");
            this.router.navigate(["campaigns", campaign.id]).then(() => {
              this.ngOnInit();
            });
          }, error: err => {
            this.utilityService.popupErrorWithTraceId("Could not replay campaign.", err);
          }
        });
      }
    });
  }

  private getCampaignStats() {
    this.campaignService.stats(this.id).subscribe({
      next: onNext => {
        // Save campaign stats to an object.
        this.campaignStats = onNext;
        this.campaignStats.groupProgress = [];

        // Extract group members for the chart.
        onNext.groupMembersReplied?.forEach((value, index) => {
          this.campaignStats!.groupProgress.push(
            new GroupProgressDto("Group " + (index + 1), value * 100 / onNext.groupMembers![index]));
        });
      }
    });
  }

  private createCondition(campaignConditionDto: CampaignConditionDto) {
    return this.fb.group({
      id: campaignConditionDto.id,
      type: campaignConditionDto.type,
      group: campaignConditionDto.group,
      stage: campaignConditionDto.stage,
      scheduleDate: campaignConditionDto.scheduleDate,
      scheduleHour: campaignConditionDto.scheduleHour,
      scheduleMinute: campaignConditionDto.scheduleMinute,
      operation: campaignConditionDto.operation,
      value: campaignConditionDto.value,
      propertyName: campaignConditionDto.propertyName,
      propertyIgnorable: campaignConditionDto.propertyIgnorable,
    });
  }
}
