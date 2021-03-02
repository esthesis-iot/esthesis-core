package esthesis.platform.backend.server.resource;

import com.eurodyn.qlack.common.exception.QExceptionWrapper;
import com.eurodyn.qlack.util.data.exceptions.ExceptionWrapper;
import com.eurodyn.qlack.util.data.filter.ReplyFilter;
import com.eurodyn.qlack.util.data.filter.ReplyPageableFilter;
import com.eurodyn.qlack.util.querydsl.EmptyPredicateCheck;
import com.querydsl.core.types.Predicate;
import esthesis.platform.backend.server.config.AppConstants;
import esthesis.platform.backend.server.config.AppConstants.Campaign.Condition;
import esthesis.platform.backend.server.config.AppConstants.Campaign.Condition.Op;
import esthesis.platform.backend.server.config.AppConstants.Campaign.Condition.Stage;
import esthesis.platform.backend.server.config.AppConstants.Campaign.State;
import esthesis.platform.backend.server.config.AppConstants.Campaign.Type;
import esthesis.platform.backend.server.dto.CampaignConditionDTO;
import esthesis.platform.backend.server.dto.CampaignDTO;
import esthesis.platform.backend.server.dto.CampaignStatsDTO;
import esthesis.platform.backend.server.dto.CampaignValidationErrorDTO;
import esthesis.platform.backend.server.model.Campaign;
import esthesis.platform.backend.server.service.CampaignService;
import esthesis.platform.backend.server.service.WorkflowService;
import java.time.Instant;
import java.util.Objects;
import java.util.stream.Stream;
import javax.validation.Valid;
import lombok.extern.java.Log;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Campaigns management endpoints.
 */
@Log
@Validated
@RestController
@RequestMapping("/campaign")
public class CampaignResource {

  private final CampaignService campaignService;
  private final WorkflowService workflowService;

  public CampaignResource(CampaignService campaignService,
    WorkflowService workflowService) {

    this.campaignService = campaignService;
    this.workflowService = workflowService;
  }

  /**
   * A parent method to call all custom validations on the campaign object.
   *
   * @param campaignDTO The campaign DTO to validate.
   * @return Returns a list of validation errors.
   */
  private CampaignValidationErrorDTO validate(CampaignDTO campaignDTO) {
    CampaignValidationErrorDTO err = new CampaignValidationErrorDTO();
    validateMain(err, campaignDTO);
    validateConditions(err, campaignDTO);

    return err;
  }

  /**
   * Validates the main parameters of the campaign (i.e. name, type, etc.)
   *
   * @param err The error object to augment with validation errors.
   * @param dto The campaign DTO to validate
   */
  private void validateMain(CampaignValidationErrorDTO err, CampaignDTO dto) {
    if (StringUtils.isEmpty(dto.getName())) {
      err.addMainError("Campaign name can not be empty.");
    }

    if (dto.getType() == null) {
      err.addMainError("Campaign type can not be empty.");
    } else {
      if (dto.getType() == Type.COMMAND) {
        if (StringUtils.isEmpty(dto.getCommandName())) {
          err.addMainError("Command name can not be empty");
        }
      }

      if (dto.getType() == Type.PROVISIONING) {
        if (StringUtils.isEmpty(dto.getProvisioningPackageId())) {
          err.addMainError("Provisioning package can not be empty");
        }
      }
    }
  }

  /**
   * Checks if a String represents a number or a percentage.
   *
   * @param value The value to check.
   * @return True if the given text is a number or a percentage, false otherwise.
   */
  private boolean isNumberOrPercentage(String value) {
    if (value.endsWith("%")) {
      return StringUtils.isNumeric(value.substring(0, value.length() - 1));
    } else {
      return StringUtils.isNumeric(value);
    }
  }

  /**
   * Validate the conditions of a campaign.
   *
   * @param err The error object to augment with validation errors.
   * @param dto The campaign DTO to validate
   */
  private void validateConditions(CampaignValidationErrorDTO err, CampaignDTO dto) {

    // ************************************************************************
    // Global checks.
    // ************************************************************************
    // Check only a single BATCH type exists per target.
    dto.getConditions().stream().map(CampaignConditionDTO::getTarget).distinct()
      .filter(Objects::nonNull).forEach(target -> {
      if (dto.getConditions().stream().filter(condition ->
        Objects.equals(condition.getType(), Condition.Type.BATCH)
          && Objects.equals(condition.getTarget(), target)).count() > 1) {
        if (target == 0) {
          err.addConditionsError(
            "No more than 1 Batch conditions are allowed for Global level.");
        } else {
          err.addConditionsError(
            "No more than 1 Batch conditions are allowed for Group " + target + ".");
        }
      }
    });

    // Check only a single SUCCESS type exists per target.
    dto.getConditions().stream().map(CampaignConditionDTO::getTarget).distinct()
      .filter(Objects::nonNull).forEach(target -> {
      if (dto.getConditions().stream().filter(condition ->
        Objects.equals(condition.getType(), Condition.Type.SUCCESS)
          && Objects.equals(condition.getTarget(), target)).count() > 1) {
        err.addConditionsError(
          "No more than 1 Success conditions are allowed for Group " + target + ".");
      }
    });

    // Check only a single PAUSE type exists per target & stage.
    dto.getConditions().stream().filter(condition ->
      condition.getType() == Condition.Type.PAUSE)
      .map(CampaignConditionDTO::getTarget).distinct()
      .filter(Objects::nonNull).forEach(target -> {
      Stream.of(Stage.ENTRY, Stage.EXIT).forEach(stage -> {
        if (dto.getConditions().stream().filter(Objects::nonNull)
          .filter(condition ->
            condition.getType() == Condition.Type.PAUSE)
          .filter(condition ->
            condition.getTarget().intValue() == target
              && condition.getStage().intValue() == stage).count() > 1) {
          if (target == 0) {
            err.addConditionsError(
              "No more than 1 Pause conditions are allowed for Global level, stage " +
                AppConstants.Campaign.Condition.Stage.of(stage) + ".");
          } else {
            err.addConditionsError(
              "No more than 1 Pause conditions are allowed for Group " + target + ", stage "
                + AppConstants.Campaign.Condition.Stage.of(stage) + ".");
          }
        }
      });
    });

    // ************************************************************************
    // Condition-specific checks.
    // ************************************************************************
    int index = 1;
    for (CampaignConditionDTO condition : dto.getConditions()) {
      Integer cTarget = condition.getTarget();
      Integer cType = condition.getType();
      String cValue = condition.getValue();
      Integer cStage = condition.getStage();
      Integer cOperation = condition.getOperation();
      Instant cScheduleDate = condition.getScheduleDate();
      String cPropertyName = condition.getPropertyName();

      // Condition has target.
      if (cTarget == null) {
        err.addConditionsError(index, "Target can not be empty.");
      }

      // BATCH condition checks.
      if (cType == Condition.Type.BATCH) {
        // Check value is numeric or percentage.
        if (StringUtils.isEmpty(cValue)) {
          err.addConditionsError(index,
            "Value can not be empty, enter a number or percentage (e.g. 23 or 23%).");
        } else {
          if (!isNumberOrPercentage(cValue)) {
            err.addConditionsError(index,
              "Value is not correct, enter a number or percentage (e.g. 23 or 23%).");
          }
        }
      }

      // DATETIME checks.
      if (cType == Condition.Type.DATETIME) {
        if (cStage == null) {
          err.addConditionsError(index, "Stage can not be empty.");
        }
        if (cOperation == null) {
          err.addConditionsError(index, "Operation can not be empty.");
        }
        if (cScheduleDate == null) {
          err.addConditionsError(index, "Date can not be empty.");
        }
      }

      // SUCCESS checks.
      if (cType == Condition.Type.SUCCESS) {
        if (StringUtils.isEmpty(cValue)) {
          err.addConditionsError(index,
            "Value can not be empty, enter a number or percentage (e.g. 23 or 23%).");
        } else {
          if (!isNumberOrPercentage(cValue)) {
            err.addConditionsError(index,
              "Value is not correct, enter a number or percentage (e.g. 23 or 23%).");
          }
        }
      }

      // PAUSE checks.
      if (cType == Condition.Type.PAUSE) {
        if (cStage == null) {
          err.addConditionsError(index, "Stage can not be empty.");
        }
        if (cOperation == null) {
          err.addConditionsError(index, "Operation can not be empty.");
        }
        if (condition.getOperation() == Op.TIMER_MINUTES) {
          if (StringUtils.isEmpty(cValue)) {
            err.addConditionsError(index, "Value can not be empty.");
          } else if (!StringUtils.isNumeric(cValue)) {
            err.addConditionsError(index, "Value should be a number.");
          }
        }
      }

      // PROPERTY checks.
      if (cType == Condition.Type.PROPERTY) {
        if (cStage == null) {
          err.addConditionsError(index, "Stage can not be empty.");
        }
        if (cOperation == null) {
          err.addConditionsError(index, "Operation can not be empty.");
        }
        if (StringUtils.isEmpty(cPropertyName)) {
          err.addConditionsError(index, "Property name can not be empty.");
        }
        if (StringUtils.isEmpty(cValue)) {
          err.addConditionsError(index, "Value can not be empty.");
        }
        if (StringUtils.isEmpty(cPropertyName) || cPropertyName.split("\\.").length != 3) {
          err.addConditionsError(index,
            "Property name should be in the form of x.y.z, e.g. telemetry.health.temperature.");
        }
      }
      index++;
    }
  }

  /**
   * Saves a campaign.
   *
   * @param campaignDTO
   * @return Returns validation errors for the form, or the Id of the newly created campaign.
   */
  @PostMapping()
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not save campaign.")
  public ResponseEntity save(
    @Valid @RequestBody CampaignDTO campaignDTO) {
    final CampaignValidationErrorDTO errors = validate(campaignDTO);

    if (errors.hasValidationErrors()) {
      return ResponseEntity.badRequest().body(errors);
    } else {
      return ResponseEntity.ok().body(campaignService.save(campaignDTO).getId());
    }
  }

  /**
   * Deletes a campaign.
   *
   * @param campaignId The Id of the campaign to delete.
   */
  @DeleteMapping(path = "{campaignId}", produces = MediaType.APPLICATION_JSON_VALUE)
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not delete campaign.")
  public void delete(@PathVariable long campaignId) {
    String processInstanceId = campaignService.delete(campaignId);
    workflowService.delete(processInstanceId);
  }

  /**
   * Returns the details of a campaign.
   *
   * @param campaignId The Id of the campaign to retrieve.
   */
  @ReplyFilter("-createdBy,-createdOn,-modifiedBy,-modifiedOn")
  @GetMapping(path = "{campaignId}", produces = MediaType.APPLICATION_JSON_VALUE)
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not fetch campaign.")
  public CampaignDTO get(@PathVariable long campaignId) {
    campaignService.updateDeviceReplies(campaignId);

    return campaignService.findById(campaignId);
  }

  /**
   * Creates and starts a new workflow instance for a campaign.
   *
   * @param campaignId The Id of the campaign to start.
   */
  @GetMapping(path = "{campaignId}/start", produces = MediaType.APPLICATION_JSON_VALUE)
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not start campaign.")
  public ResponseEntity start(@PathVariable long campaignId) {
    campaignService.instantiate(campaignId);
    workflowService.instantiate(campaignId);

    return ResponseEntity.ok().build();
  }

  /**
   * Returns various statistics about a campaign.
   *
   * @param campaignId The Id of the campaign to retrieve statistics for.
   */
  @GetMapping(path = "{campaignId}/stats", produces = MediaType.APPLICATION_JSON_VALUE)
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not campaign statistics.")
  public ResponseEntity<CampaignStatsDTO> stats(@PathVariable long campaignId) {
    return ResponseEntity.ok().body(campaignService.statsCampaign(campaignId));
  }

  /**
   * Terminates a running campaign.
   *
   * @param campaignId The Id of the campaign to terminate.
   */
  @GetMapping(path = "{campaignId}/terminate", produces = MediaType.APPLICATION_JSON_VALUE)
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not terminate campaign.")
  public ResponseEntity terminate(@PathVariable long campaignId) {
    campaignService.setState(campaignId, State.TERMINATED_BY_USER);
    workflowService.suspend(campaignId);
    return ResponseEntity.ok().build();
  }

  /**
   * Pauses a running campaign.
   *
   * @param campaignId The Id of the campaign to pause.
   */
  @GetMapping(path = "{campaignId}/pause", produces = MediaType.APPLICATION_JSON_VALUE)
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not pause campaign.")
  public ResponseEntity pause(@PathVariable long campaignId) {
    campaignService.setState(campaignId, State.PAUSED_BY_USER);
    workflowService.suspend(campaignId);
    return ResponseEntity.ok().build();
  }

  /**
   * Resumes a previously paused campaign. A campaign might have been originally paused either
   * manually by the user or automatically by the workflow.
   *
   * @param campaignId The Id of the campaign to resume.
   */
  @GetMapping(path = "{campaignId}/resume", produces = MediaType.APPLICATION_JSON_VALUE)
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not resume campaign.")
  public ResponseEntity resume(@PathVariable long campaignId) {
    int campaignStateSnapshot = campaignService.getState(campaignId);
    campaignService.setState(campaignId, State.RUNNING);
    if (campaignStateSnapshot == State.PAUSED_BY_USER) {
      workflowService.resume(campaignId);
    } else if (campaignStateSnapshot == State.PAUSED_BY_WORKFLOW) {
      workflowService.correlateMessage(campaignId);
    }

    return ResponseEntity.ok().build();
  }

  /**
   * Searches and returns all available campaigns.
   *
   * @param predicate The predicate to search by.
   * @param pageable  The paging parameters for the results.
   */
  @EmptyPredicateCheck
  @ReplyPageableFilter("id,name,type,state,startedOn,terminatedOn")
  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "There was a problem retrieving campaigns.")
  public Page<CampaignDTO> findAll(
    @QuerydslPredicate(root = Campaign.class) Predicate predicate, Pageable pageable) {
    return campaignService.findAll(predicate, pageable);
  }
}
