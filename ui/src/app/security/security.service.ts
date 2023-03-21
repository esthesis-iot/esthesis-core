import {HttpClient} from "@angular/common/http";
import {Injectable} from "@angular/core";
import {CrudService} from "../shared/services/crud.service";
import {UserDto} from "./dto/user-dto";
import {Observable} from "rxjs";
import {environment} from "../../environments/environment";
import {AppConstants} from "../app.constants";
import * as _ from "lodash";
import {Log} from "ng2-logger/browser";

/**
 * A service to provide tags manipulation.
 *
 */
@Injectable({
  providedIn: "root"
})
export class SecurityService extends CrudService<UserDto> {
  // Logger.
  private log = Log.create("SecurityUsersService");

  constructor(http: HttpClient) {
    super(http, "security/v1/users");
  }

  fetchPermissions(): Observable<string[]> {
    const username = this.getUsername();
    return this.http.get<string[]>(`${environment.apiPrefix}/security/v1/users/${username}/permissions`);
  }

  savePermissions(permissions: string[]) {
    sessionStorage.setItem(AppConstants.SECURITY.SESSION_STORAGE.PERMISSIONS, JSON.stringify(permissions));
  }

  getPermissions() {
    const permissions = sessionStorage.getItem(AppConstants.SECURITY.SESSION_STORAGE.PERMISSIONS);
    if (permissions) {
      return JSON.parse(permissions);
    } else {
      return null;
    }
  }

  isPermitted(category: string,  operation: string, resourceId?: string | null): boolean {
    const ernPrefix = AppConstants.SECURITY.ERN.ROOT + ":" + AppConstants.SECURITY.ERN.SYSTEM
      + ":" + AppConstants.SECURITY.ERN.SUBSYSTEM;
    const allow = AppConstants.SECURITY.PERMISSION.ALLOW;
    const deny = AppConstants.SECURITY.PERMISSION.DENY;
    if (!resourceId) {
      resourceId = "*";
    }

    const permissionToCheck = `${ernPrefix}:${category}:${resourceId}:${operation}`;
    this.log.data(`Evaluating ${permissionToCheck}`);

    const isPermitted = (
      _.includes(this.getPermissions(), `${ernPrefix}:${category}:${resourceId}:${operation}:${allow}`) ||
      _.includes(this.getPermissions(), `${ernPrefix}:${category}:${resourceId}:*:${allow}`) ||
      _.includes(this.getPermissions(), `${ernPrefix}:${category}:*:*:${allow}`) ||
      _.includes(this.getPermissions(), `${ernPrefix}:*:*:*:${allow}`)) &&
      !(
      _.includes(this.getPermissions(), `${ernPrefix}:${category}:${resourceId}:${operation}:${deny}`) ||
      _.includes(this.getPermissions(), `${ernPrefix}:${category}:${resourceId}:*:${deny}`) ||
      _.includes(this.getPermissions(), `${ernPrefix}:${category}:*:*:${deny}`) ||
      _.includes(this.getPermissions(), `${ernPrefix}:*:*:*:${deny}`)
      );
    this.log.data(`${permissionToCheck} evaluated to ${isPermitted}`);

    return isPermitted;
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
