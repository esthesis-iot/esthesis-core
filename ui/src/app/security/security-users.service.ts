import {HttpClient} from "@angular/common/http";
import {Injectable} from "@angular/core";
import {CrudService} from "../shared/services/crud.service";
import {GroupDto} from "./dto/group-dto";
import {PolicyDto} from "./dto/policy-dto";
import {RoleDto} from "./dto/role-dto";
import {UserDto} from "./dto/user-dto";

/**
 * A service to provide tags manipulation.
 *
 */
@Injectable({
  providedIn: "root"
})
export class SecurityUsersService extends CrudService<UserDto> {
  constructor(http: HttpClient) {
    super(http, "security/v1/users");
  }
}
