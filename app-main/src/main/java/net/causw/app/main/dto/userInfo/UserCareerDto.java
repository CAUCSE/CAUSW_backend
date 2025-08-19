package net.causw.app.main.dto.userInfo;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserCareerDto {
  private String id;

  @NotNull
  private Integer startYear;

  @NotNull
  private Integer startMonth;

  @NotNull
  private Integer endYear;

  @NotNull
  private Integer endMonth;

  @NotNull
  @Size(max = 50, message = "최대 글자수 50을 초과했습니다.")
  private String description;
}
