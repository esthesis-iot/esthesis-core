/**
 * Utility class for common functions.
 */
export class Util {
  constructor(page) {
    this.page = page;
    this.snackbar = page.locator("mat-snack-bar-container");
  }

  /**
   * Take a screenshot of the page. Screenshots are enabled when the SCREENSHOTS_ACTIVE
   * environment variable is set to true.
   * @param screenshotName The name of the screenshot file to produce.
   * @returns {Promise<*>} A promise that resolves to the screenshot.
   */
  async screenshot(screenshotName) {
    if (__ENV.SCREENSHOTS_ACTIVE) {
      if (screenshotName === undefined) {
        console.error(
          "No screenshot name provided, a screenshot will not be produced.");
      } else {
        console.log("Taking screenshot " + screenshotName + ".");
        return this.page.screenshot({path: "./screenshots/" + screenshotName});
      }
    }
  }

  /**
   * Check if an element is present in the page.
   * @param locator The locator of the element to check.
   * @param timeout The timeout in milliseconds. If a timeout is not provided,
   * the default timeout is 3000 milliseconds.
   * @returns {Promise<T | boolean>} A promise that resolves to true if the element
   * is present in the page, and false otherwise.
   */
  async isPresent(locator, timeout = 3000) {
    return locator.waitFor({state: "attached", timeout: timeout})
    .then(() => true)
    .catch(() => false);
  }

  /**
   * Checks whether a snackbar is present in the page.
   * @param timeout The timeout in milliseconds. If a timeout is not provided,
   * the default timeout is 3000 milliseconds
   * @returns {Promise<T | boolean>} A promise that resolves to true if the snackbar
   * is present in the page, and false otherwise.
   */
  async isSnackbarPresent(timeout = 3000) {
    return this.isPresent(this.snackbar, timeout);
  }
}
