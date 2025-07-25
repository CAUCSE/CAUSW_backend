package net.causw.app.main.dto.userInfo;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserCareerResponseDto {
  private Integer startYear;
  private Integer startMonth;
  private Integer endYear;
  private Integer endMonth;
  private String description;
}
