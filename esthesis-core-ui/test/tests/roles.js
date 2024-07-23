import { Util } from "../util/util.js";
import { check } from "k6";

export class Roles {
  constructor(page) {
    this.page = page;
    this.util = new Util(page);
    this.btnSidebarRoles = page.locator("app-sidebar a[href='/security/roles']");
  }

  async test() {
    await this.btnSidebarRoles.click();
    const dataFetchOK = !await this.util.isSnackbarPresent();
    check(dataFetchOK, { "List roles": () => dataFetchOK });
    await this.util.screenshot("roles-list.png");
  }
}
