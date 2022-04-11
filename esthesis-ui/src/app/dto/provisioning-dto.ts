export interface ProvisioningDto {
  id: string;
  name: string;
  description: string;
  state: boolean;
  tags: number[];
  file: string;
  fileSize: number;
  packageVersion: string;
}
