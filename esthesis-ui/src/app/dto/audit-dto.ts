import {UserDto} from './user-dto';

export interface AuditDto {
  id: string;
  createdOn: Date;
  event: string;
  shortDescription: string;
  level: string;
  userDTO: UserDto;
}
