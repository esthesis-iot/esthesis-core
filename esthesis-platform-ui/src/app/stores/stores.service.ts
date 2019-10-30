import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {QFormsService} from '@eurodyn/forms';
import {CrudService} from '../services/crud.service';
import {StoreDto} from '../dto/store-dto';

@Injectable({
  providedIn: 'root'
})
export class StoresService extends CrudService<StoreDto> {

  constructor(http: HttpClient, qForms: QFormsService) {
    super(http, 'stores', qForms);
  }

}
