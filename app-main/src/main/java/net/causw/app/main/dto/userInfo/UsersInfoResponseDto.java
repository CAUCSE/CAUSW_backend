package net.causw.app.main.dto.userInfo;

import java.util.Set;
import lombok.Builder;
import lombok.Getter;
import net.causw.app.main.domain.model.entity.uuidFile.joinEntity.UserProfileImage;
import net.causw.app.main.domain.model.enums.user.Role;
import net.causw.app.main.domain.model.enums.userAcademicRecord.AcademicStatus;

@Getter
@Builder
public class UsersInfoResponseDto {

  private String id;
  private String name;
  private Integer admissionYear;
  private UserProfileImage profileImageUrl;
  private String major;

  private Set<Role> roles;
  private AcademicStatus academicStatus;

  private String description;
  private String job;


}
