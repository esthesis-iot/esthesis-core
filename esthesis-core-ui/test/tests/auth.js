import {Util} from "../util/util.js";
import {abort, check, fail} from "k6";
import test from 'k6/execution';

export class Auth {
  constructor(page) {
    this.page = page;
    this.util = new Util(page);
    this.fldUsername = page.locator("input[name='username']");
    this.fldPassword = page.locator("input[name='password']");
    this.btnSubmit = page.locator("input[type='submit']");
    this.btnSubmit = page.locator("input[type='submit']");
    this.spnError = page.locator("span[id='input-error']");
  }

  async test() {
    await this.page.goto("http://localhost:4200");
    await this.page.waitForLoadState("networkidle");
    await this.fldUsername.fill("esthesis-admin");
    await this.fldPassword.fill("esthesis-admin");
    await this.util.screenshot("auth.png");
    await this.btnSubmit.click();
    const authFailed = await this.util.isPresent(this.spnError, 5000);
    check(authFailed, { "Authentication": () => !authFailed });
    if (authFailed) {
      test.test.abort("Authentication failed, aborting tests.");
    }
    await this.page.waitForNavigation();
  }
}
