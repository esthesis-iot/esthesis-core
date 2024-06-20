import {Component, EventEmitter, Input, Output, ViewChild} from "@angular/core";
import {CountdownComponent, CountdownConfig, CountdownEvent} from "ngx-countdown";

@Component({
  selector: "app-live-toggle",
  templateUrl: "./live-toggle.component.html"
})
export class LiveToggleComponent {
  @ViewChild("countdown", {static: false}) private countdown!: CountdownComponent;
  @Output() tick = new EventEmitter<Date>();
  @Input() counter = 10;
  live = false;
  countdownConfig: CountdownConfig = {demand: true, leftTime: this.counter};

  toggleLive() {
    this.live = !this.live;
    if (this.live) {
      this.countdown.begin();
    } else {
      this.countdown.stop();
      this.countdown.restart();
    }
  }

  countDownEvent($event: CountdownEvent) {
    if ($event.action === "done") {
      this.tick.emit(new Date());
      this.countdown.restart();
      this.countdown.begin();
    }
  }
}
