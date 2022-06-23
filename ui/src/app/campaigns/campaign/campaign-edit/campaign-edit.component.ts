import {Component, OnInit} from '@angular/core';
import {FormArray, FormBuilder, FormGroup} from '@angular/forms';
import {BaseComponent} from '../../../shared/component/base-component';
import {QFormsService} from '@qlack/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {UtilityService} from '../../../shared/service/utility.service';
import {ProvisioningService} from '../../../provisioning/provisioning.service';
import {ProvisioningDto} from '../../../dto/provisioning-dto';
import {CampaignMemberDto} from '../../../dto/campaign-member-dto';
import {DevicesService} from '../../../devices/devices.service';
import {DeviceDto} from '../../../dto/device-dto';
import {debounceTime, distinctUntilChanged} from 'rxjs/operators';
import {TagDto} from '../../../dto/tag-dto';
import {TagService} from '../../../tags/tag.service';
import {CampaignsService} from '../../campaigns.service';
import {CampaignDto} from '../../../dto/campaign-dto';
import {CampaignConditionDto} from '../../../dto/campaign-condition-dto';
import * as _ from "lodash"
import {AppConstants} from '../../../app.constants';
import {
  OkCancelModalComponent
} from '../../../shared/component/display/ok-cancel-modal/ok-cancel-modal.component';
import {MatDialog} from '@angular/material/dialog';
import {CampaignStatsDto} from "../../../dto/campaign-stats-dto";

@Component({
  selector: 'app-campaign-edit',
  templateUrl: './campaign-edit.component.html',
  styleUrls: ['./campaign-edit.component.scss']
})
export class CampaignEditComponent extends BaseComponent implements OnInit {
  // Expose application constants.
  constants = AppConstants;
  // Campaign details form.
  form!: FormGroup;
  // The id of the form currently being processed.
  id?: number;
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
  // An object containing the full details of the campaign as returned by the back-end. This is to facilitate displaying
  // information on the UI that might not necessarily be part of the underlying form object representing the campaign.
  // For example, the date a campaign started (needs to be displayed but is not exchanged with the campaign form object).
  campaign?: CampaignDto;
  now = new Date();
  campaignStats?: CampaignStatsDto;
  campaignChart?: any;

  constructor(private fb: FormBuilder, public utilityService: UtilityService,
              private qForms: QFormsService, private provisioningService: ProvisioningService,
              private route: ActivatedRoute, private router: Router,
              private utilService: UtilityService, private deviceService: DevicesService,
              private tagService: TagService, private campaignService: CampaignsService,
              private dialog: MatDialog) {
    super();
  }

  private getCampaignStats() {
    this.campaignService.stats(this.id).subscribe(
      onNext => {
        // Save campaign stats to an object.
        this.campaignStats = onNext

        // Extract group members for the chart.
        this.campaignChart =
          onNext.groupMembersReplied?.map((value, index, array) => {
            return {
              "name": "Group " + (index + 1),
              "value": value
            }
          });
      });
  }

  private initData(): void {
    // Fill-in the form with data if editing an existing item.
    if (this.id && this.id !== 0) {
      this.campaignService.findById(this.id).subscribe(onNext => {
        // Keep a copy of the complete campaign data for UI references.
        this.campaign = onNext;
        // Fill-in the campaign form object.
        this.form.patchValue(onNext);
        // Update device membership into this campaign.
        this.memberGroups = this.getMemberGroups();
        this.form.patchValue({"conditions": []});
        if (onNext.conditions) {
          onNext.conditions.forEach(c => {
            // @ts-ignore
            this.form.controls['conditions'].push(this.createCondition(c));
          });
        }

        // Disable the form if the campaign is already running.
        if (this.form.value.state !== AppConstants.CAMPAIGN.STATE.CREATED) {
          this.disableForm();
          this.getCampaignStats();
        }
      });
    }
  }

  ngOnInit(): void {
    // Check if an edit is performed and fetch data.
    this.id = Number(this.route.snapshot.paramMap.get('id'));

    // Setup the form.
    this.form = this.fb.group({
      id: ['', []],
      state: ['', []],
      name: ['', []],
      description: ['', []],
      scheduleDate: ['', []],
      scheduleHour: ['', []],
      scheduleMinute: ['', []],
      type: ['', []],
      commandName: ['', []],
      commandArguments: ['', []],
      provisioningPackageId: ['', []],
      conditions: this.fb.array([]),
      members: [[]],

      searchByHardwareId: ['', []],
      searchByTags: ['', []],
    });

    // Monitor campaign type to fetch provisioning packages.
    this.form.get("type")!.valueChanges.subscribe(onNext => {
      if (onNext === this.constants.CAMPAIGN.TYPE.PROVISIONING) {
        this.getProvisioningPackages();
      }
    });

    // Monitor for changes in search by hardware Id input.
    this.form.get("searchByHardwareId")!.valueChanges.pipe(
      debounceTime(500),
      distinctUntilChanged()
    ).subscribe(onNext => {
      if (onNext && onNext.trim() !== "") {
        this.deviceService.findDeviceByPartialHardwareId(onNext).subscribe(
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
    this.tagService.getAll().subscribe(onNext => {
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
      target: campaignConditionDto.target,
      stage: campaignConditionDto.stage,
      scheduleDate: campaignConditionDto.scheduleDate,
      scheduleHour: campaignConditionDto.scheduleHour,
      scheduleMinute: campaignConditionDto.scheduleMinute,
      operation: campaignConditionDto.operation,
      value: campaignConditionDto.value,
      propertyName: campaignConditionDto.propertyName,
    });
  }

  addCondition(type: number) {
    // @ts-ignore
    this.form.controls['conditions'].push(
      this.createCondition(new CampaignConditionDto(type)));
  }

  getConditions() {
    // @ts-ignore
    return this.form.get('conditions')['controls'];
  }

  getIcon(type: number): string | undefined {
    switch (type) {
      case this.constants.CAMPAIGN.CONDITION.TYPE.DATETIME:
        return "alarm";
      case this.constants.CAMPAIGN.CONDITION.TYPE.PAUSE:
        return "pause";
      case this.constants.CAMPAIGN.CONDITION.TYPE.PROPERTY:
        return "tune";
      case this.constants.CAMPAIGN.CONDITION.TYPE.SUCCESS:
        return "outlined_flag";
      case this.constants.CAMPAIGN.CONDITION.TYPE.BATCH:
        return "layers";
      default:
        return undefined;
    }
  }

  getProvisioningPackages() {
    this.provisioningService.find("state=1&sort=name,packageVersion").subscribe(onNext => {
      this.provisioningPackages = onNext.content;
    });
  }

  currentGroup(): number {
    let groupNo;
    if (this.form.get('members')?.value.length == 0) {
      groupNo = 1;
    } else {
      groupNo = (_.maxBy(this.form.get('members')?.value, function (o: CampaignMemberDto) {
        return o.groupOrder;
      }) as CampaignMemberDto).groupOrder;
    }

    return groupNo;
  }

  getMemberGroups() {
    return _(this.form.get('members')?.value).groupBy('groupOrder').values().value();
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
    let groupNo: number;
    if (groupNumber == undefined) {
      groupNo = this.currentGroup();
    } else {
      if (groupNumber == 0) {
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
        new CampaignMemberDto(this.constants.CAMPAIGN.MEMBER_TYPE.DEVICE, hardwareId, groupNo));
    }

    // Add tags.
    const tags = this.form.get("searchByTags")!.value;
    if (tags && tags.length > 0) {
      const tagString = tags.join(", ");
      this.form.get("members")?.value.push(
        new CampaignMemberDto(this.constants.CAMPAIGN.MEMBER_TYPE.TAG, tagString, groupNo));
    }

    // Clear search boxes.
    this.form.patchValue({
      searchByHardwareId: "",
      searchByTags: ""
    });

    this.memberGroups = this.getMemberGroups();
  }

  removeMember(identifier: string) {
    _.remove(this.form.get("members")!.value, function (o: CampaignMemberDto) {
      return o.identifier === identifier
    });
    this.memberGroups = this.getMemberGroups();
  }

  removeGroup(groupOrder: number) {
    groupOrder++;
    // Remove group members.
    _.remove(this.form.get("members")!.value, function (o: CampaignMemberDto) {
      return o.groupOrder === groupOrder
    });

    // Rearrange groups.
    _.map(this.form.get("members")!.value, function (o: CampaignMemberDto) {
      if (o.groupOrder > groupOrder) {
        o.groupOrder = o.groupOrder - 1;
      }
      return o;
    })

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
    this.form.get("searchByHardwareId")?.setValue(undefined);
    this.form.get("searchByTags")?.setValue(undefined);

    this.campaignService.save(
      this.qForms.cleanupData(this.form.getRawValue()) as CampaignDto).subscribe(
      onNext => {
        this.errorsMain = undefined;
        this.errorsDevices = undefined;
        this.errorsConditions = undefined;
        if (startCampaign) {
          this.campaignService.startCampaign(Number(onNext)).subscribe(
            onNext => {
              this.utilityService.popupSuccess('Campaign successfully started.');
              this.router.navigate(['campaigns']);
            }, onError => {
              this.utilityService.popupError("Campaign could not be started.");
            }
          );
        } else {
          this.utilityService.popupSuccess('Campaign successfully saved.');
          this.router.navigate(['campaigns']);
        }
      },
      onError => {
        this.errorsMain = onError.error.main;
        this.errorsDevices = onError.error.devices;
        this.errorsConditions = onError.error.conditions;
        if (startCampaign) {
          this.utilityService.popupError("Campaign could not be started.");
        } else {
          this.utilityService.popupError("Campaign can not be saved.");
        }
      });
  }

  removeCondition(i: number) {
    (this.form.controls['conditions'] as FormArray).removeAt(i);
  }

  delete() {
    this.dialog.open(OkCancelModalComponent, {
      data: {
        title: 'Delete campaign',
        question: 'Do you really want to delete this campaign?',
        buttons: {
          ok: true, cancel: true, reload: false
        }
      }
    }).afterClosed().subscribe(result => {
      if (result) {
        this.campaignService.delete(this.id).subscribe(onNext => {
          this.utilityService.popupSuccess('Campaign successfully deleted.');
          this.router.navigate(['campaigns']);
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
        this.campaignService.stopCampaign(this.id!).subscribe(onNext => {
          this.utilityService.popupSuccess('Campaign successfully terminated.');
          this.router.navigate(['campaigns']);
        });
      }
    });
  }

  pause() {
    this.dialog.open(OkCancelModalComponent, {
      data: {
        title: "Pause campaign",
        question: "Do you really want to pause this campaign?",
        buttons: {
          ok: true, cancel: true, reload: false
        }
      }
    }).afterClosed().subscribe(result => {
      if (result) {
        this.campaignService.pauseCampaign(this.id!).subscribe(onNext => {
          this.utilityService.popupSuccess('Campaign successfully paused.');
          this.ngOnInit();
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
        this.campaignService.resumeCampaign(this.id!).subscribe(onNext => {
          this.utilityService.popupSuccess('Campaign successfully resumed.');
          this.ngOnInit();
        });
      }
    });
  }
}
