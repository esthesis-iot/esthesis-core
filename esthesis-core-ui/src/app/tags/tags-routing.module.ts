import {NgModule} from "@angular/core";
import {RouterModule, Routes} from "@angular/router";
import {TagsListComponent} from "./tags-list/tags-list.component";
import {TagEditComponent} from "./tag-edit/tag-edit.component";
import {tagNameResolver} from "../shared/components/breadcrumb/breadcrumb.resolver";

const routes: Routes = [
  {path: "", component: TagsListComponent, data: {breadcrumb: ""}},
  {
    path: ":id", component: TagEditComponent,
    resolve: {
      breadcrumb: tagNameResolver
    }
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class TagsRoutingModule {
}
