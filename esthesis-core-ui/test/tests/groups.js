import { Util } from "../util/util.js";
import { check } from "k6";

export class Groups {
  constructor(page) {
    this.page = page;
    this.util = new Util(page);
    this.btnSidebarGroups = page.locator("app-sidebar a[href='/security/groups']");
  }

  async test() {
    await this.btnSidebarGroups.click();
    const dataFetchOK = !await this.util.isSnackbarPresent();
    check(dataFetchOK, { "List groups": () => dataFetchOK });
    await this.util.screenshot("groups-list.png");
  }
}
