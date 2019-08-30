import {HttpClient} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {AppConstants} from '../app.constants';
import {CrudService} from '../services/crud.service';
import {QFormsService} from '@eurodyn/forms';
import {CommandRequestDto} from '../dto/command-request-dto';

@Injectable({
  providedIn: 'root'
})
export class ControlService extends CrudService<CommandRequestDto> {
  private resource = `control`;

  constructor(http: HttpClient, qForms: QFormsService) {
    super(http, 'control', qForms);
  }

  getReply(replyId: number): Observable<string> {
    return this.http.get<string>(
      AppConstants.API_ROOT + `/${this.resource}` + '/reply?replyId=${replyId}');
  }
}
