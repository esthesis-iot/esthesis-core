import { Util } from "../util/util.js";
import { check } from "k6";

export class Users {
  constructor(page) {
    this.page = page;
    this.util = new Util(page);
    this.btnSidebarUsers = page.locator("app-sidebar a[href='/security/users']");
  }

  async test() {
    await this.btnSidebarUsers.click();
    const dataFetchOK = !await this.util.isSnackbarPresent();
    check(dataFetchOK, { "List users": () => dataFetchOK });
    await this.util.screenshot("users-list.png");
  }
}
