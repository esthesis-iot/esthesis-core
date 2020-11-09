import {AfterViewInit, Component, Input, OnInit, ViewChild} from '@angular/core';
import {NgTerminal} from 'ng-terminal';
import {DeviceTerminalService} from './device-terminal.service';
import {CommandExecuteOrderDto} from '../../../dto/command-execute-order-dto';

@Component({
  selector: 'app-device-terminal',
  templateUrl: './device-terminal.component.html',
  styleUrls: ['./device-terminal.component.scss']
})
export class DeviceTerminalComponent implements OnInit, AfterViewInit {
  @Input() deviceId: number;
  private hardwareId: string;
  @ViewChild('term', {static: true}) terminal: NgTerminal;
  private command = '';
  private blockInput = false;

  constructor(private deviceTerminalService: DeviceTerminalService) {
  }

  ngOnInit(): void {
    this.deviceTerminalService.getHardwareId(this.deviceId).subscribe(
      onNext => {
        // @ts-ignore
        this.hardwareId = onNext.content[0].hardwareId;
      }
    );
  }

  private getReply(requestId: number) {
    this.deviceTerminalService.getReply(requestId).subscribe(onNext => {
      let output = '';
      if (onNext && onNext.payload) {
        output = onNext.payload.replace(/\n/g, '\n\r');
        this.terminal.write(output);
      }
      this.blockInput = false;
      this.terminal.write('$ ');
    });
  }

  private executeCommand() {
    if (this.command.trim().length > 0) {
      const cmd: CommandExecuteOrderDto = {
        arguments: this.command,
        hardwareIds: this.hardwareId,
        command: '',
        description: '',
        tags: ''
      };
      this.blockInput = true;
      // this.terminal.underlying.buffer.active.cursorX = 0;
      this.deviceTerminalService.executeCommand(cmd).subscribe(onNext => {
        this.command = '';
        this.getReply(onNext);
      });
    } else {
      this.terminal.write('$ ');
    }
  }

  ngAfterViewInit(): void {
    this.terminal.keyEventInput.subscribe(e => {
      if (!this.blockInput) {
        const ev = e.domEvent;
        const printable = !ev.altKey && !ev.ctrlKey && !ev.metaKey;

        if (ev.keyCode === 13) {  // ENTER
          this.terminal.write('\r\n');
          this.executeCommand();
        } else if (ev.keyCode === 8) { // BACKSPACE
          // Do not delete the prompt
          if (this.terminal.underlying.buffer.active.cursorX > 2) {
            this.terminal.write('\b \b');
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

    this.terminal.write('$ ');
  }

}
