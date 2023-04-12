package esthesis.services.about.impl.service;

import esthesis.common.git.GitUtil;
import esthesis.service.about.dto.AboutGeneralDTO;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.jwt.JsonWebToken;

@Slf4j
@ApplicationScoped
public class AboutService {

	@Inject
	JsonWebToken jwt;

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
