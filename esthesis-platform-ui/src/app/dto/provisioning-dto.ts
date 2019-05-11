export class ProvisioningDto {
  id: string;
  name: string;
  description: string;
  defaultIP: boolean;
  state: boolean;
  tags: number[];
  file: string;
  fileSize: number;
  packageVersion: string;
}
