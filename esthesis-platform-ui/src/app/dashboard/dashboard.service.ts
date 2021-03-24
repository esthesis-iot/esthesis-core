import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable, Subject} from "rxjs";
import {AppConstants} from "../app.constants";
import {DashboardWidgetDto} from "../dto/dashboard-widget-dto";

@Injectable({
  providedIn: 'root'
})
export class DashboardService {
  private static resource = `dashboard`;
  // An observable to allow the dashboard to refresh its widgets when widgets are added/removed.
  public refreshDashboardObservable: Subject<number> = new Subject();

  constructor(private http: HttpClient) {
  }

  /**
   * Returns the widgets for the default dashboard of the logged-in user.
   */
  getWidgets(): Observable<Array<DashboardWidgetDto>> {
    return this.http.get<Array<DashboardWidgetDto>>(`${AppConstants.API_ROOT}/dashboard/widget`);
  }

  /**
   * Saves the configuration of a widget.
   * @param widget
   */
  saveWidget(widget: DashboardWidgetDto): Observable<DashboardWidgetDto> {
    return this.http.post<DashboardWidgetDto>(
      `${AppConstants.API_ROOT}/${DashboardService.resource}/widget`, widget);
  }

  /**
   * Deletes a previously configured dashboard widget.
   * @param widgetId
   */
  deleteWidget(widgetId: number) {
    return this.http.delete(`${AppConstants.API_ROOT}/${DashboardService.resource}/widget/${widgetId}`);
  }

  /**
   * Whenever a change in the widgets is done (i.e. widget added or removed), this method will
   * fire up a next-observable value, so that the dashbaord knows to refresh its list of widgets.
   * @param dashboardItemId The id of the widget that caused the refresh.
   */
  refreshDashboard(dashboardItemId: number) {
    this.refreshDashboardObservable.next(dashboardItemId);
  }

  /**
   * Finds a widget.
   * @param widgetId The id of the widget to find.
   */
  getWidget(widgetId: number): Observable<DashboardWidgetDto> {
    return this.http.get<DashboardWidgetDto>(`${AppConstants.API_ROOT}/dashboard/widget/${widgetId}`);
  }

}
