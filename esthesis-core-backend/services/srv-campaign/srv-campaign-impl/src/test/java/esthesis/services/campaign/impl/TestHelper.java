package esthesis.services.campaign.impl;

import esthesis.common.util.EsthesisCommonConstants;
import esthesis.core.common.AppConstants;
import esthesis.core.common.AppConstants.Campaign.Condition.Op;
import esthesis.core.common.AppConstants.Campaign.Condition.Stage;
import esthesis.core.common.AppConstants.Campaign.Condition.Type;
import esthesis.core.common.entity.BaseEntity;
import esthesis.service.campaign.dto.CampaignConditionDTO;
import esthesis.service.campaign.dto.CampaignMemberDTO;
import esthesis.service.campaign.entity.CampaignDeviceMonitorEntity;
import esthesis.service.campaign.entity.CampaignEntity;
import esthesis.service.common.paging.Pageable;
import esthesis.service.device.dto.DeviceKeyDTO;
import esthesis.service.device.entity.DeviceEntity;
import esthesis.services.campaign.impl.repository.CampaignDeviceMonitorRepository;
import esthesis.services.campaign.impl.repository.CampaignRepository;
import io.quarkus.mongodb.panache.PanacheQuery;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.UriInfo;
import org.bson.types.ObjectId;
import org.instancio.Instancio;
import org.mockito.Mockito;

import java.net.URI;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static esthesis.common.util.EsthesisCommonConstants.Device.Type.*;
import static esthesis.core.common.AppConstants.Campaign.Member.Type.DEVICE;
import static esthesis.core.common.AppConstants.Campaign.Member.Type.TAG;
import static esthesis.core.common.AppConstants.Device.Status.REGISTERED;
import static org.instancio.Select.all;
import static org.instancio.Select.field;
import static org.mockito.Mockito.when;

@ApplicationScoped
public class TestHelper {

	@Inject
	CampaignRepository campaignRepository;

	@Inject
	CampaignDeviceMonitorRepository campaignDeviceMonitorRepository;

	public CampaignEntity makeCampaignEntity() {
		return Instancio.of(CampaignEntity.class)
			.ignore(all(field(BaseEntity.class, "id")))
			.set(all(field("conditions")), makeAllValidConditions())
			.set(all(field("members")), makeAllValidMembers())
			.set(all(field("startedOn")), Instant.now().minus(1, ChronoUnit.DAYS))
			.set(all(field("terminatedOn")), Instant.now())
			.set(all(field("processInstanceId")), String.valueOf(new Random().nextInt(1,999999)))
			.create();
	}

	public CampaignEntity persistCampaignEntity() {
		return persistCampaignEntity(makeCampaignEntity());
	}

	public CampaignEntity persistCampaignEntity(CampaignEntity campaignEntity) {
		campaignRepository.persist(campaignEntity);
		return campaignEntity;
	}


	public CampaignDeviceMonitorEntity makeCampaignDeviceMonitorEntity() {
		return Instancio.of(CampaignDeviceMonitorEntity.class)
			.ignore(all(field(BaseEntity.class, "id")))
			.create();

	}

	public CampaignDeviceMonitorEntity persistCampaignDeviceMonitorEntity(CampaignDeviceMonitorEntity entity) {
		campaignDeviceMonitorRepository.persist(entity);
		return entity;
	}

	public CampaignDeviceMonitorEntity persistCampaignDeviceMonitorEntity() {
		CampaignDeviceMonitorEntity entity = makeCampaignDeviceMonitorEntity();
		campaignDeviceMonitorRepository.persist(entity);
		return entity;
	}


	/**
	 * Helper method to create a Pageable object with the specified parameters
	 */
	public Pageable makePageable(int page, int size, String sort) {

		// Create a mock of UriInfo
		UriInfo uriInfo = Mockito.mock(UriInfo.class);

		// Define the behavior of the mock
		when(uriInfo.getRequestUri()).thenReturn(URI.create("http://localhost:8080/find?page=0&size=10"));
		when(uriInfo.getQueryParameters()).thenReturn(new MultivaluedHashMap<>());

		Pageable pageable = new Pageable();
		pageable.setPage(page);
		pageable.setSize(size);
		pageable.setSort(sort); // Sorting field and direction (e.g., "name,asc")
		pageable.setUriInfo(uriInfo);
		return pageable;
	}

	public PanacheQuery<CampaignDeviceMonitorEntity> findAllCampaignDeviceMonitorEntities() {
		return campaignDeviceMonitorRepository.findAll();
	}

	public PanacheQuery<CampaignEntity> findAllCampaignEntities() {
		return campaignRepository.findAll();
	}

	public CampaignEntity findCampaign(String campaignId) {
		return campaignRepository.findById(new ObjectId(campaignId));
	}

	public void clearDatabase() {
		this.campaignDeviceMonitorRepository.deleteAll();
		this.campaignRepository.deleteAll();
	}

	public CampaignConditionDTO makeSuccessCondition() {
		return new CampaignConditionDTO()
			.setType(Type.SUCCESS)
			.setStage(Stage.ENTRY)
			.setGroup(1)
			.setValue("someValue");
	}

	public CampaignConditionDTO makePropertyCondition() {
		return new CampaignConditionDTO()
			.setType(Type.PROPERTY)
			.setStage(Stage.ENTRY)
			.setPropertyName("someProperty")
			.setOperation(Op.EQUAL)
			.setGroup(1)
			.setValue("someValue");
	}

	public CampaignConditionDTO makePauseCondition() {
		return new CampaignConditionDTO()
			.setType(Type.PAUSE)
			.setStage(Stage.ENTRY)
			.setGroup(1)
			.setOperation(Op.EQUAL);
	}

	public CampaignConditionDTO makeBatchCondition() {
		return new CampaignConditionDTO()
			.setType(Type.BATCH)
			.setGroup(1)
			.setValue("10");
	}

	public CampaignConditionDTO makeDateTimeCondition() {
		return new CampaignConditionDTO()
			.setType(Type.DATETIME)
			.setStage(Stage.INSIDE)
			.setOperation(Op.AFTER)
			.setGroup(1)
			.setScheduleDate(Instant.now().plus(1, ChronoUnit.DAYS));
	}

	// Method to create a list of all valid conditions
	public List<CampaignConditionDTO> makeAllValidConditions() {
		return Arrays.asList(
			makeSuccessCondition(),
			makePropertyCondition(),
			makePauseCondition(),
			makeBatchCondition(),
			makeDateTimeCondition()
		);
	}

	public DeviceEntity makeDeviceEntity(String hardwareId) {
		DeviceEntity deviceEntity = new DeviceEntity()
			.setHardwareId(hardwareId)
			.setType(CORE)
			.setCreatedOn(Instant.now().minus(1, ChronoUnit.DAYS))
			.setTags(List.of("test"))
			.setRegisteredOn(Instant.now().minus(1, ChronoUnit.DAYS))
			.setLastSeen(Instant.now().minus(1, ChronoUnit.MINUTES))
			.setStatus(REGISTERED);


		deviceEntity.setId(new ObjectId());

		return deviceEntity;

	}

	private List<CampaignMemberDTO> makeAllValidMembers() {
		return List.of(
			CampaignMemberDTO.builder().group(1).type(DEVICE).identifier("test-campaign-member-1").build(),
			CampaignMemberDTO.builder().group(2).type(DEVICE).identifier("test-campaign-member-2").build(),
			CampaignMemberDTO.builder().group(3).type(TAG).identifier("test-campaign-member-3").build(),
			CampaignMemberDTO.builder().group(4).type(TAG).identifier("test-campaign-member-4").build()
		);
	}
}
