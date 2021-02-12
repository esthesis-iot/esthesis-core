import { Injectable } from '@angular/core';
import {CrudService} from '../services/crud.service';
import {CampaignDto} from '../dto/campaign-dto';
import {HttpClient} from '@angular/common/http';
import {AppConstants} from '../app.constants';

@Injectable({
  providedIn: 'root'
})
export class CampaignsService extends CrudService<CampaignDto>{
  private static resource = `campaign`;

  constructor(http: HttpClient) {
    super(http, CampaignsService.resource);
  }

  public startCampaign(id: number) {
    return this.http.get(
      `${AppConstants.API_ROOT}/${CampaignsService.resource}/${id}/start`);
  }

  /**
   * A helper method when developing to test elements of the workflow.
   * @param id
   */
  public testWorkflow(id: number) {
    return this.http.get(
      `${AppConstants.API_ROOT}/${CampaignsService.resource}/${id}/test-workflow`);
  }

}
