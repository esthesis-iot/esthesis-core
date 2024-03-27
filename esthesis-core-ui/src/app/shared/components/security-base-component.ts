import {BaseComponent} from "./base-component";
import {SecurityService} from "../../security/security.service";
import {Directive, inject} from "@angular/core";

@Directive()
export class SecurityBaseComponent extends BaseComponent {
  // Security flags.
  allowRead = false;
  allowCreate = false;
  allowWrite = false;
  allowDelete = false;
  category: string;
  objectId?: string | null;
  private readonly securityService: SecurityService;

  constructor(category: string, objectId?: string | null) {
    super();
    this.securityService = inject(SecurityService);
    this.category = category;
    this.objectId = objectId;

    this.securityService.isPermitted(category, this.appConstants.SECURITY.OPERATION.CREATE, objectId).subscribe({
      next: (result) => {
        this.allowCreate = result;
      }
    });

    this.securityService.isPermitted(category, this.appConstants.SECURITY.OPERATION.READ, objectId).subscribe({
      next: (result) => {
        this.allowRead = result;
      }
    });

    this.securityService.isPermitted(category, this.appConstants.SECURITY.OPERATION.WRITE, objectId).subscribe({
      next: (result) => {
        this.allowWrite = result;
      }
    });

    this.securityService.isPermitted(category, this.appConstants.SECURITY.OPERATION.DELETE, objectId).subscribe({
      next: (result) => {
        this.allowDelete = result;
      }
    });
  }

}
