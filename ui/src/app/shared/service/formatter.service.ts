import {Injectable} from "@angular/core";
import {AppConstants} from "../../app.constants";
import * as moment from "moment";
import * as hd from "human-duration";

/**
 * Utility services for various components.
 */
@Injectable({
  providedIn: "root"
})
export class FormatterService {

  constructor() {
  }

  /**
   * Converts from one date format to another.
   * @param formatter The type of the format to convert to.
   * @param val The source value to convert from.
   */
  format(formatter: string, val: any): string {
    switch (formatter) {
      case AppConstants.FIELD_VALUE_FORMATTER.DATETIME_SHORT:
        return moment(val).format("YYYY-MM-DD HH:mm:ss");
      case AppConstants.FIELD_VALUE_FORMATTER.DATETIME_MEDIUM:
        return moment(val).format("MMM D, YYYY HH:mm:ss");
      case AppConstants.FIELD_VALUE_FORMATTER.DATETIME_LONG:
        return new Date(val).toString();
      case AppConstants.FIELD_VALUE_FORMATTER.DATE_SHORT:
        return moment(val).format("YYYY-MM-DD");
      case AppConstants.FIELD_VALUE_FORMATTER.DATE_MEDIUM:
        return moment(val).format("MMM D, YYYY");
      case AppConstants.FIELD_VALUE_FORMATTER.DATE_LONG:
        return moment(val).format("dddd, MMMM DD, YYYY ");
      case AppConstants.FIELD_VALUE_FORMATTER.DURATION_MSEC:
        return hd.fmt(val).toString(4);
      case AppConstants.FIELD_VALUE_FORMATTER.BYTES_TO_MB:
        return (val / 1024000).toString();
      case AppConstants.FIELD_VALUE_FORMATTER.BYTES_TO_GB:
        return (val / 1024000000).toString();
      case AppConstants.FIELD_VALUE_FORMATTER.FAHRENHEIT_TO_CELCIUS:
        return ((val - 32) / 1.8).toString();
      case AppConstants.FIELD_VALUE_FORMATTER.CELCIUS_TO_FAHRENHEIT:
        return (val * 1.8 + 32).toString();
      default:
        return val;
    }
  }

}
