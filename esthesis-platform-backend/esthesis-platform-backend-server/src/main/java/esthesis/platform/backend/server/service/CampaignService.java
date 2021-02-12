package esthesis.platform.backend.server.service;

import esthesis.platform.backend.common.device.dto.DeviceDTO;
import esthesis.platform.backend.server.config.AppConstants;
import esthesis.platform.backend.server.config.AppConstants.Campaign.Condition.Stage;
import esthesis.platform.backend.server.config.AppConstants.Campaign.State;
import esthesis.platform.backend.server.dto.CampaignConditionDTO;
import esthesis.platform.backend.server.dto.CampaignDTO;
import esthesis.platform.backend.server.dto.CampaignMemberDTO;
import esthesis.platform.backend.server.mapper.CampaignConditionMapper;
import esthesis.platform.backend.server.mapper.CampaignMapper;
import esthesis.platform.backend.server.mapper.CampaignMemberMapper;
import esthesis.platform.backend.server.model.Campaign;
import esthesis.platform.backend.server.model.CampaignCondition;
import esthesis.platform.backend.server.model.CampaignMember;
import esthesis.platform.backend.server.repository.CampaignConditionRepository;
import esthesis.platform.backend.server.repository.CampaignMemberRepository;
import esthesis.platform.backend.server.repository.CampaignRepository;
import esthesis.platform.backend.server.workflow.CWFLConstants;
import lombok.extern.java.Log;
import org.camunda.bpm.engine.ProcessEngine;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.stream.Collectors;

@Log
@Service
@Validated
@Transactional
public class CampaignService extends BaseService<CampaignDTO, Campaign> {

  private final CampaignMapper campaignMapper;
  private final CampaignMemberMapper campaignMemberMapper;
  private final CampaignConditionMapper campaignConditionMapper;
  private final ProcessEngine processEngine;
  private final CampaignConditionRepository campaignConditionRepository;
  private final CampaignMemberRepository campaignMemberRepository;
  private final DeviceService deviceService;
  private final CampaignRepository campaignRepository;

  public CampaignService(CampaignMapper campaignMapper,
    CampaignMemberMapper campaignMemberMapper,
    CampaignConditionMapper campaignConditionMapper,
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection") ProcessEngine processEngine,
    CampaignConditionRepository campaignConditionRepository, CampaignMemberRepository campaignMemberRepository, DeviceService deviceService, CampaignRepository campaignRepository) {
    this.campaignMapper = campaignMapper;
    this.campaignMemberMapper = campaignMemberMapper;
    this.campaignConditionMapper = campaignConditionMapper;
    this.processEngine = processEngine;
    this.campaignConditionRepository = campaignConditionRepository;
    this.campaignMemberRepository = campaignMemberRepository;
    this.deviceService = deviceService;
    this.campaignRepository = campaignRepository;
  }

  public CampaignDTO save(CampaignDTO campaignDTO) {
    final Campaign campaign;
    if (campaignDTO.getId() != null) {
      campaign = findEntityById(campaignDTO.getId());
      campaignMapper.map(campaignDTO, campaign);
      campaign.getConditions().clear();
      campaign.getMembers().clear();
    } else {
      campaign = campaignMapper.map(campaignDTO);
      campaign.setState(State.CREATED);
    }

    for (CampaignConditionDTO campaignConditionDTO : campaignDTO.getConditions()) {
      CampaignCondition campaignCondition = campaignConditionMapper.map(campaignConditionDTO);
      campaignCondition.setCampaign(campaign);
      campaign.getConditions().add(campaignCondition);
    }

    for (CampaignMemberDTO campaignMemberDTO : campaignDTO.getMembers()) {
      CampaignMember campaignMember = campaignMemberMapper.map(campaignMemberDTO);
      campaignMember.setCampaign(campaign);
      campaign.getMembers().add(campaignMember);
    }

    // Save campaign.
    getRepository().save(campaign);

    return campaignDTO;
  }

  private Map<String, Object> prepareWorkflowVariables(long campaignId) {
    Map<String, Object> variables = new HashMap<>();

    // Add campaign Id.
    variables.put(CWFLConstants.VAR_CAMPAIGN_ID, campaignId);

    // Add batch size for global level, if available.

    return variables;
  }

  public void startCampaign(long campaignId) {
    try {
      System.out.println("starting " + campaignId);
      // Create devices list participating in the campaign.



      // Create model instance.
//      final ProcessInstance processInstance = processEngine.getRuntimeService()
//        .startProcessInstanceByKey("esthesis_campaign", prepareWorkflowVariables(campaignId));

      //      Thread.sleep(1000);
      //      System.out.println("Deleting instance.");
      //      processEngine.getRuntimeService().deleteProcessInstance(processInstance.getId(), "delete");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Finds the workflow condition associated with a specific workflow location.
   *
   * @param campaignId    The Id of the underlying campaign.
   * @param tokenLocation The current location of the token (see {@link
   * @param conditionType The type of the condition (AppConstants.Condition.Type)
   *                      esthesis.platform.backend.server.workflow.ConditionsHelper}.
   */
  public List<CampaignConditionDTO> findConditions(Long campaignId, String tokenLocation,
    int conditionType) {
    Integer target = null;
    Integer stage = null;

    if (tokenLocation.equals(CWFLConstants.ACTIVITY_GLOBAL_ENTRY_ID)) {
      target = 0;
      stage = Stage.ENTRY;
    } else if (tokenLocation.startsWith(CWFLConstants.ACTIVITY_GROUP_ENTRY_PREFIX)) {
      target = Integer.parseInt(tokenLocation.substring(tokenLocation.lastIndexOf("_") + 1)) + 1;
      stage = Stage.ENTRY;
    }

    if (target == null || stage == null) {
      log.log(Level.SEVERE, "Could not find conditions for campaign id ''{0}'', "
          + "token location ''{1}'' and condition type ''{2}''.",
        new Object[]{campaignId, tokenLocation, conditionType});
      return null;
    }

    return campaignConditionMapper
      .map(campaignConditionRepository
        .findByCampaignIdAndTargetAndStageAndType(campaignId, target, stage, conditionType));
  }

  public List<CampaignMemberDTO> findCampaignMembersForGroup(long campaignId, int group) {
    return campaignMemberMapper.map(
      campaignMemberRepository.findByCampaignIdAndGroupOrder(campaignId, group));
  }

  public List<CampaignMemberDTO> findCampaignMembersForCampaign(long campaignId) {
    return campaignMemberMapper.map(campaignMemberRepository.findByCampaignId(campaignId));
  }

  public List<String> findHardwareIdsForGroup(long campaignId, int group) {
    final List<String> hardwareIds = new ArrayList<>();
    findCampaignMembersForGroup(campaignId, group).forEach(
      campaignMember -> {
        if (campaignMember.getType() == AppConstants.Campaign.Member.Type.DEVICE) {
          hardwareIds.add(campaignMember.getIdentifier());
        } else {
          hardwareIds.addAll(deviceService.findByTags(campaignMember.getIdentifier().split(","))
            .stream().map(DeviceDTO::getHardwareId).collect(Collectors.toList()));
        }
      }
    );

    return hardwareIds.stream().distinct().collect(Collectors.toList());
  }

  public List<String> findHardwareIdsForCampaign(long campaignId) {
    final List<String> hardwareIds = new ArrayList<>();
    findEntityById(campaignId).getMembers().forEach(campaignMember -> {
      if (campaignMember.getType() == AppConstants.Campaign.Member.Type.DEVICE) {
        hardwareIds.add(campaignMember.getIdentifier());
      } else {
        hardwareIds.addAll(deviceService.findByTags(campaignMember.getIdentifier().split(","))
          .stream().map(DeviceDTO::getHardwareId).collect(Collectors.toList()));
      }
    });

    return hardwareIds.stream().distinct().collect(Collectors.toList());
  }

  /**
   * Finds the number of groups associated with the campaign. Global group is not counted in, so if a campaign has just
   * a single group, the result will be 1.
   * @param campaignId The campaign to find the number of groups for.
   * @return Returns the number of groups on the campaign without counting the global group.
   */
  public int countCampaignGroups(long campaignId) {
    return campaignRepository.countCampaignGroups(campaignId);
  }

  public void testWorkflow(long id) {
    System.out.println("TEST WORKFLOW: " + id);
  }
}

