import {Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {CrudService} from "../services/crud.service";
import {DataflowDto} from "../dto/dataflow/dataflow-dto";

@Injectable({
  providedIn: "root"
})
export class DataflowService extends CrudService<DataflowDto> {

  constructor(http: HttpClient) {
    super(http, "v1/dataflow");
  }

}
