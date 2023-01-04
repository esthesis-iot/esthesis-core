import * as _ from "lodash-es";
import {AppConstants} from "../../app.constants";

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

}
