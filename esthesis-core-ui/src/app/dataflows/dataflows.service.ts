import {Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {CrudService} from "../shared/services/crud.service";
import {Observable} from "rxjs";
import {DataflowDto} from "./dto/dataflow-dto";
import {DockerTagsDto} from "./dto/docker-tags";
import {FormlyFieldConfig} from "@ngx-formly/core";
import {AppConstants} from "../app.constants";

@Injectable({
  providedIn: "root"
})
export class DataflowsService extends CrudService<DataflowDto> {
  private prefix = AppConstants.API_ROOT + "/dataflow/v1";

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

  override save(data: any): Observable<any> {
    return this.http.post(
      `${this.prefix}`, data, {
        headers: {
          "Content-Type": "application/json"
        }
      });
  }

  replaceSelectValues(fields: FormlyFieldConfig[], searchElement: string, values: any[]) {
    fields.forEach(f => {
      if (f.key === searchElement) {
        f.props!.options = values;
      }
      f.fieldGroup?.forEach(fg => {
        if (fg.key === searchElement) {
          fg.props!.options = values;
        }
      });
    });
  }
}
