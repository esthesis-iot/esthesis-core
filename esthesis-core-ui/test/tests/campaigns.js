import {Util} from "../util/util.js";
import {check} from "k6";

export class Campaigns {
  constructor(page) {
    this.page = page;
    this.util = new Util(page);
    this.btnSidebarCampaigns = page.locator(`app-sidebar a[href="/campaigns"]`);
  }

  async test() {
    await this.btnSidebarCampaigns.click();
    const dataFetchOK = !await this.util.isSnackbarPresent();
    check(dataFetchOK, { "List campaigns": () => dataFetchOK });
    await this.util.screenshot("campaigns-list.png");
  }

}
