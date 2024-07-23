import {Util} from "../util/util.js";
import {check, fail} from "k6";

export class Devices {
  constructor(page) {
    this.page = page;
    this.util = new Util(page);
    this.btnSidebarDevices = page.locator("app-sidebar a[href='/devices']");
  }

  async test() {
    await this.btnSidebarDevices.click();
    const dataFetchOK = !await this.util.isSnackbarPresent();
    check(dataFetchOK, { "List devices": () => dataFetchOK });
    await this.util.screenshot("devices-list.png");
  }

}
