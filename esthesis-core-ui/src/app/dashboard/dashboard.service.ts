import {Injectable} from "@angular/core";
import { HttpClient } from "@angular/common/http";
import {Observable, Subject} from "rxjs";
import {DashboardWidgetDto} from "./dto/dashboard-widget-dto";
import {AppConstants} from "../app.constants";

@Injectable({
  providedIn: "root"
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

  updateWidgetCoordinates(widgetId: number, x: number, y: number, columns: number,
    rows: number): Observable<any> {
    return this.http.put(`${AppConstants.API_ROOT}/dashboard/widget/${widgetId}/${x},${y}/${columns},${rows}`, {});
  }

  /**
   * Fetches the last value for a specific widget.
   * @param widgetId The Id of the widget to fetch its last value.
   */
  getWidgetValue(widgetId: number): Observable<string> {
    return this.http.get<string>(`${AppConstants.API_ROOT}/dashboard/widget/${widgetId}/value`);
  }
}
