import {Injectable} from "@angular/core";
import {MatDialog} from "@angular/material/dialog";
import {MatSnackBar} from "@angular/material/snack-bar";
import {Log} from "ng2-logger/browser";

@Injectable({
  providedIn: "root"
})
export class UtilityService {
  private log = Log.create("UtilityService");

  constructor(private snackBar: MatSnackBar, private dialog: MatDialog) {
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
    console.log(error);
    let traceId;
    try {
      traceId = "(trace id: " + error.error.traceId + ")";
    } catch (e) {
      this.log.error("Could not parse error message to extract trace id.", e, error);
    }
    this.snackBar.open(message + "\n\n" + traceId, "CLOSE", {
      duration: 10000,
      verticalPosition: "top",
      panelClass: "snackbar-red"
    });
  }

  popupParsableError(error: any): void {
    console.log(error);
    let errorMessage = error;
    try {
      errorMessage = error.error.errorMessage + "\n\n" +
        "(trace id: " + error.error.traceId + ")";
    } catch (e) {
      this.log.error("Could not parse error message.", e, error);
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

}
