import {DashboardUpdateDto} from "./DashboardUpdateDto";

export interface DashboardUpdateAuditDto extends DashboardUpdateDto {
  auditEntries: [{}];
}
