export interface DockerTagsDto {
  count: number;
  results: [{
    name: string;
    status: string;
    lastUpdated: Date;
  }];
}
