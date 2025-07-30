package net.causw.app.main.dto.userInfo;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserInfoUpdateRequestDto {

  @Email(message = "이메일 형식에 맞지 않습니다.")
  private String email;

  @Pattern(regexp = "^01(?:0|1|[6-9])-(\\d{3}|\\d{4})-\\d{4}$", message = "전화번호 형식에 맞지 않습니다.")
  private String phoneNumber;

  @Size(max = 200, message = "최대 글자수 200을 초과했습니다.")
  private String description;

  @Size(max = 50, message = "최대 글자수 50을 초과했습니다.")
  private String job;

  @Valid
  private List<UserCareerDto> userCareer;

  private String githubLink;
  private String linkedInLink;
  private String velogLink;
  private String notionLink;
  private String instagramLink;

  @NotNull
  private Boolean isPhoneNumberVisible;
}
