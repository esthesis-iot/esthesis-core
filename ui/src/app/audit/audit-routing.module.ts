import {NgModule} from "@angular/core";
import {RouterModule, Routes} from "@angular/router";
import {AuditListComponent} from "./audit-list/audit-list.component";

const routes: Routes = [
  {path: "", component: AuditListComponent},
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class AuditRoutingModule {
}
