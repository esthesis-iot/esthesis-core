package esthesis.services.campaign.impl.service;

import esthesis.service.campaign.entity.CampaignDeviceMonitorEntity;
import esthesis.service.common.BaseService;
import jakarta.enterprise.context.ApplicationScoped;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;

@Slf4j
@ApplicationScoped
public class CampaignDeviceMonitorService extends BaseService<CampaignDeviceMonitorEntity> {

	/**
	 * Finds the devices of a campaign.
	 *
	 * @param campaignId
	 * @return
	 */
	public List<CampaignDeviceMonitorEntity> findByCampaignID(String campaignId) {
		return findByColumn("campaignId", new ObjectId(campaignId));
	}

	/**
	 * Finds the devices of a campaign group.
	 *
	 * @param campaignId The campaign id to find the devices for.
	 * @param group      The group to find the devices for.
	 */
	public List<CampaignDeviceMonitorEntity> findByCampaignIdAndGroup(String campaignId, int group) {
		return getRepository().find("campaignId = ?1 and group = ?2", new ObjectId(campaignId), group)
			.list();
	}


	/**
	 * Count all devices for the whole campaign.
	 *
	 * @param campaignId The campaign id to count the devices for.
	 * @return
	 */
	public long countAll(String campaignId) {
		return getRepository().count("campaignId = ?1", new ObjectId(campaignId));
	}

	/**
	 * Count device replies for the whole campaign.
	 *
	 * @param campaignId The campaign id to count the replies for.
	 */
	public long countReplies(String campaignId) {
		return getRepository().count("campaignId = ?1 and commandRequestId is not null"
			+ " and commandReplyId is not null", new ObjectId(campaignId));
	}

	/**
	 * Cound device replies for a specific campaign group.
	 *
	 * @param campaignId The campaign id to count the replies for.
	 * @param group      The group to count the replies for.
	 */
	public long countReplies(String campaignId, int group) {
		return getRepository().count("campaignId = ?1 and group = ?2 and commandRequestId is not null"
			+ " and commandReplyId is not null", new ObjectId(campaignId), group);
	}

	/**
	 * Count the devices that have been contacted for the whole campaign.
	 *
	 * @param campaignId The campaign id to count the devices for.
	 */
	public long countContacted(String campaignId) {
		return getRepository().count("campaignId = ?1 and commandRequestId is not null",
			new ObjectId(campaignId));
	}

	public long countInGroup(String campaignId, int group) {
		return getRepository().count("campaignId = ?1 and group = ?2",
			new ObjectId(campaignId), group);
	}

	/**
	 * Count the devices that have been contacted for a specific campaign group.
	 *
	 * @param campaignId The campaign id to count the devices for.
	 * @param group      The group to count the devices for.
	 */
	public long countContacted(String campaignId, int group) {
		return getRepository().count("campaignId = ?1 and group = ?2 and commandRequestId is not null",
			new ObjectId(campaignId), group);
	}

	/**
	 * Count the devices that have been contacted but have not replied for the whole campaign.
	 *
	 * @param campaignId The campaign id to count the devices for.
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
}
