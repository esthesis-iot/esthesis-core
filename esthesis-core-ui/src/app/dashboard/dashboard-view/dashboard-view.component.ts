import {Component, HostListener, OnDestroy, OnInit, ViewChild} from "@angular/core";
import {BaseComponent} from "../../shared/components/base-component";
import {DashboardService} from "../dashboard.service";
import {NgxMasonryComponent} from "ngx-masonry";
import {DashboardDto} from "../dto/view-edit/dashboard-dto";
import {UtilityService} from "../../shared/services/utility.service";
import screenfull from "screenfull";
import {SseClient} from "ngx-sse-client";
import {OidcSecurityService} from "angular-auth-oidc-client";
import {HttpHeaders} from "@angular/common/http";
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
  masonryOptions = {
    columnWidth: 100,
    horizontalOrder: true,
  };
  dashboardLoading = true;
  lastEventDate?: Date;
  protected readonly screenfull = screenfull;
  private sseSubscription: Subscription | null = null;
  private subscriptionRefreshHandler?: number;
  private subscriptionId!: string;

  constructor(private utilityService: UtilityService, private dashboardService: DashboardService,
    private sseClient: SseClient, private oidcSecurityService: OidcSecurityService) {
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

  private subscribeToDashboard() {
    if (this.selectedDashboard && this.selectedDashboard.items.length > 0) {
      this.oidcSecurityService.getAccessToken().subscribe((token) => {
        // Set up a refresh subscription handler.
        this.subscriptionRefreshHandler = window.setInterval(() => {
          this.dashboardService.refreshSub(this.subscriptionId).subscribe({
            next: () => {
              console.debug("Refreshed dashboard " + this.selectedDashboard!.id);
            }, error: (error) => {
              this.utilityService.popupErrorWithTraceId("Could not refresh dashboard.", error);
            }
          });
        }, this.appConstants.DASHBOARD.REFRESH_INTERVAL_MINUTES * 60000);

        // Subscribe to SSE events for this dashboard.
        const headers = new HttpHeaders().set("Authorization", `Bearer ${token}`);
        this.sseSubscription = this.sseClient.stream(
          `api/dashboard/v1/sub/${this.selectedDashboard?.id}/${this.subscriptionId}`,
          {keepAlive: true, reconnectionDelay: 3000, responseType: "event"},
          {headers}, "GET").subscribe((event) => {
          if (event.type === "error") {
            const errorEvent = event as ErrorEvent;
            console.error(event, errorEvent.message);
          } else {
            const messageEvent = event as MessageEvent;
            JSON.parse(messageEvent.data).forEach((item: any) => {
              this.dashboardService.sendMessage(item)
            });
            this.lastEventDate = new Date();
          }
        });
      });
    }
  }

  private unsubscribeFromDashboard(): Observable<boolean> {
    console.debug("Unsubscribing from dashboard " + this.selectedDashboard?.id + ".");
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

        console.debug("Unsubscribed from dashboard " + this.selectedDashboard?.id + ".");
      }),
      map(() => true),
      catchError(error => {
        this.utilityService.popupErrorWithTraceId(
          "Could not unsubscribe from dashboard " + this.selectedDashboard?.id + ".",
          error
        );
        return of(false)
      })
    );
  }

  @HostListener("window:beforeunload", ["$event"])
  handleBeforeUnload(event: Event): void {
    this.unsubscribeFromDashboard().subscribe(
      (success) => {
        if (success) {
          console.debug("Unsubscribed from dashboard before window unload.");
        }
      }
    );
  }

  ngOnDestroy() {
    this.unsubscribeFromDashboard().subscribe(
      (success) => {
        if (success) {
          console.debug("Unsubscribed from dashboard on destroy.");
        }
      }
    );
  }

  switchDashboard(id: string) {
    this.unsubscribeFromDashboard().subscribe(
      (success) => {
        if (success) {
          this.subscriptionId = uuidv4();
          this.selectedDashboard = this.ownDashboards.find((dashboard) => dashboard.id === id);
          this.subscribeToDashboard();
        }
      }
    )
  }

  fullscreen(dashboardDiv: HTMLElement) {
    if (screenfull.isEnabled) {
      screenfull.toggle(dashboardDiv).then(() => {
      });
    }
  }

  protected readonly Date = Date;
}
