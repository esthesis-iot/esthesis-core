import {AfterViewInit, Component, Input, ViewChild} from "@angular/core";
import {NgTerminal} from "ng-terminal";
import {DeviceTerminalService} from "./device-terminal.service";

@Component({
  selector: "app-device-terminal",
  templateUrl: "./device-terminal.component.html",
  styleUrls: []
})
export class DeviceTerminalComponent implements AfterViewInit {
  @Input() hardwareId!: string;
  @ViewChild("term", {static: true}) terminal!: NgTerminal;
  private command = "";
  private blockInput = false;

  constructor(private deviceTerminalService: DeviceTerminalService) {
  }


  private termColorRed(message: string) {
    return "\u001b[31m" + message + "\u001b[39m";
  }

  private getReply(requestId: number) {
    this.deviceTerminalService.getReply(requestId).subscribe(
      onNext => {
        let output = "";
        // if (onNext && onNext.payload) {
        //   output = onNext.payload.replace(/\n/g, "\n\r");
        //   this.terminal.write(output);
        // }
        this.blockInput = false;
        this.terminal.write("$ ");
      },
      onError => {
        this.terminal.write(this.termColorRed("ERROR: Timeout waiting for device to reply.\n"));
        this.terminal.write("\r$ ");
        this.blockInput = false;
      }
    );
  }

  private executeCommand() {
    if (this.command.trim().length > 0) {
      const cmd = null;
      // const cmd: CommandExecuteRequestDto = {
      //   // arguments: this.command,
      //   // hardwareIds: this.hardwareId,
      //   // command: "",
      //   // description: "",
      //   // tags: ""
      // };
      this.blockInput = true;
      this.command = "";
      // this.deviceTerminalService.executeCommand(cmd).subscribe(
      //   onNext => {
      //     this.getReply(Object.values(onNext)[0]);
      //   },
      //   onError => {
      //     this.terminal.write(this.termColorRed("ERROR: Could not dispatch command to
      // device.\n")); this.terminal.write("\r$ "); this.blockInput = false; } );
    } else {
      this.terminal.write("$ ");
    }
  }

  ngAfterViewInit(): void {
    this.terminal.keyEventInput.subscribe(e => {
      if (!this.blockInput) {
        const ev = e.domEvent;
        const printable = !ev.altKey && !ev.ctrlKey && !ev.metaKey;

        if (ev.keyCode === 13) {  // ENTER
          this.terminal.write("\r\n");
          this.executeCommand();
        } else if (ev.keyCode === 8) { // BACKSPACE
          // Do not delete the prompt
          if (this.terminal.underlying.buffer.active.cursorX > 2) {
            this.terminal.write("\b \b");
          }
          if (this.command.length > 0) {
            this.command = this.command.substr(0, this.command.length - 1);
          }
        } else if (printable) {
          this.terminal.write(e.key);
          this.command += e.key;
        }
      }
    });

    this.terminal.write("$ ");
  }

}
