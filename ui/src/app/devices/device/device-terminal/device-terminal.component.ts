import {AfterViewInit, Component, Input, ViewChild} from "@angular/core";
import {NgTerminal} from "ng-terminal";
import {DeviceTerminalService} from "./device-terminal.service";
import {BaseComponent} from "../../../shared/component/base-component";
import {CommandExecuteRequestDto} from "../../../commands/dto/command-execute-request-dto";

@Component({
  selector: "app-device-terminal",
  templateUrl: "./device-terminal.component.html"
})
export class DeviceTerminalComponent extends BaseComponent implements AfterViewInit {
  @Input() hardwareId!: string;
  @ViewChild("term", {static: true}) terminal!: NgTerminal;
  private command = "";
  private blockInput = false;
  private history: string[] = [];
  private historyPointer = 0;

  constructor(private deviceTerminalService: DeviceTerminalService) {
    super();
  }

  private termColorRed(message: string) {
    return "\u001b[31m" + message + "\u001b[39m";
  }

  private executeCommand() {
    const cmdSplit = this.command.split(" ");
    const cmd: CommandExecuteRequestDto = {
      hardwareIds: this.hardwareId,
      commandType: this.appConstants.DEVICE.COMMAND.TYPE.EXECUTE,
      executionType: this.appConstants.DEVICE.COMMAND.EXECUTION.SYNCHRONOUS,
      command: cmdSplit[0],
      arguments: cmdSplit.slice(1).join("")
    };
    this.blockInput = true;
    this.command = "";
    this.deviceTerminalService.executeCommand(cmd).subscribe({
      next: (next) => {
        if (next != null && next.length === 1) {
          const output = next[0].output.replace(/\n/g, "\n\r");
          this.terminal.write(output + "\n\r");
          this.blockInput = false;
          this.terminal.write("$ ");
        } else {
          this.terminal.write(this.termColorRed("ERROR: Timeout waiting for device to reply.\n"));
          this.terminal.write("\r$ ");
          this.blockInput = false;
        }
      }, error: () => {
        this.terminal.write(this.termColorRed("ERROR: Could not dispatch command to device.\n"));
        this.terminal.write("\r$ ");
        this.blockInput = false;
      }
    });
  }

  private addToHistory(command: string) {
    if (this.history[this.history.length - 1] !== command) {
      this.history.push(command);
    }
  }

  private replaceFromHistory() {
    if (this.historyPointer < 0) {
      this.historyPointer = this.history.length - 1;
    }
    if (this.historyPointer >= this.history.length) {
      this.historyPointer = 0;
    }
    this.terminal.write("\b \b".repeat(this.terminal.underlying.buffer.active.cursorX - 2));
    const historyCommand = this.history[this.historyPointer];
    this.terminal.write(historyCommand);
    this.command = historyCommand;
  }

  ngAfterViewInit(): void {
    this.terminal.onKey().subscribe(e => {
      if (!this.blockInput) {
        const ev = e.domEvent;
        const printable = !ev.altKey && !ev.ctrlKey && !ev.metaKey;

        if (ev.key === "Enter") {
          this.terminal.write("\r\n");
          if (this.command.trim().length > 0) {
            this.addToHistory(this.command);
            this.executeCommand();
          } else {
            this.terminal.write("$ ");
          }
          this.historyPointer = this.history.length;
        } else if (ev.key === "ArrowDown") {
          this.historyPointer++;
          this.replaceFromHistory();
        } else if (ev.key === "ArrowUp") {
          this.historyPointer--;
          this.replaceFromHistory();
        } else if (ev.key === "Backspace") { // BACKSPACE
          // Do not delete the prompt
          if (this.terminal.underlying.buffer.active.cursorX > 2) {
            this.terminal.write("\b \b");
          }
          if (this.command.length > 0) {
            this.command = this.command.substring(0, this.command.length - 1);
          }
        } else if (ev.ctrlKey && (ev.key === "c" || ev.key === "C")) {
          this.command = "";
          this.terminal.write("\r\n$ ");
        } else if (printable) {
          this.terminal.write(e.key);
          this.command += e.key;
        }
      }
    });

    this.terminal.setXtermOptions({
      fontFamily: "\"Cascadia Code\", Menlo, monospace",
      cursorBlink: true
    });

    this.terminal.write("$ ");
  }

}
