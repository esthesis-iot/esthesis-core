export interface ProvisioningDto {
  id: string;
  name: string;
  description: string;
  available: boolean;
  version: string;
  prerequisiteVersion: string;
  size: number;
  filename: string;
  contentType: string;
  attributes: string;
  hash: string;
  type: string;
  typeSpecificConfiguration: string;
  cacheStatus: number;
}
