import { HttpClient } from "@angular/common/http";
import {Injectable} from "@angular/core";
import {CrudService} from "../shared/services/crud.service";
import {GroupDto} from "./dto/group-dto";

/**
 * A service to provide tags manipulation.
 *
 */
@Injectable({
  providedIn: "root"
})
export class SecurityGroupsService extends CrudService<GroupDto> {
  constructor(http: HttpClient) {
    super(http, "security/v1/groups");
  }
}
