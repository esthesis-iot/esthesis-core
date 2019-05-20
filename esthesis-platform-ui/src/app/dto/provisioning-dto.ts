export class ProvisioningDto {
  id: string;
  name: string;
  description: string;
  state: boolean;
  tags: number[];
  file: string;
  fileSize: number;
  packageVersion: string;
  signed: boolean;
  encrypted: boolean;
}
