import { Injectable } from '@angular/core';
import {CrudService} from '../services/crud.service';
import {CampaignDto} from '../dto/campaign-dto';
import {HttpClient} from '@angular/common/http';
import {AppConstants} from '../app.constants';
import {Observable} from "rxjs";
import {CampaignStatsDto} from "../dto/campaign-stats-dto";

@Injectable({
  providedIn: 'root'
})
export class CampaignsService extends CrudService<CampaignDto>{
  private static resource = `campaign`;

  constructor(http: HttpClient) {
    super(http, CampaignsService.resource);
  }

  public startCampaign(campaignId: number) {
    return this.http.get(
      `${AppConstants.API_ROOT}/${CampaignsService.resource}/${campaignId}/start`);
  }

  public stopCampaign(campaignId: number) {
    return this.http.get(
      `${AppConstants.API_ROOT}/${CampaignsService.resource}/${campaignId}/terminate`);
  }

  pauseCampaign(campaignId: number) {
    return this.http.get(
      `${AppConstants.API_ROOT}/${CampaignsService.resource}/${campaignId}/pause`);
  }

  resumeCampaign(campaignId: number) {
    return this.http.get(
      `${AppConstants.API_ROOT}/${CampaignsService.resource}/${campaignId}/resume`);
  }

  stats(campaignId: any) : Observable<CampaignStatsDto> {
    return this.http.get<CampaignStatsDto>(
      `${AppConstants.API_ROOT}/${CampaignsService.resource}/${campaignId}/stats`);
  }
}
