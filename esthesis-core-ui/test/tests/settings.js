import { Util } from "../util/util.js";
import { check } from "k6";

export class Settings {
  constructor(page) {
    this.page = page;
    this.util = new Util(page);
    this.btnSidebarSettings = page.locator("app-sidebar a[href='/settings']");
  }

  async test() {
    await this.btnSidebarSettings.click();
    const dataFetchOK = !await this.util.isSnackbarPresent();
    check(dataFetchOK, { "List settings": () => dataFetchOK });
    await this.util.screenshot("settings-list.png");
  }
}
