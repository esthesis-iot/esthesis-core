package esthesis.common.git;

import java.util.Properties;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GitUtil {

  private static final String GIT_PROPERTIES_NAME = "git.properties";

  private boolean initialised = false;
  private final Properties properties = new Properties();

  public static final String GIT_PROPERTY_BUILD_TIME = "git.build.time";
  public static final String GIT_PROPERTY_VERSION = "git.build.version";
  public static final String GIT_PROPERTY_COMMIT_ID_ABBREV = "git.commit.id.abbrev";
  public static final String GIT_PROPERTY_COMMIT_ID_FULL = "git.commit.id.full";

  public GitUtil() {
    try {
      properties.load(GitUtil.class.getClassLoader()
          .getResourceAsStream(GIT_PROPERTIES_NAME));
      initialised = true;
    } catch (Exception e) {
      log.warn("Could not read {} file.", GIT_PROPERTIES_NAME);
    }
  }

  public String getGitProperty(String gitProperty) {
    if (initialised) {
      return properties.getProperty(gitProperty);
    } else {
      return "{not available}";
    }
  }

  public boolean isInitialised() {
    return initialised;
  }

}
