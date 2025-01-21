import {HttpClient} from "@angular/common/http";
import {Injectable} from "@angular/core";
import {CrudService} from "../shared/services/crud.service";
import {PolicyDto} from "./dto/policy-dto";

/**
 * A service to provide tags manipulation.
 *
 */
@Injectable({
  providedIn: "root"
})
export class SecurityPoliciesService extends CrudService<PolicyDto> {
  constructor(http: HttpClient) {
    super(http, "security/v1/policies");
  }
}
