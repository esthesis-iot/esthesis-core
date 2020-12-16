export interface UserSessionDTO {
  id: string;
  createdOn: Date;
  terminatedOn: Date;
  // attributes: SessionAttributeDTO[];
}

export interface SessionAttributeDTO {
  name: string;
  value: string;
}
