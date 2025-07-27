package net.causw.app.main.dto.userInfo;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserInfoSummaryResponseDto {

  private String id;
  private String name;
  private String email;
  private Integer admissionYear;
  private String profileImageUrl;
  private String major;

  private String description;
  private String job;

  private List<UserCareerDto> userCareer;
}
