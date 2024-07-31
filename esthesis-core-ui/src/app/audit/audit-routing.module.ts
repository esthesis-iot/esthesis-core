import {NgModule} from "@angular/core";
import {RouterModule, Routes} from "@angular/router";
import {AuditListComponent} from "./audit-list/audit-list.component";
import {AuditViewComponent} from "./audit-view/audit-view.component";
import {auditNameResolver} from "../shared/components/breadcrumb/breadcrumb.resolver";

const routes: Routes = [
  {path: "", component: AuditListComponent, data: {breadcrumb: ""}},
  {path: ":id", component: AuditViewComponent, resolve: {breadcrumb: auditNameResolver}},
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class AuditRoutingModule {
}
