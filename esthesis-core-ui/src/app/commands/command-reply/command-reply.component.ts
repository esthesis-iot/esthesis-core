import {Component, OnInit} from "@angular/core";
import {ActivatedRoute, Router} from "@angular/router";
import {CommandsService} from "../commands.service";
import {CommandRequestDto} from "../dto/command-request-dto";
import {CommandReplyDto} from "../dto/command-reply-dto";
import {
  OkCancelModalComponent
} from "../../shared/components/ok-cancel-modal/ok-cancel-modal.component";
import {MatDialog} from "@angular/material/dialog";
import {UtilityService} from "../../shared/services/utility.service";
import {SecurityBaseComponent} from "../../shared/components/security-base-component";
import {AppConstants} from "../../app.constants";

@Component({
  selector: "app-command-reply",
  templateUrl: "./command-reply.component.html"
})
export class CommandReplyComponent extends SecurityBaseComponent implements OnInit {
  id!: string;
  commandRequest?: CommandRequestDto;
  commandReplies?: CommandReplyDto[];

  constructor(private route: ActivatedRoute, private commandService: CommandsService,
    private utilityService: UtilityService, private dialog: MatDialog, private router: Router) {
    super(AppConstants.SECURITY.CATEGORY.COMMAND, route.snapshot.paramMap.get("id"));
  }

  ngOnInit(): void {
    this.id = this.route.snapshot.paramMap.get("id")!;
    this.commandService.findById(this.id).subscribe({
      next: (command) => {
        this.commandRequest = command;
        this.commandService.getReply(command.id!).subscribe({
          next: (replies) => {
            if (replies !== null && replies.length > 0) {
              this.commandReplies = replies;
            }
          }, error: (error) => {
            this.utilityService.popupErrorWithTraceId("Could not fetch command replies.", error);
          }
        });
      }, error: (error) => {
        this.utilityService.popupErrorWithTraceId("Could not fetch command details.", error);
      }
    });
  }

  copyOutputToClipboard(index: number) {
    this.utilityService.copyToClipboard(this.commandReplies?.[index].output);
  }

  deleteCommand() {
    this.dialog.open(OkCancelModalComponent, {
      data: {
        title: "Delete Command",
        question: "Do you really want to delete this command and replies?",
        buttons: {
          ok: true, cancel: true, reload: false
        }
      }
    }).afterClosed().subscribe(result => {
      if (result) {
        this.commandService.deleteCommand(this.id).subscribe({
          next: () => {
            this.utilityService.popupSuccess("Command successfully deleted.");
            this.router.navigate(["command"]);
          }, error: (error) => {
            this.utilityService.popupErrorWithTraceId("Could not delete command.", error);
          }
        });
      }
    });
  }

  deleteReplies() {
    this.dialog.open(OkCancelModalComponent, {
      data: {
        title: "Delete Replies",
        question: "Do you really want to delete all replies for this command?",
        buttons: {
          ok: true, cancel: true, reload: false
        }
      }
    }).afterClosed().subscribe(result => {
      if (result) {
        this.commandService.deleteReplies(this.commandRequest?.id!).subscribe({
          next: () => {
            this.utilityService.popupSuccess("Command reply successfully deleted.");
            this.commandReplies = [];
          }, error: (error) => {
            this.utilityService.popupErrorWithTraceId("Could not delete command reply.", error);
          }
        });
      }
    });
  }

  deleteReply(replyId: string, index: number) {
    this.dialog.open(OkCancelModalComponent, {
      data: {
        title: "Delete Reply",
        question: "Do you really want to delete this reply?",
        buttons: {
          ok: true, cancel: true, reload: false
        }
      }
    }).afterClosed().subscribe(result => {
      if (result) {
        this.commandService.deleteReply(replyId).subscribe({
          next: () => {
            this.utilityService.popupSuccess("Command reply successfully deleted.");
            this.commandReplies?.splice(index, 1);
            if (this.commandReplies?.length === 0) {
              this.commandReplies = undefined;
            }
          }, error: (error) => {
            this.utilityService.popupErrorWithTraceId("Could not delete command reply.", error);
          }
        });
      }
    });
  }
}
