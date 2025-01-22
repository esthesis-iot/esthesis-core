import {Component, HostListener, OnDestroy, OnInit, ViewChild} from "@angular/core";
import {BaseComponent} from "../../shared/components/base-component";
import {DashboardService} from "../dashboard.service";
import {NgxMasonryComponent, NgxMasonryOptions} from "ngx-masonry";
import {DashboardDto} from "../dto/dashboard-dto";
import {UtilityService} from "../../shared/services/utility.service";
import screenfull from "screenfull";
import {SseClient} from "ngx-sse-client";
import {OidcSecurityService} from "angular-auth-oidc-client";
import {catchError, map, Observable, of, Subscription, tap} from "rxjs";
import {v4 as uuidv4} from "uuid";

@Component({
  selector: "app-dashboard-view",
  templateUrl: "./dashboard-view.component.html"
})
export class DashboardViewComponent extends BaseComponent implements OnInit, OnDestroy {
  @ViewChild(NgxMasonryComponent, {static: false}) masonry!: NgxMasonryComponent;
  selectedDashboard?: DashboardDto;
  ownDashboards: DashboardDto[] = [];
  sharedDashboards: DashboardDto[] = [];
  masonryOptions: NgxMasonryOptions = {
    columnWidth: this.appConstants.DASHBOARD.DEFAULTS.COLUMN_WIDTH,
    horizontalOrder: true
  };
  dashboardLoading = true;
  lastEventDate?: Date;
  protected readonly screenfull = screenfull;
  private sseSubscription: Subscription | null = null;
  private subscriptionRefreshHandler?: number;
  private subscriptionId!: string;
  protected readonly Date = Date;

  constructor(private readonly utilityService: UtilityService,
    private readonly dashboardService: DashboardService, private readonly sseClient: SseClient,
    private readonly oidcSecurityService: OidcSecurityService) {
    super();
  }

  ngOnInit() {
    // Generate a random subscription ID for this session.
    this.subscriptionId = uuidv4();

    // Find all available dashboards.
    this.dashboardService.findAllForCurrentUser().subscribe({
      next: (response) => {
        this.ownDashboards = response;
        this.dashboardService.findShared().subscribe({
          next: (response) => {
            // Remove from shared dashboards the ones that are already in own dashboards.
            this.sharedDashboards = response.filter((shared) => {
              return !this.ownDashboards.some((own) => own.id === shared.id);
            });
          }, error: (error) => {
            this.utilityService.popupErrorWithTraceId("Could not fetch shared dashboards.", error);
          }
        });
        // Find from own dashboards the one that is selected as home.
        this.selectedDashboard = this.ownDashboards.find((dashboard) => dashboard.home);
        // Subscribe to selected dashboard.
        this.subscribeToDashboard();
      }, error: (error) => {
        this.utilityService.popupErrorWithTraceId("Could not fetch user dashboards.", error);
      }, complete: () => {
        this.dashboardLoading = false;
      }
    });
  }

  public isSharedByOthers(): boolean {
    if (!this.selectedDashboard) {
      return false;
    } else {
      return this.sharedDashboards.some((dashboard) => dashboard.id === this.selectedDashboard?.id);
    }
  }

  private subscribeToDashboard() {
    this.logger.logDebug("Subscribing to dashboard " + this.selectedDashboard?.id + ".");
    this.oidcSecurityService.getAccessToken().subscribe((token) => {
      // Set up a refresh subscription handler.
      this.logger.logDebug("Setting up refresh subscription handler for dashboard " + this.selectedDashboard!.id + ".");
      this.subscriptionRefreshHandler = window.setInterval(() => {
        this.dashboardService.refreshSub(this.subscriptionId).subscribe({
          next: () => {
            this.logger.logDebug("Refreshed dashboard " + this.selectedDashboard!.id);
          }, error: (error) => {
            this.logger.logError("Could not refresh dashboard " + this.selectedDashboard!.id, error);
          }
        });
      }, this.appConstants.DASHBOARD.REFRESH_INTERVAL_MINUTES * 60000);

      // Subscribe to SSE events for this dashboard.
      this.logger.logDebug("Subscribing to SSE events for dashboard " + this.selectedDashboard?.id + ".");
      this.sseSubscription = this.sseClient.stream(
        `/api/dashboard/v1/sub/${this.selectedDashboard?.id}/${this.subscriptionId}`,
        {keepAlive: true, reconnectionDelay: 3000, responseType: "event"}).subscribe((event) => {
        if (event.type === "error") {
          const errorEvent = event as ErrorEvent;
          this.logger.logError("Error in SSE event for dashboard " + this.selectedDashboard?.id
            + ": " + errorEvent.message + ".");
        } else {
          const messageEvent = event as MessageEvent;
          const eventData = JSON.parse(messageEvent.data);
          // Uncomment to debug all incoming messages.
          // this.logger.logDebug("Received SSE event for dashboard.", eventData);
          this.dashboardService.sendMessage(eventData);
          this.lastEventDate = new Date();
        }
      });
    });
  }

  private unsubscribeFromDashboard(): Observable<boolean> {
    this.logger.logDebug("Unsubscribing from dashboard " + this.selectedDashboard?.id + ".");
    if (!this.selectedDashboard) {
      return of(true);
    }

    return this.dashboardService.unsub(this.subscriptionId).pipe(
      tap(() => {
        // Clear the subscription refresh handler.
        if (this.subscriptionRefreshHandler) {
          clearInterval(this.subscriptionRefreshHandler);
          this.subscriptionRefreshHandler = undefined;
        }

        // Unsubscribe from SSE events
        this.sseSubscription?.unsubscribe();
        this.sseSubscription = null;

        this.logger.logDebug("Unsubscribed from dashboard " + this.selectedDashboard?.id + ".");
      }),
      map(() => true),
      catchError(error => {
        this.logger.logError("Could not unsubscribe from dashboard " + this.selectedDashboard?.id + ".", error);
        return of(false);
      })
    );
  }

  /**
   * Failsafe to terminate a dashboard subscription before window unload.
   * @param event The beforeunload event.
   */
  @HostListener("window:beforeunload", ["$event"])
  handleBeforeUnload(event: Event): void {
    this.unsubscribeFromDashboard().subscribe(
      (success) => {
        if (success) {
          this.logger.logDebug("Unsubscribed from dashboard before window unload.");
        }
      }
    );
  }

  /**
   * Terminate dashboard subscription when the view changes.
   */
  ngOnDestroy() {
    this.unsubscribeFromDashboard().subscribe(
      (success) => {
        if (success) {
          this.logger.logDebug("Unsubscribed from dashboard on destroy.");
        }
      }
    );
  }

  switchDashboard(id: string) {
    this.unsubscribeFromDashboard().subscribe(
      (success) => {
        if (success) {
          this.subscriptionId = uuidv4();
          if (this.ownDashboards.find((dashboard) => dashboard.id === id)) {
            this.selectedDashboard = this.ownDashboards.find((dashboard) => dashboard.id === id);
          } else if (this.sharedDashboards.find((dashboard) => dashboard.id === id)) {
            this.selectedDashboard = this.sharedDashboards.find((dashboard) => dashboard.id === id);
          }
          this.subscribeToDashboard();
        }
      }
    );
  }

  fullscreen(dashboardDiv: HTMLElement) {
    if (screenfull.isEnabled) {
      screenfull.toggle(dashboardDiv).then(() => {
      });
    }
  }
}
