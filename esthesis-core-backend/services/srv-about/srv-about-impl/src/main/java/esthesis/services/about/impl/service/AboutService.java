package esthesis.services.about.impl.service;

import static esthesis.core.common.AppConstants.Security.Category.ABOUT;
import static esthesis.core.common.AppConstants.Security.Operation.READ;

import esthesis.common.git.GitUtil;
import esthesis.service.about.dto.AboutGeneralDTO;
import esthesis.service.security.annotation.ErnPermission;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class AboutService {

	@ErnPermission(category = ABOUT, operation = READ)
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
