import {Util} from "../util/util.js";
import {check} from "k6";

export class Command {
  constructor(page) {
    this.page = page;
    this.util = new Util(page);
    this.btnSidebarCommand = page.locator(`app-sidebar a[href="/command"]`);
  }

  async test() {
    await this.btnSidebarCommand.click();
    const dataFetchOK = !await this.util.isSnackbarPresent();
    check(dataFetchOK, { "List commands": () => dataFetchOK });
    await this.util.screenshot("command-list.png");
  }

}
