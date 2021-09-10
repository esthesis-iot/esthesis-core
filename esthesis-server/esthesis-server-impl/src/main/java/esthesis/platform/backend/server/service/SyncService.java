package esthesis.platform.backend.server.service;

import esthesis.platform.backend.server.dto.NiFiDTO;
import esthesis.platform.backend.server.model.NiFi;
import esthesis.platform.backend.server.nifi.client.dto.EsthesisTemplateDTO;
import esthesis.platform.backend.server.nifi.client.dto.NiFiTemplateDTO;
import esthesis.platform.backend.server.nifi.client.services.NiFiClientService;
import esthesis.platform.backend.server.nifi.client.util.NiFiConstants.PATH;
import esthesis.platform.backend.server.nifi.client.util.NiFiConstants.Properties.Values.STATE;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Log
@Service
@Transactional
@RequiredArgsConstructor
public class SyncService {

  private final NiFiService niFiService;
  private final NiFiSinkService niFiSinkService;
  private final NiFiClientService niFiClientService;

  /**
   * Syncs esthesis with NiFi. Uploads and initiates the latets Workflow template and adds all
   * missing sinks. Also, deletes any outdated versions.
   */
  public void sync() throws IOException {
    String latestTemplateName = "esthesis_" + niFiService.getLatestWFVersion();
    NiFiDTO activeNiFi = niFiService.getActiveNiFi();

    List<EsthesisTemplateDTO> deployedEsthesisTemplates = getDeployedTemplates();
    boolean isLatestDeployed = deployedEsthesisTemplates.stream()
      .anyMatch(esthesisTemplateDTO -> esthesisTemplateDTO.getName().contains(latestTemplateName));

    String latestWorkflowTemplateResource = niFiService.getLatestWorkflowTemplateResource();
    Optional<NiFiTemplateDTO> template = niFiClientService.getTemplate(latestTemplateName);
    NiFiTemplateDTO niFiTemplateDTO = template.orElse(null);

    //Upload latest workflow.
    if (niFiTemplateDTO == null) {
      niFiTemplateDTO = uploadWorkflow(latestWorkflowTemplateResource);
    }

    String rootProcessGroupId = null;
    String outdatedWorkflowId = null;
    //Stop previous workflow and collect all needed ids.
    if (deployedEsthesisTemplates.size() == 1 && !isLatestDeployed) {
      niFiClientService.changeProcessorGroupState(PATH.ESTHESIS.getGroupPath(), STATE.DISABLED);
      rootProcessGroupId = deployedEsthesisTemplates.get(0).getFlowGroupId();
      outdatedWorkflowId = deployedEsthesisTemplates.get(0).getTemplateId();
    }

    //Deploy latest workflow
    if (!isLatestDeployed) {
      initWorkflow(niFiTemplateDTO);
      niFiClientService.changeProcessorGroupState(new String[]{PATH.ESTHESIS.asString(),
        PATH.PRODUCERS.asString()}, STATE.RUNNING);
    }

    boolean allMissingSinks = niFiSinkService.createAllMissingSinks();

    //Delete previous workflow and it's deployment.
    if (ObjectUtils.allNotNull(rootProcessGroupId, outdatedWorkflowId)) {
      niFiClientService.deleteTemplate(outdatedWorkflowId);
      niFiClientService.deleteProcessGroup(rootProcessGroupId);
    }

    NiFi entity = niFiService.findEntityById(activeNiFi.getId());
    entity.setWfVersion(niFiService.getLatestWFVersion());
    entity.setLastChecked(Instant.now());
    entity.setSynced(allMissingSinks);
  }

  /**
   * Deletes deployed Workflow.
   */
  public void deleteWorkflow() throws IOException {

    List<EsthesisTemplateDTO> deployedTemplates = getDeployedTemplates();

    if (!deployedTemplates.isEmpty()) {
      for (EsthesisTemplateDTO esthesisTemplateDTO : deployedTemplates) {
        String rootProcessGroupId = esthesisTemplateDTO.getFlowGroupId();
        String workflowId = esthesisTemplateDTO.getTemplateId();

        niFiClientService.deleteProcessGroup(rootProcessGroupId);
        niFiClientService.deleteTemplate(workflowId);
      }
    }
  }

  private List<EsthesisTemplateDTO> getDeployedTemplates() throws IOException {
    return niFiClientService
      .getDeployedEsthesisTemplates();
  }

  private NiFiTemplateDTO uploadWorkflow(String latestWorkflowTemplateResource) throws IOException {
    return niFiClientService.uploadTemplate(latestWorkflowTemplateResource);
  }

  private void initWorkflow(NiFiTemplateDTO niFiTemplateDTO) throws IOException {
    niFiClientService.instantiateTemplate(niFiTemplateDTO.getId());
    niFiClientService.changeProcessorGroupState(PATH.ESTHESIS.getGroupPath(), STATE.RUNNING);
  }


  public void clearQueues() throws IOException {
    niFiClientService.clearRootGroupQueues();
  }
}
