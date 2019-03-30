export class FirmwareDto {
  id: string;
  version: string;
  releaseDate: Date;
  pushDate: Date;
  description: string;
  supportedDeployments: string;
  deploymentURL: string;
  certificates: string;
  releaseNotes: string;
}
