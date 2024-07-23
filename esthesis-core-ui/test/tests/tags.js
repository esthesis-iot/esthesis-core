import { Util } from "../util/util.js";
import { check } from "k6";

export class Tags {
  constructor(page) {
    this.page = page;
    this.util = new Util(page);
    this.btnSidebarTags = page.locator("app-sidebar a[href='/tags']");
  }

  async test() {
    await this.btnSidebarTags.click();
    const dataFetchOK = !await this.util.isSnackbarPresent();
    check(dataFetchOK, { "List tags": () => dataFetchOK });
    await this.util.screenshot("tags-list.png");
  }
}
