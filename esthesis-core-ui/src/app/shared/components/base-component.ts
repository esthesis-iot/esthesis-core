import * as _ from "lodash-es";
import {AppConstants} from "../../app.constants";
import {FormGroup} from "@angular/forms";
import {inject} from "@angular/core";
import {LumberjackService} from "@ngworker/lumberjack";
import {catchError, concat, map, Observable, of, Subject, switchMap, tap} from "rxjs";
import {TagDto} from "../../tags/dto/tag-dto";
import {TagsService} from "../../tags/tags.service";
import {debounceTime, distinctUntilChanged} from "rxjs/operators";

export abstract class BaseComponent {
  protected readonly appConstants = AppConstants;
  protected readonly logger = inject(LumberjackService);
  protected readonly tagsService = inject(TagsService);

  // Tag filtering support.
  filteredTags$?: Observable<TagDto[]>;
  filteredTagsLoading = false;
  filteredTagsInput$ = new Subject<string>();
  tagsLoading = false;
  tagsPage = 0;

    /**
     * Tag filtering support.
     * @param selected The selected tags to display initially.
     */
    protected loadFilteredTags(selected: TagDto[]) {
    this.filteredTags$ = concat(
      of(selected),
      this.filteredTagsInput$.pipe(
        distinctUntilChanged(),
        debounceTime(500),
        tap(() => (this.filteredTagsLoading = true)),
        switchMap((term) =>
          this.tagsService.find(`name${term}&page=${this.tagsPage}&size=20&sort=name,asc`).pipe(
            catchError(() => of([])),
            map((tags: any) => tags.content),
            tap(() => (this.filteredTagsLoading = false)),
          ),
        ),
      ),
    );
  }
  tagsTrackByFn(item: TagDto) {
    return item.id;
  }
  onScrollTags($event: { start: number; end: number }) {
    console.log("start " + $event.start + " end " + $event.end);
    this.tagsPage++;
    this.loadFilteredTags([]);
  }
  onScrollToEndTags() {
  }

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
