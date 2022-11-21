import {Component, OnInit} from "@angular/core";
import {ActivatedRoute, Router} from "@angular/router";
import {CommandService} from "../command.service";
import {CommandRequestDto} from "../../dto/command-request-dto";
import {CommandReplyDto} from "../../dto/command-reply-dto";
import {UtilityService} from "../../shared/service/utility.service";
import {BaseComponent} from "../../shared/component/base-component";
import {
  OkCancelModalComponent
} from "../../shared/component/display/ok-cancel-modal/ok-cancel-modal.component";
import {MatDialog} from "@angular/material/dialog";

@Component({
  selector: "app-command-reply",
  templateUrl: "./command-reply.component.html",
  styleUrls: ["./command-reply.component.scss"]
})
export class CommandReplyComponent extends BaseComponent implements OnInit {
  id!: string;
  commandRequest!: CommandRequestDto;
  commandReply?: CommandReplyDto;

  constructor(private route: ActivatedRoute, private commandService: CommandService,
    private utilityService: UtilityService, private dialog: MatDialog, private router: Router) {
    super();
  }

  ngOnInit(): void {
    this.id = this.route.snapshot.paramMap.get("id")!;
    this.commandService.findById(this.id).subscribe({
      next: (command) => {
        this.commandRequest = command;
        this.commandService.getReply(command.id!).subscribe({
          next: (reply) => {
            if (reply !== null && reply.length > 0) {
              this.commandReply = reply[0];
            }
          }, error: (error) => {
            this.utilityService.popupErrorWithTraceId("Could not fetch command reply.", error);
          }
        });
      }, error: (error) => {
        this.utilityService.popupErrorWithTraceId("Could not fetch command details.", error);
      }
    });
  }

  copyOutputToClipboard() {
    this.utilityService.copyToClipboard(this.commandReply?.output);
  }

  deleteCommand() {
    this.dialog.open(OkCancelModalComponent, {
      data: {
        title: "Delete Command",
        question: "Do you really want to delete this command together with any potential reply?",
        buttons: {
          ok: true, cancel: true, reload: false
        }
      }
    }).afterClosed().subscribe(result => {
      if (result) {
        this.commandService.deleteCommand(this.id).subscribe({
          next: (next) => {
            this.utilityService.popupSuccess("Command successfully deleted.");
            this.router.navigate(["command"]);
          }, error: (error) => {
            this.utilityService.popupErrorWithTraceId("Could not delete command.", error);
          }
        });
      }
    });
  }

  deleteReply() {
    this.dialog.open(OkCancelModalComponent, {
      data: {
        title: "Delete Reply",
        question: "Do you really want to delete this command reply?",
        buttons: {
          ok: true, cancel: true, reload: false
        }
      }
    }).afterClosed().subscribe(result => {
      if (result) {
        this.commandService.deleteReply(this.commandReply?.id!).subscribe({
          next: (next) => {
            this.utilityService.popupSuccess("Command reply successfully deleted.");
            this.commandReply = undefined;
          }, error: (error) => {
            this.utilityService.popupErrorWithTraceId("Could not delete command reply.", error);
          }
        });
      }
    });
  }
}
