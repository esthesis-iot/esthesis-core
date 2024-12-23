package esthesis.service.device.dto;

import lombok.Data;

@Data
public class DevicesLastSeenStatsDTO {

	// Total devices.
	private long total;
	// Total devices with status disabled.
	private long disabled;
	// Total devices with status preregistered.
	private long preregistered;
	// Total devices with status registered.
	private long registered;

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
