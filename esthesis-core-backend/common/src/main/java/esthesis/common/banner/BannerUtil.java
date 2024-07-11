package esthesis.common.banner;

import esthesis.common.git.GitUtil;

public class BannerUtil {

	private BannerUtil() {
	}

	//@formatter:off
  private static final String BANNER = """
***************************************************
https://esthes.is              esthesis@eurodyn.com

           _   _               _       _       _
  ___  ___| |_| |__   ___  ___(_)___  (_) ___ | |_
 / _ \\/ __| __| '_ \\ / _ \\/ __| / __| | |/ _ \\| __|
|  __/\\__ \\ |_| | | |  __/\\__ \\ \\__ \\ | | (_) | |_
 \\___||___/\\__|_| |_|\\___||___/_|___/ |_|\\___/ \\__|
""";
	//@formatter:on

	@SuppressWarnings("java:S106")
	public static void showBanner(String... title) {
		// Display the common banner.
		System.out.print(BANNER);

		// Display the title if any.
		if (title.length > 0) {
			System.out.println("\n" + title[0]);
		}

		// Prepare version information.
		GitUtil gitUtil = new GitUtil();
		String buildTime =	gitUtil.getGitProperty(GitUtil.GIT_PROPERTY_BUILD_TIME).replaceAll("(.)(?=..$)", "$1:");
		String localTime = java.time.OffsetDateTime.now().format(
				java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX"));

		// Display version information.
		if (gitUtil.isInitialised()) {
			System.out.println("Version   : " + gitUtil.getGitProperty(GitUtil.GIT_PROPERTY_VERSION));
			System.out.println(
				"Commit    : " + gitUtil.getGitProperty(GitUtil.GIT_PROPERTY_COMMIT_ID_FULL));
			System.out.println("Build time: " + buildTime);
			System.out.println("Local time: " + localTime);
		}

		System.out.println("***************************************************\n");
	}
}
