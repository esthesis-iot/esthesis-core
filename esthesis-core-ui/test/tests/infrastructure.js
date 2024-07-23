import { Util } from "../util/util.js";
import { check } from "k6";

export class Infrastructure {
  constructor(page) {
    this.page = page;
    this.util = new Util(page);
    this.btnSidebarInfrastructure = page.locator("app-sidebar a[href='/infrastructure']");
  }

  async test() {
    await this.btnSidebarInfrastructure.click();
    const dataFetchOK = !await this.util.isSnackbarPresent();
    check(dataFetchOK, { "List infrastructure": () => dataFetchOK });
    await this.util.screenshot("infrastructure-list.png");
  }
}
