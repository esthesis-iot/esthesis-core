import {HttpClient} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {TagDto} from '../dto/tag-dto';
import {CrudService} from '../services/crud.service';

/**
 * A service to provide tags manipulation.
 *
 */
@Injectable({
  providedIn: 'root'
})
export class TagService extends CrudService<TagDto> {
  constructor(http: HttpClient) {
    super(http, 'v1/tag');
  }
}
