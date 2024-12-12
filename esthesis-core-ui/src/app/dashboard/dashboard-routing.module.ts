import {NgModule} from "@angular/core";
import {RouterModule, Routes} from "@angular/router";
import {DashboardViewComponent} from "./dashboard-view/dashboard-view.component";
import {DashboardEditComponent} from "./dashboard-edit/dashboard-edit.component";
import {dashboardNameResolver} from "../shared/components/breadcrumb/breadcrumb.resolver";
import {DashboardItemNewComponent} from "./dashboard-item-new/dashboard-item-new.component";

const routes: Routes = [
  {path: "", component: DashboardViewComponent, data: {breadcrumb: ""}},
  {path: ":id", component: DashboardEditComponent,
    resolve: {
      breadcrumb: dashboardNameResolver
    }
  },
  {path: ":id/new-item", component: DashboardItemNewComponent,
    data: {breadcrumb: ""}
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class DashboardRoutingModule {
}
