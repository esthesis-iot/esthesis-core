export class UserDto {
  id: string;
  fn: string;
  ln: string;
  email: string;
  password: string;
  newPassword1: string;
  newPassword2: string;
  userType: string;
  status: string;
  createdOn: Date;
  salt: string;
}
