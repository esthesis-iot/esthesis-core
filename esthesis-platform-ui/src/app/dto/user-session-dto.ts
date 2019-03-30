export class UserSessionDTO {
  id: string;
  createdOn: Date;
  terminatedOn: Date;
  // attributes: SessionAttributeDTO[];
}

export class SessionAttributeDTO {
  name: string;
  value: string;
}
