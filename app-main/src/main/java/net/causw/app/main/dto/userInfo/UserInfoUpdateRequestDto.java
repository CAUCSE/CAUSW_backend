package net.causw.app.main.dto.userInfo;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserInfoUpdateRequestDto {

	@Pattern(regexp = "^01(?:0|1|[6-9])-(\\d{3}|\\d{4})-\\d{4}$", message = "전화번호 형식에 맞지 않습니다.")
	private String phoneNumber;

	@Size(max = 200, message = "최대 글자수 200을 초과했습니다.")
	private String description;

	@Size(max = 50, message = "최대 글자수 50을 초과했습니다.")
	private String job;

	@Valid
	private List<UserCareerDto> userCareer;

	@Size(max = 10, message = "소셜 링크는 최대 10개까지 등록할 수 있습니다.")
	private List<String> socialLinks;

	@NotNull
	private Boolean isPhoneNumberVisible;
}
