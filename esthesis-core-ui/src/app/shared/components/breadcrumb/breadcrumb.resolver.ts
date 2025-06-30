import {ActivatedRouteSnapshot, ResolveFn} from "@angular/router";
import {DevicesService} from "../../../devices/devices.service";
import {inject} from "@angular/core";
import {firstValueFrom} from "rxjs";
import {ProvisioningService} from "../../../provisioning/provisioning.service";
import {AppConstants} from "../../../app.constants";
import {CampaignsService} from "../../../campaigns/campaigns.service";
import {CertificatesService} from "../../../certificates/certificates.service";
import {CasService} from "../../../cas/cas.service";
import {KeystoresService} from "../../../keystores/keystores.service";
import {DataflowsService} from "../../../dataflows/dataflows.service";
import {
  InfrastructureMqttService
} from "../../../infrastructure/infrastructure-mqtt/infrastructure-mqtt.service";
import {ApplicationsService} from "../../../applications/applications.service";
import {TagsService} from "../../../tags/tags.service";
import {SecurityService} from "../../../security/security.service";
import {SecurityGroupsService} from "../../../security/security-groups.service";
import {SecurityRolesService} from "../../../security/security-roles.service";
import {SecurityPoliciesService} from "../../../security/security-policies.service";
import {AuditService} from "../../../audit/audit.service";
import {DashboardService} from "../../../dashboard/dashboard.service";
import {CommandsService} from "../../../commands/commands.service";

export const deviceNameResolver: ResolveFn<string> = async (route: ActivatedRouteSnapshot) => {
  return "|" + (await firstValueFrom(inject(DevicesService).findById(route.paramMap.get("id")))).hardwareId;
};

export const provisioningPackageNameResolver: ResolveFn<string> = async (route: ActivatedRouteSnapshot) => {
  const id = route.paramMap.get("id");
  if (id === AppConstants.NEW_RECORD_ID) {
    return "|New provisioning package";
  } else {
    return "|" + (await firstValueFrom(inject(ProvisioningService).findById(route.paramMap.get("id")))).name;
  }
};

export const campaignNameResolver: ResolveFn<string> = async (route: ActivatedRouteSnapshot) => {
  const id = route.paramMap.get("id");
  if (id === AppConstants.NEW_RECORD_ID) {
    return "|New campaign";
  } else {
    return "|" + (await firstValueFrom(inject(CampaignsService).findById(route.paramMap.get("id")))).name;
  }
};

export const certificateNameResolver: ResolveFn<string | undefined> = async (route: ActivatedRouteSnapshot) => {
  const id = route.paramMap.get("id");
  if (id === AppConstants.NEW_RECORD_ID) {
    return "|New certificate";
  } else {
    return "|" + (await firstValueFrom(inject(CertificatesService).findById(route.paramMap.get("id")))).name;
  }
};

export const caNameResolver: ResolveFn<string> = async (route: ActivatedRouteSnapshot) => {
  const id = route.paramMap.get("id");
  if (id === AppConstants.NEW_RECORD_ID) {
    return "|New certificate authority";
  } else {
    return "|" + (await firstValueFrom(inject(CasService).findById(route.paramMap.get("id")))).name;
  }
};

export const keystoreNameResolver: ResolveFn<string> = async (route: ActivatedRouteSnapshot) => {
  const id = route.paramMap.get("id");
  if (id === AppConstants.NEW_RECORD_ID) {
    return "|New keystore";
  } else {
    return "|" + (await firstValueFrom(inject(KeystoresService).findById(route.paramMap.get("id")))).name;
  }
};

export const dflNameResolver: ResolveFn<string> = async (route: ActivatedRouteSnapshot) => {
  const id = route.paramMap.get("id");
  if (id === AppConstants.NEW_RECORD_ID) {
    const type = route.paramMap.get("type");
    return `|New ${type} dataflow`;
  } else {
    return "|" + (await firstValueFrom(inject(DataflowsService).findById(route.paramMap.get("id")))).name;
  }
};

export const infraMqttNameResolver: ResolveFn<string> = async (route: ActivatedRouteSnapshot) => {
  const id = route.paramMap.get("id");
  if (id === AppConstants.NEW_RECORD_ID) {
    return "|New MQTT server";
  } else {
    return "|" + (await firstValueFrom(inject(InfrastructureMqttService).findById(route.paramMap.get("id")))).name;
  }
};

export const applicationNameResolver: ResolveFn<string> = async (route: ActivatedRouteSnapshot) => {
  const id = route.paramMap.get("id");
  if (id === AppConstants.NEW_RECORD_ID) {
    return "|New application";
  } else {
    return "|" + (await firstValueFrom(inject(ApplicationsService).findById(route.paramMap.get("id")))).name;
  }
};

export const tagNameResolver: ResolveFn<string> = async (route: ActivatedRouteSnapshot) => {
  const id = route.paramMap.get("id");
  if (id === AppConstants.NEW_RECORD_ID) {
    return "|New tag";
  } else {
    return "|" + (await firstValueFrom(inject(TagsService).findById(route.paramMap.get("id")))).name;
  }
};

export const dashboardNameResolver: ResolveFn<string> = async (route: ActivatedRouteSnapshot) => {
  const id = route.paramMap.get("id");
  if (id === AppConstants.NEW_RECORD_ID) {
    return "|New dashboard";
  } else {
    return "|" + (await firstValueFrom(inject(DashboardService).findById(route.paramMap.get("id")))).name;
  }
};

export const userNameResolver: ResolveFn<string | null> = (route: ActivatedRouteSnapshot) => {
  const id = route.paramMap.get("id");
  if (id === AppConstants.NEW_RECORD_ID) {
    return "|New user";
  } else {
    return `|${inject(SecurityService).getFullName()} (${inject(SecurityService).getUsername()})`;
  }
};

export const securityGroupNameResolver: ResolveFn<string> = async (route: ActivatedRouteSnapshot) => {
  const id = route.paramMap.get("id");
  if (id === AppConstants.NEW_RECORD_ID) {
    return "|New group";
  } else {
    return "|" + (await firstValueFrom(inject(SecurityGroupsService).findById(route.paramMap.get("id")))).name;
  }
};

export const securityRoleNameResolver: ResolveFn<string> = async (route: ActivatedRouteSnapshot) => {
  const id = route.paramMap.get("id");
  if (id === AppConstants.NEW_RECORD_ID) {
    return "|New role";
  } else {
    return "|" + (await firstValueFrom(inject(SecurityRolesService).findById(route.paramMap.get("id")))).name;
  }
};

export const securityPolicyNameResolver: ResolveFn<string> = async (route: ActivatedRouteSnapshot) => {
  const id = route.paramMap.get("id");
  if (id === AppConstants.NEW_RECORD_ID) {
    return "|New policy";
  } else {
    return "|" + (await firstValueFrom(inject(SecurityPoliciesService).findById(route.paramMap.get("id")))).name;
  }
};

export const auditNameResolver: ResolveFn<string> = async (route: ActivatedRouteSnapshot) => {
  return "|" + (await firstValueFrom(inject(AuditService).findById(route.paramMap.get("id")))).message;
};

export const commandsNameResolver: ResolveFn<string> = async (route: ActivatedRouteSnapshot) => {
  const id = route.paramMap.get("id");
  if (id === AppConstants.NEW_RECORD_ID) {
    return `|New command`;
  } else {
    const commandTypeValue = (await firstValueFrom(inject(CommandsService).findById(id))).commandType;
    const reverseCommandTypeMap = Object.fromEntries(
      Object.entries(AppConstants.DEVICE.COMMAND.TYPE).map(([key, value]) => [value, key])
    );
    const commandTypeName = reverseCommandTypeMap[commandTypeValue] || commandTypeValue;

    return "|" + commandTypeName;
  }
};
