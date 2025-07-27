package net.causw.app.main.dto.userInfo;

import java.util.List;
import java.util.Set;
import lombok.Builder;
import lombok.Getter;
import net.causw.app.main.domain.model.enums.user.Role;
import net.causw.app.main.domain.model.enums.userAcademicRecord.AcademicStatus;

@Getter
@Builder
public class UserInfoResponseDto {

  private String id;
  private String name;
  private String email;
  private String phoneNumber;
  private Integer admissionYear;
  private String profileImageUrl;
  private String major;

  private Set<Role> roles;
  private AcademicStatus academicStatus;

  private String description;
  private String job;

  private List<UserCareerResponseDto> userCareer;

  private String githubLink;
  private String linkedInLink;
  private String velogLink;
  private String notionLink;
  private String instagramLink;

  private boolean isPhoneNumberVisible;
}
