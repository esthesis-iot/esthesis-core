import {HttpClient} from "@angular/common/http";
import {Injectable} from "@angular/core";
import {CrudService} from "../../shared/services/crud.service";
import {InfrastructureMqttDto} from "./dto/Infrastructure-mqtt-dto";

/**
 * A service to provide tags manipulation.
 *
 */
@Injectable({
  providedIn: "root"
})
export class InfrastructureMqttService extends CrudService<InfrastructureMqttDto> {
  constructor(http: HttpClient) {
    super(http, "infrastructure/mqtt/v1");
  }
}
