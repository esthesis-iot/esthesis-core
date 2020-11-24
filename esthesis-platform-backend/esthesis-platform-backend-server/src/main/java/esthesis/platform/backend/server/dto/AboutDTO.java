package esthesis.platform.backend.server.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

public class AboutDTO {

  private String branch;
  private String buildHost;
  private String buildTime;
  private String buildUserEmail;
  private String buildUserName;
  private String buildVersion;
  private int closestTagCommitCount;
  private String closestTagName;
  private String commitId;
  private String commitIdAbbrev;
  private String commitIdDescribe;
  private String commitIdDescribeShort;
  private String commitMessageFull;
  private String commitMessageShort;
  private String commitTime;
  private String commitUserEmail;
  private String commitUserName;
  private boolean dirty;
  private String localBranchAhead;
  private String localBranchBehind;
  private String remoteOriginUrl;
  private String tags;
  private int totalCommitCount;

  @Getter @Setter
  private String nodeId;

  @JsonProperty("git.branch")
  public void setBranch(String branch) {
    this.branch = branch;
  }

  @JsonProperty("git.build.host")
  public void setBuildHost(String buildHost) {
    this.buildHost = buildHost;
  }

  @JsonProperty("git.build.time")
  public void setBuildTime(String buildTime) {
    this.buildTime = buildTime;
  }

  @JsonProperty("git.build.user.email")
  public void setBuildUserEmail(String buildUserEmail) {
    this.buildUserEmail = buildUserEmail;
  }

  @JsonProperty("git.build.user.name")
  public void setBuildUserName(String buildUserName) {
    this.buildUserName = buildUserName;
  }

  @JsonProperty("git.build.version")
  public void setBuildVersion(String buildVersion) {
    this.buildVersion = buildVersion;
  }

  @JsonProperty("git.closest.tag.commit.count")
  public void setClosestTagCommitCount(int closestTagCommitCount) {
    this.closestTagCommitCount = closestTagCommitCount;
  }

  @JsonProperty("git.closest.tag.name")
  public void setClosestTagName(String closestTagName) {
    this.closestTagName = closestTagName;
  }

  @JsonProperty("git.commit.id")
  public void setCommitId(String commitId) {
    this.commitId = commitId;
  }

  @JsonProperty("git.commit.id.abbrev")
  public void setCommitIdAbbrev(String commitIdAbbrev) {
    this.commitIdAbbrev = commitIdAbbrev;
  }

  @JsonProperty("git.commit.id.describe")
  public void setCommitIdDescribe(String commitIdDescribe) {
    this.commitIdDescribe = commitIdDescribe;
  }

  @JsonProperty("git.commit.id.describe-short")
  public void setCommitIdDescribeShort(String commitIdDescribeShort) {
    this.commitIdDescribeShort = commitIdDescribeShort;
  }

  @JsonProperty("git.commit.message.full")
  public void setCommitMessageFull(String commitMessageFull) {
    this.commitMessageFull = commitMessageFull;
  }

  @JsonProperty("git.commit.message.short")
  public void setCommitMessageShort(String commitMessageShort) {
    this.commitMessageShort = commitMessageShort;
  }

  @JsonProperty("git.commit.time")
  public void setCommitTime(String commitTime) {
    this.commitTime = commitTime;
  }

  @JsonProperty("git.commit.user.email")
  public void setCommitUserEmail(String commitUserEmail) {
    this.commitUserEmail = commitUserEmail;
  }

  @JsonProperty("git.commit.user.name")
  public void setCommitUserName(String commitUserName) {
    this.commitUserName = commitUserName;
  }

  @JsonProperty("git.dirty")
  public void setDirty(boolean dirty) {
    this.dirty = dirty;
  }

  @JsonProperty("git.local.branch.ahead")
  public void setLocalBranchAhead(String localBranchAhead) {
    this.localBranchAhead = localBranchAhead;
  }

  @JsonProperty("git.local.branch.behind")
  public void setLocalBranchBehind(String localBranchBehind) {
    this.localBranchBehind = localBranchBehind;
  }

  @JsonProperty("git.remote.origin.url")
  public void setRemoteOriginUrl(String remoteOriginUrl) {
    this.remoteOriginUrl = remoteOriginUrl;
  }

  @JsonProperty("git.tags")
  public void setTags(String tags) {
    this.tags = tags;
  }

  @JsonProperty("git.total.commit.count")
  public void setTotalCommitCount(int totalCommitCount) {
    this.totalCommitCount = totalCommitCount;
  }

  @JsonProperty("branch")
  public String getBranch() {
    return branch;
  }

  @JsonProperty("buildHost")
  public String getBuildHost() {
    return buildHost;
  }

  @JsonProperty("buildTime")
  public String getBuildTime() {
    return buildTime;
  }

  @JsonProperty("buildUserEmail")
  public String getBuildUserEmail() {
    return buildUserEmail;
  }

  @JsonProperty("buildUserName")
  public String getBuildUserName() {
    return buildUserName;
  }

  @JsonProperty("buildVersion")
  public String getBuildVersion() {
    return buildVersion;
  }

  @JsonProperty("closestTagCommitCount")
  public int getClosestTagCommitCount() {
    return closestTagCommitCount;
  }

  @JsonProperty("closestTagName")
  public String getClosestTagName() {
    return closestTagName;
  }

  @JsonProperty("commitId")
  public String getCommitId() {
    return commitId;
  }

  @JsonProperty("commitIdAbbrev")
  public String getCommitIdAbbrev() {
    return commitIdAbbrev;
  }

  @JsonProperty("commitIdDescribe")
  public String getCommitIdDescribe() {
    return commitIdDescribe;
  }

  @JsonProperty("commitIdDescribeShort")
  public String getCommitIdDescribeShort() {
    return commitIdDescribeShort;
  }

  @JsonProperty("commitMessageFull")
  public String getCommitMessageFull() {
    return commitMessageFull;
  }

  @JsonProperty("commitMessageShort")
  public String getCommitMessageShort() {
    return commitMessageShort;
  }

  @JsonProperty("commitTime")
  public String getCommitTime() {
    return commitTime;
  }

  @JsonProperty("commitUserEmail")
  public String getCommitUserEmail() {
    return commitUserEmail;
  }

  @JsonProperty("commitUserName")
  public String getCommitUserName() {
    return commitUserName;
  }

  @JsonProperty("dirty")
  public boolean isDirty() {
    return dirty;
  }

  @JsonProperty("localBranchAhead")
  public String getLocalBranchAhead() {
    return localBranchAhead;
  }

  @JsonProperty("localBranchBehind")
  public String getLocalBranchBehind() {
    return localBranchBehind;
  }

  @JsonProperty("remoteOriginUrl")
  public String getRemoteOriginUrl() {
    return remoteOriginUrl;
  }

  @JsonProperty("tags")
  public String getTags() {
    return tags;
  }

  @JsonProperty("totalCommitCount")
  public int getTotalCommitCount() {
    return totalCommitCount;
  }
}
