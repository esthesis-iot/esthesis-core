export interface AuditDto {
  id: string;
  createdOn: Date;
  createdBy: string;
  category: string;
  operation: string;
  message: string;
  valueIn: string;
  valueOut: string;
}
