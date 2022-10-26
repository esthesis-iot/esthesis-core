import {HttpClient} from "@angular/common/http";
import {Injectable} from "@angular/core";
import {ApplicationDto} from "../dto/application-dto";
import {CrudService} from "../services/crud.service";

/**
 * A service providing functionality for the user of the application, including authentication,
 * authorisation and session management.
 */
@Injectable({
  providedIn: "root"
})
export class ApplicationService extends CrudService<ApplicationDto> {
  constructor(http: HttpClient) {
    super(http, "v1/application");
  }
}
