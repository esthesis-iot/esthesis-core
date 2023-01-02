import {HttpClient} from "@angular/common/http";
import {Injectable} from "@angular/core";
import {Observable} from "rxjs";
import {AuditDto} from "./dto/audit-dto";
import {environment} from "../../environments/environment";
import {CrudService} from "../services/crud.service";

/**
 * A service providing functionality to view and manage the audit log.
 */
@Injectable({
  providedIn: "root"
})
export class AuditService extends CrudService<AuditDto> {
  private prefix = environment.apiPrefix + "/audit/v1";

  constructor(http: HttpClient) {
    super(http, "audit/v1");
  }

  // Returns the audit logs.
  // getLogs(queryString: string): Observable<QPageableReply<AuditDto>> {
  //   return this.http.get<QPageableReply<AuditDto>>(`${this.prefix}/find?${queryString}`);
  // }

  getCategories(): Observable<string[]> {
    return this.http.get<string[]>(`${this.prefix}/categories`);
  }

  getOperations(): Observable<string[]> {
    return this.http.get<string[]>(`${this.prefix}/operations`);
  }

}
