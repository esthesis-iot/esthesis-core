import {NgModule} from "@angular/core";
import {RouterModule, Routes} from "@angular/router";
import {AuditListComponent} from "./audit-list/audit-list.component";
import {AuditViewComponent} from "./audit-view/audit-view.component";

const routes: Routes = [
  {path: "", component: AuditListComponent},
  {path: ":id", component: AuditViewComponent},
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class AuditRoutingModule {
}
