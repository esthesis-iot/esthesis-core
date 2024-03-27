import {Pipe, PipeTransform} from "@angular/core";
import * as _ from "lodash-es";

@Pipe({
  name: "titlelise"
})
export class TitlelisePipe implements PipeTransform {

  transform(value: unknown, ...args: unknown[]): unknown {
    let retval = (value as string).replace("_", " ");
    retval = _.startCase(_.camelCase(retval));

    return retval;
  }

}
