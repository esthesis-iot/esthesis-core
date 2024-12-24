import {Injectable} from "@angular/core";
import { HttpClient } from "@angular/common/http";
import {CrudService} from "../shared/services/crud.service";
import {Observable} from "rxjs";
import {DataflowDto} from "./dto/dataflow-dto";
import {FormlyFieldConfig} from "@ngx-formly/core";
import {AppConstants} from "../app.constants";

@Injectable({
  providedIn: "root"
})
export class DataflowsService extends CrudService<DataflowDto> {
  private prefix = AppConstants.API_ROOT + "/dataflow/v1";
  // A static instance of this service to be used when defining Formly templates, for populating
  // dynamic field values such as selects. Do not use this instance anywhere else.
  static instance: DataflowsService;

  constructor(http: HttpClient) {
    super(http, "dataflow/v1");
    DataflowsService.instance = this;
  }

  getNamespaces(): Observable<any[]> {
    return this.http.get<string[]>(`${this.prefix}/namespaces`);
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
