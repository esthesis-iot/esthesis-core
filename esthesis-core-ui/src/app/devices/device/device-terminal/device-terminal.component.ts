import {AfterViewInit, Component, Input, ViewChild} from "@angular/core";
import {NgTerminal} from "ng-terminal";
import {DeviceTerminalService} from "./device-terminal.service";
import {CommandExecuteRequestDto} from "../../../commands/dto/command-execute-request-dto";
import {SecurityBaseComponent} from "../../../shared/components/security-base-component";
import {AppConstants} from "../../../app.constants";
import {ActivatedRoute} from "@angular/router";

@Component({
  selector: "app-device-terminal",
  templateUrl: "./device-terminal.component.html"
})
export class DeviceTerminalComponent extends SecurityBaseComponent implements AfterViewInit {
  @Input() hardwareId!: string;
  @ViewChild("term", {static: true}) terminal!: NgTerminal;
  counters = {
    latency: 0,
    latestCommand: new Date(),
    latestReply: new Date(),
    bytesSent: 0,
    bytesReceived: 0
  };
  timeout = 3000;
  polling = 500;
  private command = "";
  private blockInput = false;
  private history: string[] = [];
  private historyPointer = 0;
  private winX: number;

  constructor(private deviceTerminalService: DeviceTerminalService, private route: ActivatedRoute) {
    super(AppConstants.SECURITY.CATEGORY.DEVICE, route.snapshot.paramMap.get("id"));
    this.winX = window.innerWidth;
  }

  ngAfterViewInit(): void {
    // Configuration options.
    this.terminal.setXtermOptions({
      cursorBlink: true,
    });

    // Keypress handling.
    this.terminal.onKey().subscribe(e => {
      if (this.blockInput) {
        return;
      }
      const ev = e.domEvent;
      const printable = !ev.altKey && !ev.ctrlKey && !ev.metaKey;

      switch (ev.key) {
        case "Enter":
          this.terminal.write("\r\n");
          if (this.command.trim().length > 0) {
            this.addToHistory(this.command);
            this.executeCommand();
          } else {
            this.terminal.write("$ ");
          }
          this.historyPointer = this.history.length;
          break;
        case "ArrowDown":
          this.historyPointer++;
          this.replaceFromHistory();
          break;
        case "ArrowUp":
          this.historyPointer--;
          this.replaceFromHistory();
          break;
        case "Backspace":
          // Do not delete the prompt
          if (this.terminal.underlying!.buffer.active.cursorX > 2) {
            this.terminal.write("\b \b");
          }
          if (this.command.length > 0) {
            this.command = this.command.substring(0, this.command.length - 1);
          }
          break;
        default:
          if (ev.ctrlKey && (ev.key === "c" || ev.key === "C")) {
            this.command = "";
            this.terminal.write("\r\n$ ");
          } else if (printable) {
            this.terminal.write(e.key);
            this.command += e.key;
          }
      }
    });

    this.terminal.write("$ ");
  }

  private termColorRed(message: string) {
    return "\u001b[31m" + message + "\u001b[39m";
  }

  private executeCommand() {
    // Normalize multiple spaces into one
    this.command = this.command.replace(/\s+/g, ' ').trim();

    const cmdSplit = this.command.split(" ");
    const cmd: CommandExecuteRequestDto = {
      hardwareIds: this.hardwareId,
      commandType: this.appConstants.DEVICE.COMMAND.TYPE.EXECUTE,
      executionType: this.appConstants.DEVICE.COMMAND.EXECUTION.SYNCHRONOUS,
      command: cmdSplit[0],
      arguments: cmdSplit.slice(1).join(" ")
    };
    this.blockInput = true;
    this.command = "";
    this.counters.latestCommand = new Date();
    this.counters.bytesSent += cmd.command.length + cmd.arguments.length;
    this.deviceTerminalService.executeCommandWithParams(cmd, this.polling, this.timeout).subscribe({
      next: (next) => {
        this.counters.latestReply = new Date();
        this.counters.latency = this.counters.latestReply.getTime() - this.counters.latestCommand.getTime();
        if (next != null && next.length === 1) {
          this.counters.bytesReceived += next[0].output.length;
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
    this.terminal.write("\b \b".repeat(this.terminal.underlying!.buffer.active.cursorX - 2));
    const historyCommand = this.history[this.historyPointer];
    this.terminal.write(historyCommand);
    this.command = historyCommand;
  }
}
