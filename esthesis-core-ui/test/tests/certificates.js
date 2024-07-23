import {Util} from "../util/util.js";
import {check} from "k6";

export class Certificates {
  constructor(page) {
    this.page = page;
    this.util = new Util(page);
    this.btnSidebarCertificates = page.locator(`app-sidebar a[href="/certificates"]`);
  }

  async test() {
    await this.btnSidebarCertificates.click();
    const dataFetchOK = !await this.util.isSnackbarPresent();
    check(dataFetchOK, { "List certificates": () => dataFetchOK });
    await this.util.screenshot("certificates-list.png");
  }

}
