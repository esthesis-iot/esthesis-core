package esthesis.service.about.dto;

import lombok.Data;

@Data
public class AboutGeneralDTO {

	private String gitVersion;
	private String gitBuildTime;
	private String gitCommitIdAbbrev;
	private String gitCommitId;
}
