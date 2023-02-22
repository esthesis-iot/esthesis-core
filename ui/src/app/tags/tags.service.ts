import {HttpClient} from "@angular/common/http";
import {Injectable} from "@angular/core";
import {TagDto} from "./dto/tag-dto";
import {CrudService} from "../shared/services/crud.service";

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
}
