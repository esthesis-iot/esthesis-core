import {Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {CrudService} from "../services/crud.service";
import {DataflowDto} from "../dto/dataflow/dataflow-dto";
import {Observable} from "rxjs";
import {environment} from "../../environments/environment";
import {DockerTagsDto} from "../dto/dataflow/docker-tags";

@Injectable({
  providedIn: "root"
})
export class DataflowService extends CrudService<DataflowDto> {
  private static serviceContext = "v1/dataflow";

  constructor(http: HttpClient) {
    super(http, DataflowService.serviceContext);
  }

  getAvailableTags(dflType: string): Observable<DockerTagsDto> {
    return this.http.get<DockerTagsDto>(
      `${environment.apiPrefix}/${DataflowService.serviceContext}/docker-tags/${dflType}`);
  }

  getNamespaces(): Observable<string[]> {
    return this.http.get<string[]>(
      `${environment.apiPrefix}/${DataflowService.serviceContext}/namespaces`);
  }

}
