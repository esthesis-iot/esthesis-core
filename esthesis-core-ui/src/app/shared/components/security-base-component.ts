import {BaseComponent} from "./base-component";
import {SecurityService} from "../../security/security.service";
import {Directive, inject} from "@angular/core";
import {forkJoin, map, Observable} from "rxjs";

@Directive()
export class SecurityBaseComponent extends BaseComponent {
  // Security flags.
  allowRead: boolean | undefined;
  allowCreate: boolean | undefined;
  allowWrite: boolean | undefined;
  allowDelete: boolean | undefined;
  category: string;
  resourceId?: string | null;
  private readonly securityService: SecurityService;

  constructor(category: string, resourceId?: string | null) {
    super();
    // Manually inject the SecurityService to simplify child class constructors.
    this.securityService = inject(SecurityService);
    // Set the category and resource id.
    this.category = category;
    this.resourceId = resourceId;

    // Evaluate permissions.
    forkJoin({
      create: this.securityService.isPermitted(this.category, this.appConstants.SECURITY.OPERATION.CREATE, this.resourceId),
      read: this.securityService.isPermitted(this.category, this.appConstants.SECURITY.OPERATION.READ, this.resourceId),
      write: this.securityService.isPermitted(this.category, this.appConstants.SECURITY.OPERATION.WRITE, this.resourceId),
      del: this.securityService.isPermitted(this.category, this.appConstants.SECURITY.OPERATION.DELETE, this.resourceId)
    }).subscribe({
      next: ({create, read, write, del}) => {
        this.allowCreate = create;
        this.allowRead = read;
        this.allowWrite = write;
        this.allowDelete = del;
      },
      error: (err) => {
        console.error("Error evaluating permissions", err);
      }
    });
  }

  /**
   * A convenience method to check whether a form should be disabled based on the user's
   * permissions.
   */
  isFormDisabled(): Observable<boolean> {
    return forkJoin({
      create: this.securityService.isPermitted(this.category, this.appConstants.SECURITY.OPERATION.CREATE, this.resourceId),
      write: this.securityService.isPermitted(this.category, this.appConstants.SECURITY.OPERATION.WRITE, this.resourceId)
    }).pipe(
      map(({create, write}) => (this.resourceId && this.resourceId !== this.appConstants.NEW_RECORD_ID && !write) || !create)
    );
  }
}
