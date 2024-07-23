import {Util} from "../util/util.js";
import {check} from "k6";

export class Provisioning {
  constructor(page) {
    this.page = page;
    this.util = new Util(page);
    this.btnSidebarProvisioning = page.locator("app-sidebar a[href='/provisioning']");
  }

  async test() {
    await this.btnSidebarProvisioning.click();
    const dataFetchOK = !await this.util.isSnackbarPresent();
    check(dataFetchOK, { "List provisioning": () => dataFetchOK });
    await this.util.screenshot("provisioning-list.png");
  }

}
