package esthesis.services.campaign.impl.resource;

import esthesis.common.AppConstants.Audit.Category;
import esthesis.common.AppConstants.Audit.Operation;
import esthesis.service.audit.ccc.Audited;
import esthesis.service.campaign.dto.CampaignStatsDTO;
import esthesis.service.campaign.entity.CampaignEntity;
import esthesis.service.campaign.resource.CampaignResource;
import esthesis.service.common.paging.Page;
import esthesis.service.common.paging.Pageable;
import esthesis.services.campaign.impl.service.CampaignService;
import esthesis.util.kogito.client.KogitoClient;
import javax.inject.Inject;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import org.bson.types.ObjectId;

public class CampaignResourceImpl implements CampaignResource {

  @Inject
  CampaignService campaignService;

  @Inject
  KogitoClient kogitoClient;

  @Override
  @Audited(cat = Category.CAMPAIGN, op = Operation.READ, msg = "Find campaigns")
  public Page<CampaignEntity> find(Pageable pageable) {
    return campaignService.find(pageable);
  }

  @Override
  public void save(CampaignEntity campaignEntity) {
    campaignService.save(campaignEntity);
  }

  @Override
  public CampaignEntity findById(ObjectId campaignId) {
    return campaignService.findById(campaignId);
  }

  @Override
  public void start(ObjectId campaignId) {
    campaignService.start(campaignId);
  }

  @Override
  public void resume(ObjectId campaignId) {
    campaignService.resume(campaignId);
  }

  @Override
  public CampaignStatsDTO getCampaignStats(ObjectId campaignId) {
    return campaignService.getCampaignStats(campaignId);
  }

  @Audited(cat = Category.CAMPAIGN, op = Operation.DELETE, msg = "Delete campaign")
  public Response delete(@PathParam("id") ObjectId campaignId) {
    return campaignService.deleteById(campaignId)
        ? Response.ok().build() : Response.notModified().build();
  }

  @Override
  public void test() {
    try {
      kogitoClient.startInstance("test1", "{}");

//    StartCampaignDTO startCampaignDTO = new StartCampaignDTO();
//    startCampaignDTO.setNameArg("nassos");
//    InstanceDTO instanceDTO = null;
//    try {
//      instanceDTO = kogitoClient.startInstance("DeviceCampaign", startCampaignDTO);
//      System.out.println(instanceDTO);
//
//      List<InstanceDTO> campaigns = kogitoClient.getInstances("DeviceCampaign");
//      System.out.println(campaigns);
//
//      List<TaskDTO> tasks = kogitoClient.getTasks("DeviceCampaign",
//          campaigns.get(0).getId());
//      System.out.println(tasks);
//
//      InstanceDTO instanceDTO1 = kogitoClient.completeTask("DeviceCampaign",
//          campaigns.get(0).getId(),
//          "Task1", tasks.get(0).getId());
//      System.out.println(instanceDTO1);
//
////      System.out.println(
////          kogitoClient.deleteInstance("DeviceCampaign", instanceDTO.getId())
////      );
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
