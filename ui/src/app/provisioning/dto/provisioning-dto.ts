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
  log: string;
  attributes: string;
  sha256: string;
  type: string;
  typeSpecificConfiguration: string;
  cacheStatus: string;
}
