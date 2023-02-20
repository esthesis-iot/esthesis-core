import * as _ from "lodash-es";
import {AppConstants} from "../../app.constants";
import {FormGroup} from "@angular/forms";

export class BaseComponent {

  readonly appConstants = AppConstants;

  /**
   * Utility method to lookup within an object by the value of its keys and return the key name.
   * This is particularly useful when looking up constants.
   * @param obj The object to search on.
   * @param val The value to search for.
   */
  lookupByValue(obj: any, val: any): any {
    return _.startCase((_.invert(obj))[val].replace("_", " ").toLowerCase());
  }

  /**
   * Converts a string by replacing all underscores with spaces and capitalizing the first letter.
   * @param str The string to convert.
   */
  normaliseString(str: string): string {
    return _.capitalize(str.replace("_", " "));
  }

  /**
   * Checks if a field in a FormGroup is valid
   * @param form The FormGroup to check.
   * @param field The name of the field to check.
   */
  isFieldValid(form: FormGroup, field: string): boolean {
    return !form.get(field)?.valid && form.get(field)?.touched || false;
  }
}
