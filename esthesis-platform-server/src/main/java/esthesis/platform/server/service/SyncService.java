package esthesis.platform.server.service;

import esthesis.platform.server.dto.NiFiDTO;
import esthesis.platform.server.nifi.client.dto.EsthesisTemplateDTO;
import esthesis.platform.server.nifi.client.dto.NiFiTemplateDTO;
import esthesis.platform.server.nifi.client.services.NiFiClientService;
import esthesis.platform.server.nifi.client.util.NiFiConstants.PATH;
import esthesis.platform.server.nifi.client.util.NiFiConstants.Properties.Values.STATE;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

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
      .filter(esthesisTemplateDTO -> esthesisTemplateDTO.getName().contains(latestTemplateName))
      .findFirst().isPresent();

    String latestWorkflowTemplateResource = niFiService.getLatestWorkflowTemplateResource();
    Optional<NiFiTemplateDTO> template = niFiClientService.getTemplate(latestTemplateName);
    NiFiTemplateDTO niFiTemplateDTO = template.isPresent() ? template.get() : null;

    //Upload latest workflow.
    if (niFiTemplateDTO == null) {
      niFiTemplateDTO = uploadWorkflow(latestWorkflowTemplateResource);
    }

    String rootProcessGroupId = null;
    String outdatedWorkflowId = null;
    //Stop previous workflow and collect all needed ids.
    if (deployedEsthesisTemplates.size() == 1 && !isLatestDeployed) {
      niFiClientService.changeProcessorGroupState(PATH.ESTHESIS.getPath(), STATE.DISABLED);
      rootProcessGroupId = deployedEsthesisTemplates.get(0).getFlowGroupId();
      outdatedWorkflowId = deployedEsthesisTemplates.get(0).getTemplateId();
    }

    //Deploy latest workflow
    if (!isLatestDeployed) {
      initWorkflow(niFiTemplateDTO);
    }

    boolean allMissingSinks = niFiSinkService.createAllMissingSinks();

    //Delete previous workflow and it's deployment.
    if (ObjectUtils.allNotNull(rootProcessGroupId, outdatedWorkflowId)) {
      niFiClientService.deleteTemplate(outdatedWorkflowId);
      niFiClientService.deleteProcessGroup(rootProcessGroupId);
    }

    activeNiFi.setWfVersion(niFiService.getLatestWFVersion());
    activeNiFi.setLastChecked(Instant.now());
    activeNiFi.setSynced(allMissingSinks);

    niFiService.save(activeNiFi);
  }

  /**
   * Deletes deployed Workflow.
   */
  public void deleteWorkflow() throws IOException {
    List<EsthesisTemplateDTO> deployedTemplates = getDeployedTemplates();

    if (deployedTemplates.size() > 0) {
      niFiClientService.changeProcessorGroupState(PATH.ESTHESIS.getPath(), STATE.DISABLED);
      deployedTemplates.forEach(esthesisTemplateDTO -> {
        String rootProcessGroupId = esthesisTemplateDTO.getFlowGroupId();
        String workflowId = esthesisTemplateDTO.getTemplateId();

        try {
          niFiClientService.deleteTemplate(workflowId);
          niFiClientService.deleteProcessGroup(rootProcessGroupId);
        } catch (IOException exception) {
          exception.printStackTrace();
        }
      });
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
  }
}
