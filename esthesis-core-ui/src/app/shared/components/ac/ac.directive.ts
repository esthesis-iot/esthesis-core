import {Directive, Input, TemplateRef, ViewContainerRef} from "@angular/core";
import {SecurityService} from "../../../security/security.service";

/**
 * Add the template content to the DOM unless the condition is true.
 */
@Directive({
  selector: "[ac]"
})
export class AcDirective {
  constructor(
    private readonly templateRef: TemplateRef<any>,
    private readonly viewContainer: ViewContainerRef,
    private readonly securityUsersService: SecurityService) {
  }

  @Input() set ac(conditions: string[]) {
    const category = conditions[0];
    const operation = conditions[1];
    let resourceId = null;
    if (conditions.length === 3) {
      resourceId = conditions[2];
    }

    const isPermitted = this.securityUsersService.isPermitted(category, operation, resourceId);

    if (isPermitted) {
      this.viewContainer.createEmbeddedView(this.templateRef);
    } else if (conditions[0]) {
      this.viewContainer.clear();
    }
  }

}
