package esthesis.service.security.dto;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

/**
 * A representation of statistics about security.
 */
@Data
@Builder
@ToString
public class StatsDTO {

	private long users;
	private long groups;
	private long roles;
	private long policies;
	private long audits;
}
