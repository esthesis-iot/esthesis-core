import {NgModule} from "@angular/core";
import {ActivatedRouteSnapshot, RouterModule, Routes} from "@angular/router";
import {TagsListComponent} from "./tags-list/tags-list.component";
import {TagEditComponent} from "./tag-edit/tag-edit.component";

const routes: Routes = [
  {path: "", component: TagsListComponent, data: {breadcrumb: ""}},
  {
    path: ":id", component: TagEditComponent, data: {
      breadcrumb: (route: ActivatedRouteSnapshot) => {
        return "Tag " + route.params['id'];
      }
    }
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class TagsRoutingModule {
}
