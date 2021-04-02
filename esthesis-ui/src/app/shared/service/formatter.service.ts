import {Injectable} from '@angular/core';
import {AppConstants} from '../../app.constants';
import * as moment from 'moment';
import * as hd from 'human-duration';
import {Color} from "@angular-material-components/color-picker";

/**
 * Utility services for various components.
 */
@Injectable({
  providedIn: 'root'
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
        return moment(val).format('YYYY-MM-DD HH:mm:ss');
        break;
      case AppConstants.FIELD_VALUE_FORMATTER.DATETIME_MEDIUM:
        return moment(val).format('MMM D, YYYY HH:mm:ss');
        break;
      case AppConstants.FIELD_VALUE_FORMATTER.DATETIME_LONG:
        return new Date(val).toString();
        break;
      case AppConstants.FIELD_VALUE_FORMATTER.DATE_SHORT:
        return moment(val).format('YYYY-MM-DD');
        break;
      case AppConstants.FIELD_VALUE_FORMATTER.DATE_MEDIUM:
        return moment(val).format('MMM D, YYYY');
        break;
      case AppConstants.FIELD_VALUE_FORMATTER.DATE_LONG:
        return moment(val).format('dddd, MMMM DD, YYYY ');
        break;
      case AppConstants.FIELD_VALUE_FORMATTER.DURATION_MSEC:
        return hd.fmt(val).toString(4);
        break;
      case AppConstants.FIELD_VALUE_FORMATTER.BYTES_TO_MB:
        return (val / 1024000).toString();
        break;
      case AppConstants.FIELD_VALUE_FORMATTER.BYTES_TO_GB:
        return (val / 1024000000).toString();
        break;
      case AppConstants.FIELD_VALUE_FORMATTER.FAHRENHEIT_TO_CELCIUS:
        return ((val - 32) / 1.8).toString();
        break;
      case AppConstants.FIELD_VALUE_FORMATTER.CELCIUS_TO_FAHRENHEIT:
        return (val * 1.8 + 32).toString();
        break;
      default:
        return val;
    }
  }

  /**
   * Converts a string comma-separated value representing a color in RGBA to a Color object.
   * @param color The RGBA value to convert.
   */
  rgbaStringToColor(color: string): Color {
    const cc = color.split(",");
    return new Color(Number(cc[0]), Number(cc[1]), Number(cc[2]), Number(cc[3]));
  }

  /**
   * Converts a Color object to a comma-separated RGBA string representation.
   * @param color The color object to convert from.
   */
  colorToRgbaString(color: Color): string {
    return [color.r, color.g, color.b, color.a].join(",");
  }
}
