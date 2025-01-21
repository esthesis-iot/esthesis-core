import {HttpClient} from "@angular/common/http";
import {Injectable} from "@angular/core";
import {CrudService} from "../shared/services/crud.service";
import {RoleDto} from "./dto/role-dto";

/**
 * A service to provide tags manipulation.
 *
 */
@Injectable({
  providedIn: "root"
})
export class SecurityRolesService extends CrudService<RoleDto> {
  constructor(http: HttpClient) {
    super(http, "security/v1/roles");
  }
}
