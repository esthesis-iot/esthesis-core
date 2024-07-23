import { Util } from "../util/util.js";
import { check } from "k6";

export class DataFlows {
  constructor(page) {
    this.page = page;
    this.util = new Util(page);
    this.btnSidebarDataFlows = page.locator(`app-sidebar a[href="/dataflow"]`);
  }

  async test() {
    await this.btnSidebarDataFlows.click();
    const dataFetchOK = !await this.util.isSnackbarPresent();
    check(dataFetchOK, { "List dataflows": () => dataFetchOK });
    await this.util.screenshot("dataflows-list.png");
  }
}
