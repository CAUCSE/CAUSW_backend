package net.causw.application.dto.userInfo;

import java.util.List;
import java.util.Set;
import lombok.Builder;
import lombok.Getter;
import net.causw.adapter.persistence.uuidFile.joinEntity.UserProfileImage;
import net.causw.domain.model.enums.user.Role;
import net.causw.domain.model.enums.userAcademicRecord.AcademicStatus;

@Getter
@Builder
public class UserInfoUpdateRequestDto {

  private String email;
  private String phoneNumber;
  private UserProfileImage profileImageUrl;

  private String description;
  private String job;

  private List<UserCareerResponseDto> career;

  private String githubLink;
  private String linkedInLink;
  private String velogLink;
  private String notionLink;
  private String instagramLink;


}
