package esthesis.platform.backend.server.resource;

import com.eurodyn.qlack.common.exception.QExceptionWrapper;
import com.eurodyn.qlack.util.data.exceptions.ExceptionWrapper;
import com.eurodyn.qlack.util.data.filter.ReplyFilter;
import com.eurodyn.qlack.util.querydsl.EmptyPredicateCheck;
import com.querydsl.core.types.Predicate;
import esthesis.platform.backend.server.config.AppConstants;
import esthesis.platform.backend.server.config.AppConstants.Campaign.Condition;
import esthesis.platform.backend.server.config.AppConstants.Campaign.Condition.Op;
import esthesis.platform.backend.server.config.AppConstants.Campaign.Condition.Stage;
import esthesis.platform.backend.server.config.AppConstants.Campaign.Type;
import esthesis.platform.backend.server.dto.CampaignConditionDTO;
import esthesis.platform.backend.server.dto.CampaignDTO;
import esthesis.platform.backend.server.dto.CampaignValidationErrorDTO;
import esthesis.platform.backend.server.model.Campaign;
import esthesis.platform.backend.server.service.CampaignService;
import lombok.extern.java.Log;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.Instant;
import java.util.Objects;
import java.util.stream.Stream;

@Log
@Validated
@RestController
@RequestMapping("/campaign")
public class CampaignResource {

  private final CampaignService campaignService;

  public CampaignResource(CampaignService campaignService) {

    this.campaignService = campaignService;
  }

  /**
   * @return Returns a map having as key the hardware Id of the device on which the command was *
   * executed and as value the command Id (so that the reply for that command can be queried later *
   * on if needed).
   */
  @PostMapping()
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not save campaign.")
  public ResponseEntity save(
    @Valid @RequestBody CampaignDTO campaignDTO) {
    System.out.println(campaignDTO);
    final CampaignValidationErrorDTO errors = validate(campaignDTO);

    if (errors.hasValidationErrors()) {
      return ResponseEntity.badRequest().body(errors);
    } else {
      campaignService.save(campaignDTO);
      return ResponseEntity.ok().build();
    }
  }

  @ReplyFilter("-createdBy,-createdOn,-modifedBy,-modifiedOn")
  @GetMapping(path = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not fetch campaign.")
  public CampaignDTO get(@PathVariable long id) {
    return campaignService.findById(id);
  }

  @GetMapping(path = "{id}/start", produces = MediaType.APPLICATION_JSON_VALUE)
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not start workflow.")
  public ResponseEntity start(@PathVariable long id) {
    campaignService.startCampaign(id);
    return ResponseEntity.ok().build();
  }

  /**
   * A helper method to test during development/debugging. Keep it disabled.
   */
  @GetMapping(path = "{id}/test-workflow", produces = MediaType.APPLICATION_JSON_VALUE)
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not start testing.")
  public ResponseEntity testWorkflow(@PathVariable long id) {
    campaignService.testWorkflow(id);
    return ResponseEntity.ok().build();
  }

  @EmptyPredicateCheck
  //  @ReplyPageableFilter("-certificate,-privateKey,-publicKey")
  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "There was a problem retrieving campaigns.")
  public Page<CampaignDTO> findAll(
    @QuerydslPredicate(root = Campaign.class) Predicate predicate, Pageable pageable) {
    return campaignService.findAll(predicate, pageable);
  }

  /**
   * Custom validation checks.
   */
  private CampaignValidationErrorDTO validate(CampaignDTO campaignDTO) {
    CampaignValidationErrorDTO err = new CampaignValidationErrorDTO();
    validateMain(err, campaignDTO);
    validateConditions(err, campaignDTO);

    return err;
  }

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

  private boolean isNumberOrPercentage(String value) {
    if (value.endsWith("%")) {
      return StringUtils.isNumeric(value.substring(0, value.length() - 1));
    } else {
      return StringUtils.isNumeric(value);
    }
  }

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

    // Check only a single PAUSE type exists per target & stage.
    dto.getConditions().stream().map(CampaignConditionDTO::getTarget).distinct()
      .filter(Objects::nonNull).forEach(target -> {
      Stream.of(Stage.ENTRY, Stage.EXIT).forEach(stage -> {
        if (dto.getConditions().stream().filter(Objects::nonNull).filter(condition -> {
          return condition.getTarget().intValue() == target
            && condition.getStage().intValue() == stage
            && condition.getType().intValue() == Condition.Type.PAUSE;
        }).count() > 1) {
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

      // SUCCESS/FAILURE checks.
      if (cType == Condition.Type.FAILURE || cType == Condition.Type.SUCCESS) {
        if (cStage == null) {
          err.addConditionsError(index, "Stage can not be empty.");
        }
        if (cOperation == null) {
          err.addConditionsError(index, "Operation can not be empty.");
        }
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
          err.addConditionsError(index, "Property name should be in the form of x.y.z, e.g. telemetry.health.temperature.");
        }
      }
      index++;
    }
  }
}
