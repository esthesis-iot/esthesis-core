import { Util } from "../util/util.js";
import { check } from "k6";

export class Applications {
  constructor(page) {
    this.page = page;
    this.util = new Util(page);
    this.btnSidebarApplications = page.locator(`app-sidebar a[href="/applications"]`);
  }

  async test() {
    await this.btnSidebarApplications.click();
    const dataFetchOK = !await this.util.isSnackbarPresent();
    check(dataFetchOK, { "List applications": () => dataFetchOK });
    await this.util.screenshot("applications-list.png");
  }
}
