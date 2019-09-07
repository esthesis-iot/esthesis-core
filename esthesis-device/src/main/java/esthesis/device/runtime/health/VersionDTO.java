package esthesis.device.runtime.health;

import com.fasterxml.jackson.annotation.JsonProperty;

public class VersionDTO {
  private String commitId;
  private String buildVersion;

  @JsonProperty("git.commit.id")
  public String getCommitId() {
    return commitId;
  }

  @JsonProperty("git.commit.id")
  public void setCommitId(String commitId) {
    this.commitId = commitId;
  }

  @JsonProperty("git.build.version")
  public String getBuildVersion() {
    return buildVersion;
  }

  @JsonProperty("git.build.version")
  public void setBuildVersion(String buildVersion) {
    this.buildVersion = buildVersion;
  }
}
