import {Component, OnInit} from "@angular/core";
import {FormArray, FormBuilder, FormGroup, Validators} from "@angular/forms";
import {BaseComponent} from "../../shared/component/base-component";
import {QFormsService} from "@qlack/forms";
import {ActivatedRoute, Router} from "@angular/router";
import {UtilityService} from "../../shared/service/utility.service";
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
import * as _ from "lodash";
import {
  OkCancelModalComponent
} from "../../shared/component/display/ok-cancel-modal/ok-cancel-modal.component";
import {MatDialog} from "@angular/material/dialog";
import {CampaignStatsDto} from "../dto/campaign-stats-dto";
import {ProvisioningDto} from "../../provisioning/dto/provisioning-dto";
import {AppConstants} from "../../app.constants";
import {ObjectID} from "bson";
import {GroupProgressDto} from "../dto/group-progress-dto";

@Component({
  selector: "app-campaign-edit",
  templateUrl: "./campaign-edit.component.html",
  styleUrls: ["./campaign-edit.component.scss"]
})
export class CampaignEditComponent extends BaseComponent implements OnInit {
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
  // A flag to indicate whether the form is disabled.
  formDisabled = false;
  // An object containing the full details of the campaign as returned by the back-end. This is to
  // facilitate displaying information on the UI that might not necessarily be part of the
  // underlying form object representing the campaign. For example, the date a campaign started
  // (needs to be displayed but is not exchanged with the campaign form object).
  campaign?: CampaignDto;
  now = new Date();
  // The statistics of this campaign.
  campaignStats?: CampaignStatsDto;
  // Group progress.
  groupProgress = [] as GroupProgressDto[];

  constructor(private fb: FormBuilder, public utilityService: UtilityService,
    private qForms: QFormsService, private provisioningService: ProvisioningService,
    private route: ActivatedRoute, private router: Router,
    private deviceService: DevicesService,
    private tagService: TagsService, private campaignService: CampaignsService,
    private dialog: MatDialog) {
    super();
  }

  private getCampaignStats() {
    this.campaignService.stats(this.id).subscribe({
      next: onNext => {
        // Save campaign stats to an object.
        this.campaignStats = onNext;

        // Extract group members for the chart.
        onNext.groupMembersReplied?.forEach((value, index) => {
          this.groupProgress?.push(
            new GroupProgressDto("Group " + (index + 1), value * 100 / onNext.groupMembers![index]));
        });
      }
    });
  }

  private initData(): void {
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

          // Disable the form if the campaign is already running.
          if (this.form.value.state !== AppConstants.CAMPAIGN.STATE.CREATED) {
            this.disableForm();
            this.getCampaignStats();
          }
        }
      });
    }
  }

  ngOnInit(): void {
    // Check if an edit is performed and fetch data.
    this.id = this.route.snapshot.paramMap.get("id");

    // Set up the form.
    this.form = this.fb.group({
      id: [],
      state: [],
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

  private disableForm() {
    this.form.disable();
    this.formDisabled = true;
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

  // @ts-ignore
  addCondition(type: AppConstants.CAMPAIGN.CONDITION.TYPE) {
    const condition = new CampaignConditionDto(type);
    condition.id = new ObjectID().toHexString();

    if (type === this.appConstants.CAMPAIGN.CONDITION.TYPE.SUCCESS) {
      condition.stage = this.appConstants.CAMPAIGN.CONDITION.STAGE.EXIT;
    }
    if (type === this.appConstants.CAMPAIGN.CONDITION.TYPE.BATCH) {
      condition.stage = this.appConstants.CAMPAIGN.CONDITION.STAGE.INSIDE;
    }

    // @ts-ignore
    this.form.controls.conditions.push(
      this.createCondition(condition));
  }

  getConditions() {
    // @ts-ignore
    return this.form.get("conditions").controls;
  }

  // @ts-ignore
  getIcon(type: AppConstants.CAMPAIGN.CONDITION.TYPE): string | undefined {
    switch (type) {
      case this.appConstants.CAMPAIGN.CONDITION.TYPE.DATETIME:
        return "alarm";
      case this.appConstants.CAMPAIGN.CONDITION.TYPE.PAUSE:
        return "pause";
      case this.appConstants.CAMPAIGN.CONDITION.TYPE.PROPERTY:
        return "tune";
      case this.appConstants.CAMPAIGN.CONDITION.TYPE.SUCCESS:
        return "outlined_flag";
      case this.appConstants.CAMPAIGN.CONDITION.TYPE.BATCH:
        return "layers";
      default:
        return undefined;
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
    return _(this.form.get("members")?.value).groupBy("group").values().value();
  }

  /**
   *
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
          new ObjectID().toHexString(),
          this.appConstants.CAMPAIGN.MEMBER_TYPE.DEVICE,
          hardwareId, groupNo));
    }

    // Add tags.
    const tags = this.form.get("searchByTags")!.value;
    if (tags && tags.length > 0) {
      const tagString = tags.join(", ");
      this.form.get("members")?.value.push(
        new CampaignMemberDto(new ObjectID().toHexString(),
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

  validate() {
    // Check that each group/phase only has one condition of type PAUSE.
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
      }
    });
  }

  removeCondition(i: number) {
    (this.form.controls.conditions as FormArray).removeAt(i);
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

  replicate() {
    this.dialog.open(OkCancelModalComponent, {
      data: {
        title: "Replicate campaign",
        question: "Do you really want to replicate this campaign?",
        buttons: {
          ok: true, cancel: true, reload: false
        }
      }
    }).afterClosed().subscribe(result => {
      if (result) {
        this.campaignService.replicateCampaign(this.id!).subscribe({
          next: (campaign) => {
            this.utilityService.popupSuccess("Campaign successfully replicated.");
            console.log(campaign);
            this.router.navigate(["campaigns", campaign.id]).then(() => {
              this.ngOnInit();
            });
          }, error: err => {
            this.utilityService.popupErrorWithTraceId("Could not replicate campaign.", err);
          }
        });
      }
    });
  }
}
