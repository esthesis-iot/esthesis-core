package esthesis.services.about.impl.service;

import static esthesis.core.common.AppConstants.Security.Category.ABOUT;
import static esthesis.core.common.AppConstants.Security.Operation.READ;

import esthesis.common.git.GitUtil;
import esthesis.service.about.dto.AboutGeneralDTO;
import esthesis.service.security.annotation.ErnPermission;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * Provides information about the application.
 */
@Slf4j
@ApplicationScoped
public class AboutService {

	/**
	 * Returns general information about the application.
	 *
	 * @return general information about the application.
	 */
	@ErnPermission(category = ABOUT, operation = READ)
	public AboutGeneralDTO getGeneralInfo() {
		AboutGeneralDTO about = new AboutGeneralDTO();

		// Set Git info.
		GitUtil gitUtil = new GitUtil();

		String gitBuildTime = gitUtil.getGitProperty(GitUtil.GIT_PROPERTY_BUILD_TIME);
		String gitCommitIdFull = gitUtil.getGitProperty(GitUtil.GIT_PROPERTY_COMMIT_ID_FULL);
		String gitCommitIdAbbrev = gitUtil.getGitProperty(GitUtil.GIT_PROPERTY_COMMIT_ID_ABBREV);
		String gitVersion = gitUtil.getGitProperty(GitUtil.GIT_PROPERTY_VERSION);

		if (StringUtils.isNotBlank(gitBuildTime)) {
			about.setGitBuildTime(gitBuildTime);
		}
		if (StringUtils.isNotBlank(gitCommitIdFull)) {
			about.setGitCommitId(gitCommitIdFull);
		}

		if (StringUtils.isNotBlank(gitCommitIdAbbrev)) {
			about.setGitCommitIdAbbrev(gitCommitIdAbbrev);
		}
		if (StringUtils.isNotBlank(gitVersion)) {
			about.setGitVersion(gitVersion);
		}

		return about;
	}
}
