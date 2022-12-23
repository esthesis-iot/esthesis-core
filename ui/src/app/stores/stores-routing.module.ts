import {NgModule} from "@angular/core";
import {RouterModule, Routes} from "@angular/router";
import {StoresListComponent} from "./stores-list/stores-list.component";
import {StoreEditComponent} from "./store-edit/store-edit.component";

const routes: Routes = [
  {path: "", component: StoresListComponent},
  {path: ":id", component: StoreEditComponent},
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class StoresRoutingModule {
}
