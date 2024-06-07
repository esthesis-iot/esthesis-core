package esthesis.services.about.impl.service;

import esthesis.common.git.GitUtil;
import esthesis.service.about.dto.AboutGeneralDTO;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class AboutService {

	public AboutGeneralDTO getGeneralInfo() {
		AboutGeneralDTO about = new AboutGeneralDTO();

		// Set Git info.
		GitUtil gitUtil = new GitUtil();
		about.setGitBuildTime(gitUtil.getGitProperty(GitUtil.GIT_PROPERTY_BUILD_TIME));
		about.setGitCommitId(gitUtil.getGitProperty(GitUtil.GIT_PROPERTY_COMMIT_ID_FULL));
		about.setGitCommitIdAbbrev(gitUtil.getGitProperty(GitUtil.GIT_PROPERTY_COMMIT_ID_ABBREV));
		about.setGitVersion(gitUtil.getGitProperty(GitUtil.GIT_PROPERTY_VERSION));

		return about;
	}
}
