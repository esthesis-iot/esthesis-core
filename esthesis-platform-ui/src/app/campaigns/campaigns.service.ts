import { Injectable } from '@angular/core';
import {CrudService} from '../services/crud.service';
import {CampaignDto} from '../dto/campaign-dto';
import {HttpClient} from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class CampaignsService extends CrudService<CampaignDto>{

  constructor(http: HttpClient) {
    super(http, 'campaign');
  }

}
