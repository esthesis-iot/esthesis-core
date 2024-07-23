import { Util } from "../util/util.js";
import { check } from "k6";

export class Policies {
  constructor(page) {
    this.page = page;
    this.util = new Util(page);
    this.btnSidebarPolicies = page.locator("app-sidebar a[href='/security/policies']");
  }

  async test() {
    await this.btnSidebarPolicies.click();
    const dataFetchOK = !await this.util.isSnackbarPresent();
    check(dataFetchOK, { "List policies": () => dataFetchOK });
    await this.util.screenshot("policies-list.png");
  }
}
