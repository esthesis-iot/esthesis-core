import {BaseComponent} from "./base-component";
import {SecurityService} from "../../security/security.service";
import {Directive, inject} from "@angular/core";
import {Log} from "ng2-logger/browser";

@Directive()
export class SecurityBaseComponent extends BaseComponent {
  // Security flags.
  allowRead = false;
  allowCreate = false;
  allowWrite = false;
  allowDelete = false;
  category: string;
  objectId?: string | null;
  private log = Log.create("SecurityBaseComponent");
  private readonly securityService: SecurityService;

  constructor(category: string, objectId?: string | null) {
    super();
    this.securityService = inject(SecurityService);
    this.category = category;
    this.objectId = objectId;

    this.log.data(`Initialising security for category '${this.category}' and objectId '${this.objectId}'.`);
    console.log("SECURITY INIT", this.securityService);

    this.allowCreate = this.securityService.isPermitted(category, this.appConstants.SECURITY.OPERATION.CREATE, objectId);
    this.allowRead = this.securityService.isPermitted(category, this.appConstants.SECURITY.OPERATION.READ, objectId);
    this.allowWrite = this.securityService.isPermitted(category, this.appConstants.SECURITY.OPERATION.WRITE, objectId);
    this.allowDelete = this.securityService.isPermitted(category, this.appConstants.SECURITY.OPERATION.DELETE, objectId);
  }

}
