package esthesis.service.device.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * A representation of the statistics of the devices last seen.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DevicesLastSeenStatsDTO extends DevicesTotalsStatsDTO {

	// The number of devices that sent a successful ping last month.
	private long seenLastMonth;
	// The number of devices that sent a successful ping last week.
	private long seenLastWeek;
	// The number of devices that sent a successful ping last day.
	private long seenLastDay;
	// The number of devices that sent a successful ping last hour.
	private long seenLastHour;
	// The number of devices that sent a successful ping last minute.
	private long seenLastMinute;

	// The number of devices that joined last month
	private long joinedLastMonth;
	// The number of devices that joined last week
	private long joinedLastWeek;
	// The number of devices that joined last day.
	private long joinedLastDay;
	// The number of devices that joined last hour.
	private long joinedLastHour;
	// The number of devices that joined last minute.
	private long joinedLastMinute;
}
