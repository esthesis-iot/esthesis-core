import {HttpClient} from "@angular/common/http";
import {Injectable} from "@angular/core";
import {CrudService} from "../shared/services/crud.service";
import {UserDto} from "./dto/user-dto";
import {BehaviorSubject, Observable, tap} from "rxjs";
import {AppConstants} from "../app.constants";
import * as _ from "lodash-es";

/**
 * A service to provide tags manipulation.
 *
 */
@Injectable({
  providedIn: "root"
})
export class SecurityService extends CrudService<UserDto> {
  permissionsFetched = false;
  // An event emitter for components that need to be notified when authentication is done.
  // @Output() public authDoneEvent: EventEmitter<any> = new EventEmitter<any>();
  // tslint:disable-next-line:variable-name
  private _authDone = new BehaviorSubject<boolean>(false);
  // tslint:disable-next-line:variable-name
  private _authDone$ = this._authDone.asObservable();

  constructor(http: HttpClient) {
    super(http, "security/v1/users");
  }

  authDone(authResult: boolean) {
    return this._authDone.next(authResult);
  }

  isAuthDone(): Observable<boolean> {
    return this._authDone$;
  }

  getPermissions(): Observable<string[]> {
    // this.permissionsFetched = false;
    if (!this.permissionsFetched) {
      return this.http.get<string[]>(`${AppConstants.API_ROOT}/security/v1/users/user-permissions`)
      .pipe(tap((permissions) => {
        sessionStorage.setItem(AppConstants.SECURITY.SESSION_STORAGE.PERMISSIONS, JSON.stringify(permissions));
        this.permissionsFetched = true;
        // this.log.data("User permissions fetched from remote service:", permissions);
      }));
    } else {
      return new Observable<string[]>(observer => {
        const permissions = JSON.parse(sessionStorage.getItem(AppConstants.SECURITY.SESSION_STORAGE.PERMISSIONS)!);
        // this.log.data("User permissions fetched from session storage:", permissions);
        observer.next(permissions);
        observer.complete();
      });
    }
  }

  isIncluded(policy: string): Observable<boolean> {
    let permissionEvaluation = false;

    return new Observable<boolean>(observer => {
      this.getPermissions().subscribe({
        next: permissions => {
          permissionEvaluation = _.includes(permissions, policy);
          observer.next(permissionEvaluation);
          observer.complete();
        }, error: err => {
          console.log("Could not evaluate user permission.", err);
          observer.error(err);
        }
      });
    });
  }

  isPermitted(category: string, operation: string,
    resourceId?: string | null): Observable<boolean> {
    const ernPrefix = AppConstants.SECURITY.ERN.ROOT + ":" + AppConstants.SECURITY.ERN.SYSTEM
      + ":" + AppConstants.SECURITY.ERN.SUBSYSTEM;
    const allow = AppConstants.SECURITY.PERMISSION.ALLOW;
    const deny = AppConstants.SECURITY.PERMISSION.DENY;
    if (!resourceId) {
      resourceId = "*";
    }
    let permissionEvaluation = false;

    return new Observable<boolean>(observer => {
      this.getPermissions().subscribe({
        next: permissions => {
          permissionEvaluation = (
              _.includes(permissions, `${ernPrefix}:${category}:${resourceId}:${operation}:${allow}`) ||
              _.includes(permissions, `${ernPrefix}:${category}:${resourceId}:*:${allow}`) ||
              _.includes(permissions, `${ernPrefix}:${category}:*:${operation}:${allow}`) ||
              _.includes(permissions, `${ernPrefix}:${category}:*:*:${allow}`) ||
              _.includes(permissions, `${ernPrefix}:*:*:*:${allow}`)) &&
            !(
              _.includes(permissions, `${ernPrefix}:${category}:${resourceId}:${operation}:${deny}`) ||
              _.includes(permissions, `${ernPrefix}:${category}:${resourceId}:*:${deny}`) ||
              _.includes(permissions, `${ernPrefix}:${category}:*:${operation}:${deny}`) ||
              _.includes(permissions, `${ernPrefix}:${category}:*:*:${deny}`) ||
              _.includes(permissions, `${ernPrefix}:*:*:*:${deny}`)
            );
          observer.next(permissionEvaluation);
          observer.complete();
        }, error: err => {
          console.log("Could not evaluate user permission.", err);
          observer.error(err);
        }
      });
    });
  }

  getUsername(): string | null {
    const userData = this.getUserData();
    if (userData) {
      return userData[AppConstants.SECURITY.USERDATA.USERNAME] as string;
    } else {
      return null;
    }
  }

  getFullName(): string | null {
    const userData = this.getUserData();
    if (userData) {
      return userData[AppConstants.SECURITY.USERDATA.FIRST_NAME] + " "
      + userData[AppConstants.SECURITY.USERDATA.LAST_NAME] as string;
    } else {
      return null;
    }
  }

  saveUserData(userData: any) {
    sessionStorage.setItem(AppConstants.SECURITY.SESSION_STORAGE.USERDATA, JSON.stringify(userData));
  }

  getUserData() {
    const userData = sessionStorage.getItem(AppConstants.SECURITY.SESSION_STORAGE.USERDATA);
    if (userData) {
      return JSON.parse(userData);
    } else {
      return null;
    }
  }
}
