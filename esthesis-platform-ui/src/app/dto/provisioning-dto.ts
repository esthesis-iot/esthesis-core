export class ProvisioningDto {
  id: string;
  name: string;
  description: string;
  defaultIP: boolean;
  state: boolean;
  tags: number[];
  // type: string;
  file: string;
  fileSize: number;
  packageVersion: string;
}
