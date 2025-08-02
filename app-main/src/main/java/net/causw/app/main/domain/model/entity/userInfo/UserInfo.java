package net.causw.app.main.domain.model.entity.userInfo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.causw.app.main.domain.model.entity.base.BaseEntity;
import net.causw.app.main.domain.model.entity.user.User;
import org.hibernate.annotations.ColumnDefault;

@Getter
@Entity
@Builder(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "tb_user_info")
public class UserInfo extends BaseEntity {

  @OneToOne
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(name = "description", nullable = true)
  private String description;

  @Column(name = "job", nullable = true)
  private String job;

  @Column(name = "github_link", nullable = true)
  private String githubLink;

  @Column(name = "linkedin_link", nullable = true)
  private String linkedInLink;

  @Column(name = "instagram_link", nullable = true)
  private String instagramLink;

  @Column(name = "notion_link", nullable = true)
  private String notionLink;

  @Column(name = "blog_link", nullable = true)
  private String blogLink;

  @Column(name = "is_phone_number_visible")
  @Builder.Default
  private Boolean isPhoneNumberVisible = false;

  @OneToMany(mappedBy = "userInfo", fetch = FetchType.LAZY)
  @Builder.Default
  private List<UserCareer> userCareer = new ArrayList<>();


  public static UserInfo of(User user) {
    return UserInfo.builder()
        .user(user)
        .build();
  }

  public void update(
      String description, String job,
      String githubLink, String linkedInLink, String instagramLink, String notionLink, String blogLink,
      boolean isPhoneNumberVisible
  ) {
    this.description = description;
    this.job = job;
    this.githubLink = githubLink;
    this.linkedInLink = linkedInLink;
    this.instagramLink = instagramLink;
    this.notionLink = notionLink;
    this.blogLink = blogLink;
    this.isPhoneNumberVisible = isPhoneNumberVisible;
  }
}
