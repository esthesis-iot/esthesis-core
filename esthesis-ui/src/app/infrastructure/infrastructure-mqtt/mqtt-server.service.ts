import {HttpClient} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {MqttServerDto} from '../../dto/mqtt-server-dto';
import {CrudService} from '../../services/crud.service';
import {QFormsService} from '@eurodyn/forms';

@Injectable({
  providedIn: 'root'
})
export class MqttServerService extends CrudService<MqttServerDto> {
  constructor(http: HttpClient, qForms: QFormsService) {
    super(http, 'mqtt-server', qForms);
  }
}
