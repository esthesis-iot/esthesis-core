import {HttpClient} from "@angular/common/http";
import {Injectable} from "@angular/core";
import {TagDto} from "./dto/tag-dto";
import {CrudService} from "../shared/services/crud.service";
import {Observable, of} from "rxjs";
import {AppConstants} from "../app.constants";

/**
 * A service to provide tags manipulation.
 *
 */
@Injectable({
  providedIn: "root"
})
export class TagsService extends CrudService<TagDto> {
  constructor(http: HttpClient) {
    super(http, "tag/v1");
  }

  findByName(names: string): Observable<TagDto[]> {
    if (!names || names.length === 0) {
      return of([]);
    } else {
      return this.http.get<TagDto[]>(
        `${AppConstants.API_ROOT}/${this.endpoint}/find/by-names?names=${names}`
      );
    }
  }
}
