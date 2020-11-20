import {HttpClient} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {TagDto} from '../dto/tag-dto';
import {Observable} from 'rxjs';
import {CrudService} from '../services/crud.service';
import {QFormsService, QPageableReply} from '@eurodyn/forms';

/**
 * A service to provide extra functionality for the tags.
 *
 */
@Injectable({
  providedIn: 'root'
})
export class TagService extends CrudService<TagDto> {
  constructor(http: HttpClient, qForms: QFormsService) {
    super(http, 'tags', qForms);
  }

  getAll(params?: string): Observable<QPageableReply<TagDto>> {
    if (!params) {
      return super.getAll('order=name,asc');
    } else {
      return super.getAll(params);
    }
  }
}
