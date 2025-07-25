package net.causw.app.main.domain.model.entity.userInfo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.causw.app.main.domain.model.entity.base.BaseEntity;
import net.causw.app.main.domain.model.entity.user.User;

@Getter
@Entity
@Builder(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "tb_user_info")
public class UserInfo extends BaseEntity {

  @OneToOne
  @JoinColumn(name = "user_id")
  private User user;

  @Column(name = "description", nullable = true)
  private String description;

  @Column(name = "github_link", nullable = true)
  private String githubLink;

  @Column(name = "linkedin_link", nullable = true)
  private String linkedInLink;

  @Column(name = "instagram_link", nullable = true)
  private String instagramLink;

  @Column(name = "notion_link", nullable = true)
  private String notionLink;

  @Column(name = "gvelo_link", nullable = true)
  private String velogLink;

  @Column(name = "job", nullable = true)
  private String job;

  @OneToMany(mappedBy = "userInfo", fetch = FetchType.LAZY)
  private List<UserCareer> userCareer;


  public void updateJob(String job) {
    this.job = job;
  }

  public void updateDescription(String description) {
    this.description = description;
  }

  public void updateGithubLink(String githubLink) {
    this.githubLink = githubLink;
  }

  public void updateLinkedInLink(String linkedInLink) {
    this.linkedInLink = linkedInLink;
  }

  public void updateInstagramLink(String instagramLink) {
    this.instagramLink = instagramLink;
  }

  public void updateNotionLink(String notionLink) {
    this.notionLink = notionLink;
  }

  public void updateVelogLink(String velogLink) {
    this.velogLink = velogLink;
  }

  public void updateUserCareer(List<UserCareer> userCareer) {
    this.userCareer = userCareer;
  }
}
