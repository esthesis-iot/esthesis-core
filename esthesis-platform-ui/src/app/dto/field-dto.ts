export interface FieldDto {
  id: number;
  measurement: string;
  field: string;
  datatype: string;
  shown: boolean;
  label: string;
  formatter: string;
  valueHandler: string;
  value: any;
  lastUpdatedOn: Date;
}
