import { Util } from "../util/util.js";
import { check } from "k6";

export class Audit {
  constructor(page) {
    this.page = page;
    this.util = new Util(page);
    this.btnSidebarAudit = page.locator(`a[href="/audit"]`);
  }

  async test() {
    await this.btnSidebarAudit.click();
    const dataFetchOK = !await this.util.isSnackbarPresent();
    check(dataFetchOK, { "Audit logs": () => dataFetchOK });
    await this.util.screenshot("audit-list.png");
  }
}
