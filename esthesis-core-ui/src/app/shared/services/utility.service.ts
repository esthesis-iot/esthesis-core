import {Injectable} from "@angular/core";
import {Clipboard} from "@angular/cdk/clipboard";
import {MatSnackBar} from "@angular/material/snack-bar";

@Injectable({
  providedIn: "root"
})
export class UtilityService {
  constructor(private snackBar: MatSnackBar,
    private clipboard: Clipboard) {
  }

  /**
   * Display a success message.
   * @param message The message to display.
   */
  popupSuccess(message: string): void {
    this.snackBar.open(message, "CLOSE", {
      duration: 5000,
      verticalPosition: "top",
      panelClass: "snackbar-green"
    });
  }

  /**
   * Display an error message.
   * @param message The message to display.
   */
  popupError(message: string): void {
    this.snackBar.open(message, "CLOSE", {
      duration: 10000,
      verticalPosition: "top",
      panelClass: "snackbar-red"
    });
  }

  popupErrorWithTraceId(message: string, error: any): void {
    let traceId = "";
    try {
      traceId = "\n\n(trace id: " + error.error.traceId + ")";
    } catch (e) {
      console.log("Could not parse error message to extract trace id.", e, error);
    }
    this.snackBar.open(message + traceId, "CLOSE", {
      duration: 10000,
      verticalPosition: "top",
      panelClass: "snackbar-red"
    });
  }

  popupParsableError(error: any): void {
    let errorMessage = error;
    try {
      errorMessage = error.error.errorMessage + "\n\n" +
        "(trace id: " + error.error.traceId + ")";
    } catch (e) {
      console.log("Could not parse error message.", e, error);
    }
    this.snackBar.open(errorMessage, "CLOSE", {
      duration: 10000,
      verticalPosition: "top",
      panelClass: "snackbar-red"
    });
  }

  /**
   * Display an info message.
   * @param message The message to display.
   */
  popupInfo(message: string): void {
    this.snackBar.open(message, "CLOSE", {
      duration: 10000,
      verticalPosition: "top",
      panelClass: "snackbar-blue"
    });
  }

  /**
   * Performs a deep map on the given object's values.
   * @param obj The object to map.
   * @param cb The function to apply.
   */
  deepMap(obj: any, cb: any) {
    const out = {};

    Object.keys(obj).forEach((k) => {
      let val;

      if (obj[k] !== null && typeof obj[k] === "object") {
        val = this.deepMap(obj[k], cb);
      } else {
        val = cb(obj[k], k);
      }

      // @ts-ignore
      out[k] = val;
    });

    return out;
  }

  copyToClipboard(value: any) {
    this.clipboard.copy(value);
    this.popupSuccess("Value copied to clipboard.");
  }

}
