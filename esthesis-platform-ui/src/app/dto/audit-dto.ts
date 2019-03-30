import {UserDto} from './user-dto';

export class AuditDto {
  id: string;
  createdOn: Date;
  event: string;
  shortDescription: string;
  level: string;
  userDTO: UserDto;
}
