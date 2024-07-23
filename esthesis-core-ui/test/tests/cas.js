import {Util} from "../util/util.js";
import {check} from "k6";

export class CAs {
  constructor(page) {
    this.page = page;
    this.util = new Util(page);
    this.btnSidebarCAs = page.locator(`app-sidebar a[href="/cas"]`);
  }

  async test() {
    await this.btnSidebarCAs.click();
    const dataFetchOK = !await this.util.isSnackbarPresent();
    check(dataFetchOK, { "List CAs": () => dataFetchOK });
    await this.util.screenshot("cas-list.png");
  }
}
