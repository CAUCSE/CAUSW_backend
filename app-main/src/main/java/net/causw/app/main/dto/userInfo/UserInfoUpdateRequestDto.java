package net.causw.app.main.dto.userInfo;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserInfoUpdateRequestDto {

  private String id;
  private String email;
  private String phoneNumber;

  private String description;
  private String job;

  private List<UserCareerDto> userCareer;

  private String githubLink;
  private String linkedInLink;
  private String velogLink;
  private String notionLink;
  private String instagramLink;

  private boolean isPhoneNumberVisible;
}
