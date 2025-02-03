package esthesis.service.about.dto;

import lombok.Data;

/**
 * A DTO representing general information about the application.
 */
@Data
public class AboutGeneralDTO {

	private String gitVersion;
	private String gitBuildTime;
	private String gitCommitIdAbbrev;
	private String gitCommitId;
}
