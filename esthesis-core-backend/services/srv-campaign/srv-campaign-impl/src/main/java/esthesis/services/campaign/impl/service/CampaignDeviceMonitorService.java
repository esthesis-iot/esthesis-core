package esthesis.services.campaign.impl.service;

import esthesis.service.campaign.entity.CampaignDeviceMonitorEntity;
import esthesis.service.common.BaseService;
import jakarta.enterprise.context.ApplicationScoped;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;

/**
 * Service for managing the monitoring of devices in a campaign.
 */
@Slf4j
@ApplicationScoped
public class CampaignDeviceMonitorService extends BaseService<CampaignDeviceMonitorEntity> {

	/**
	 * Finds the devices of a campaign.
	 *
	 * @param campaignId The campaign id to find the devices for.
	 * @return The devices of the campaign.
	 */
	public List<CampaignDeviceMonitorEntity> findByCampaignID(String campaignId) {
		return findByColumn("campaignId", new ObjectId(campaignId));
	}

	/**
	 * Finds the devices of a campaign group.
	 *
	 * @param campaignId The campaign id to find the devices for.
	 * @param group      The group to find the devices for.
	 * @return The devices of the campaign group.
	 */
	public List<CampaignDeviceMonitorEntity> findByCampaignIdAndGroup(String campaignId, int group) {
		return getRepository().find("campaignId = ?1 and group = ?2", new ObjectId(campaignId), group)
			.list();
	}


	/**
	 * Counts all devices for the whole campaign.
	 *
	 * @param campaignId The campaign id to count the devices for.
	 * @return The number of devices.
	 */
	public long countAll(String campaignId) {
		return getRepository().count("campaignId = ?1", new ObjectId(campaignId));
	}

	/**
	 * Counts device replies for the whole campaign.
	 *
	 * @param campaignId The campaign id to count the replies for.
	 * @return The number of replies.
	 */
	public long countReplies(String campaignId) {
		return getRepository().count("campaignId = ?1 and commandRequestId is not null"
			+ " and commandReplyId is not null", new ObjectId(campaignId));
	}

	/**
	 * Counts device replies for a specific campaign group.
	 *
	 * @param campaignId The campaign id to count the replies for.
	 * @param group      The group to count the replies for.
	 * @return The number of replies.
	 */
	public long countReplies(String campaignId, int group) {
		return getRepository().count("campaignId = ?1 and group = ?2 and commandRequestId is not null"
			+ " and commandReplyId is not null", new ObjectId(campaignId), group);
	}

	/**
	 * Counts the devices that have been contacted for the whole campaign.
	 *
	 * @param campaignId The campaign id to count the devices for.
	 * @return The number of devices.
	 */
	public long countContacted(String campaignId) {
		return getRepository().count("campaignId = ?1 and commandRequestId is not null",
			new ObjectId(campaignId));
	}

	/**
	 * Counts the devices in a group.
	 *
	 * @param campaignId The campaign id to count the devices for.
	 * @param group      The group to count the devices for.
	 * @return The number of devices.
	 */
	public long countInGroup(String campaignId, int group) {
		return getRepository().count("campaignId = ?1 and group = ?2",
			new ObjectId(campaignId), group);
	}

	/**
	 * Count the devices that have been contacted for a specific campaign group.
	 *
	 * @param campaignId The campaign id to count the devices for.
	 * @param group      The group to count the devices for.
	 * @return The number of devices.
	 */
	public long countContacted(String campaignId, int group) {
		return getRepository().count("campaignId = ?1 and group = ?2 and commandRequestId is not null",
			new ObjectId(campaignId), group);
	}

	/**
	 * Count the devices that have been contacted but have not replied for the whole campaign.
	 *
	 * @param campaignId The campaign id to count the devices for.
	 * @return The number of devices.
	 */
	public long countContactedNotReplied(String campaignId) {
		return getRepository().count("campaignId = ?1 and commandRequestId is not null"
			+ " and commandReplyId is null", new ObjectId(campaignId));
	}

	/**
	 * Finds the devices that have been contacted but not replied yet for a specific campaign and
	 * group.
	 *
	 * @param campaignId The campaign to search for.
	 * @param group      The group to search for.
	 * @return The devices that have been contacted but not replied yet.
	 */
	public List<CampaignDeviceMonitorEntity> findContactedNotReplied(String campaignId, int group) {
		return getRepository()
			.find("campaignId = ?1 and group = ?2 and commandRequestId is not null and commandReplyId "
				+ "is null", new ObjectId(campaignId), group)
			.list();
	}

	/**
	 * Finds the devices that have not been contacted for a specific campaign and group.
	 *
	 * @param campaignId The campaign to search for.
	 * @param group      The group to search for.
	 * @param batchSize  The maximum number of devices to return.
	 * @return The devices that have not been contacted yet.
	 */
	public List<CampaignDeviceMonitorEntity> findNotContacted(String campaignId,
		int group, int batchSize) {
		return getRepository()
			.find("campaignId = ?1 and group = ?2 and commandRequestId is null",
				new ObjectId(campaignId), group)
			.range(0, batchSize - 1)
			.list();
	}

	/**
	 * Finds if a specific group for a campaign still has devices that have not been contacted.
	 *
	 * @param campaignId The campaign to search for.
	 * @param group      The group to search for.
	 * @return true if there are devices that have not been contacted yet, false otherwise.
	 */
	public boolean hasUncontactedDevices(String campaignId, int group) {
		return getRepository()
			.count("campaignId = ?1 and group = ?2 and commandRequestId is null",
				new ObjectId(campaignId), group) > 0;
	}


	/**
	 * Find the rate of replies (0 to 1).
	 *
	 * @param campaignId The campaign id to count the replies for.
	 * @param group      The group to count the replies for.
	 */
	public BigDecimal checkRate(String campaignId, int group) {
		long contacted = countContacted(campaignId, group);
		long replied = countReplies(campaignId, group);
		if (contacted == 0) {
			return BigDecimal.ZERO;
		}

		return BigDecimal.valueOf(replied)
			.divide(BigDecimal.valueOf(contacted), 2, RoundingMode.FLOOR);
	}

	@Override
	public CampaignDeviceMonitorEntity save(CampaignDeviceMonitorEntity entity) {
		return super.save(entity);
	}

	@Override
	public long deleteByColumn(String columnName, Object value) {
		return super.deleteByColumn(columnName, value);
	}
}
