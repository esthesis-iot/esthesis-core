package esthesis.common.banner;

import esthesis.common.git.GitUtil;
import java.time.ZoneId;

public class BannerUtil {

	private BannerUtil() {
	}

	//@formatter:off
  private static final String BANNER = """
***************************************************"
https://esthes.is              esthesis@eurodyn.com"

           _   _               _       _       _
  ___  ___| |_| |__   ___  ___(_)___  (_) ___ | |_
 / _ \\/ __| __| '_ \\ / _ \\/ __| / __| | |/ _ \\| __|
|  __/\\__ \\ |_| | | |  __/\\__ \\ \\__ \\ | | (_) | |_
 \\___||___/\\__|_| |_|\\___||___/_|___/ |_|\\___/ \\__|
""";
	//@formatter:on

	@SuppressWarnings("java:S106")
	public static void showBanner(String... title) {
		System.out.print(BANNER);

		if (title.length > 0) {
			System.out.println("\n" + title[0]);
		}

		GitUtil gitUtil = new GitUtil();
		if (gitUtil.isInitialised()) {
			System.out.println("Version   : " + gitUtil.getGitProperty(GitUtil.GIT_PROPERTY_VERSION));
			System.out.println(
				"Commit    : " + gitUtil.getGitProperty(GitUtil.GIT_PROPERTY_COMMIT_ID_FULL));
			System.out.println("Build time: " + gitUtil.getGitProperty(GitUtil.GIT_PROPERTY_BUILD_TIME));
		}
		System.out.println("Time zone : " + ZoneId.systemDefault());

		System.out.println("***************************************************\n");
	}
}
