import {Util} from "../util/util.js";
import {check} from "k6";

export class Keystores {
  constructor(page) {
    this.page = page;
    this.util = new Util(page);
    this.btnSidebarKeystores = page.locator("app-sidebar a[href='/keystores']");
  }

  async test() {
    await this.btnSidebarKeystores.click();
    const dataFetchOK = !await this.util.isSnackbarPresent();
    check(dataFetchOK, { "List keystores": () => dataFetchOK });
    await this.util.screenshot("keystores-list.png");
  }
}
