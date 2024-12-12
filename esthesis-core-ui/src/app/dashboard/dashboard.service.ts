import {Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {Observable, Subject} from "rxjs";
import {AppConstants} from "../app.constants";
import {CrudService} from "../shared/services/crud.service";
import {DashboardDto} from "./dto/view-edit/dashboard-dto";
import {DashboardUpdateDto} from "./dto/updates/DashboardUpdateDto";

@Injectable({
  providedIn: "root"
})
export class DashboardService extends CrudService<DashboardDto> {
  private static readonly resource = `dashboard/v1`;
  // An emitter for dashboard updates.
  private dashboardEmitter = new Subject<DashboardUpdateDto>();

  constructor(http: HttpClient) {
    super(http, DashboardService.resource);
  }

  sendMessage(message: DashboardUpdateDto) {
    this.dashboardEmitter.next(message);
  }

  getMessage(): Observable<DashboardUpdateDto> {
    return this.dashboardEmitter.asObservable();
  }

  findAllForCurrentUser(): Observable<DashboardDto[]> {
    return this.http.get<DashboardDto[]>(`${AppConstants.API_ROOT}/${DashboardService.resource}/all-for-current-user`);
  }

  findShared(): Observable<DashboardDto[]> {
    return this.http.get<DashboardDto[]>(`${AppConstants.API_ROOT}/${DashboardService.resource}/shared`);
  }

  unsub(subscriptionId: string) {
    return this.http.delete(`${AppConstants.API_ROOT}/${DashboardService.resource}/sub/${subscriptionId}`);
  }

  refreshSub(subscriptionId: string) {
    return this.http.get(`${AppConstants.API_ROOT}/${DashboardService.resource}/refresh-sub/${subscriptionId}`, {});
  }

}
