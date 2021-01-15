import {Injectable} from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import * as _ from 'lodash';

@Injectable({
  providedIn: 'root'
})
export class UtilityService {

  constructor(private snackBar: MatSnackBar, private dialog: MatDialog) {
  }

  /**
   * Display a success message.
   * @param message The message to display.
   */
  popupSuccess(message: string) {
    this.snackBar.open(message, 'CLOSE', {
      duration: 5000,
      verticalPosition: 'top',
      panelClass: 'snackbar-green'
    });
  }

  /**
   * Display an error message.
   * @param message The message to display.
   */
  popupError(message: string) {
    this.snackBar.open(message, 'CLOSE', {
      duration: 10000,
      verticalPosition: 'top',
      panelClass: 'snackbar-red'
    });
  }

  /**
   * Display an info message.
   * @param message The message to display.
   */
  popupInfo(message: string) {
    this.snackBar.open(message, 'CLOSE', {
      duration: 10000,
      verticalPosition: 'top',
      panelClass: 'snackbar-blue'
    });
  }

  /**
   * A utility method to return a sequence of numbers, padded with 0.
   * @param min The minimum number to start with.
   * @param max The maximum number to finish on.
   * @param step The distance between two consecutive numbers.
   */
  getZeroPaddedStringRange(min: number, max: number, step: number): Array<string> {
    return _.range(min, max + 1, step)
      .map(e => {
        return _.padStart(e.toString(), (max+1).toString().length, "0")
      });
  }

}
