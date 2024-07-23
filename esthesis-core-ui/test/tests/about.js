import { Util } from "../util/util.js";
import { check } from "k6";

export class About {
  constructor(page) {
    this.page = page;
    this.util = new Util(page);
    this.btnSidebarAbout = page.locator(`app-sidebar a[href="/about"]`);
  }

  async test() {
    await this.btnSidebarAbout.click();
    const dataFetchOK = !await this.util.isSnackbarPresent();
    check(dataFetchOK, { "About page": () => dataFetchOK });
    await this.util.screenshot("about-page.png");
  }
}
