import {Injectable} from "@angular/core";
import {CrudService} from "../shared/services/crud.service";
import {CampaignDto} from "./dto/campaign-dto";
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {CampaignStatsDto} from "./dto/campaign-stats-dto";
import {environment} from "../../environments/environment";

@Injectable({
  providedIn: "root"
})
export class CampaignsService extends CrudService<CampaignDto> {
  constructor(http: HttpClient) {
    super(http, "campaign/v1");
  }

  public startCampaign(campaignId: string) {
    return this.http.get(
      `${environment.apiPrefix}/campaign/v1/${campaignId}/start`);
  }

  public stopCampaign(campaignId: string) {
    return this.http.get(
      `${environment.apiPrefix}/campaign/v1/${campaignId}/terminate`);
  }

  pauseCampaign(campaignId: string) {
    return this.http.get(
      `${environment.apiPrefix}/campaign/v1/${campaignId}/pause`);
  }

  resumeCampaign(campaignId: string) {
    return this.http.get(
      `${environment.apiPrefix}/campaign/v1/${campaignId}/resume`);
  }

  replicateCampaign(campaignId: string): Observable<CampaignDto> {
    return this.http.get<CampaignDto>(
      `${environment.apiPrefix}/campaign/v1/${campaignId}/replicate`);
  }

  stats(campaignId: any): Observable<CampaignStatsDto> {
    return this.http.get<CampaignStatsDto>(
      `${environment.apiPrefix}/campaign/v1/${campaignId}/stats`);
  }
}
