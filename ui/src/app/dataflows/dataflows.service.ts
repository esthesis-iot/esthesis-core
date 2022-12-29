import {Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {CrudService} from "../services/crud.service";
import {Observable} from "rxjs";
import {environment} from "../../environments/environment";
import {DataflowDto} from "./dto/dataflow-dto";
import {DockerTagsDto} from "./dto/docker-tags";

@Injectable({
  providedIn: "root"
})
export class DataflowsService extends CrudService<DataflowDto> {
  private prefix = environment.apiPrefix + "/dataflow/v1";

  constructor(http: HttpClient) {
    super(http, "dataflow/v1");
  }

  getAvailableTags(dflType: string): Observable<DockerTagsDto> {
    return this.http.get<DockerTagsDto>(
      `${this.prefix}/docker-tags/${dflType}`);
  }

  getNamespaces(): Observable<string[]> {
    return this.http.get<string[]>(
      `${this.prefix}/namespaces`);
  }

  save(data: any): Observable<any> {
    return this.http.post(
      `${this.prefix}`, data, {
        headers: {
          "Content-Type": "application/json"
        }
      });
  }
}
