package net.causw.app.main.domain.user.account.api.v1.dto;

import net.causw.app.main.domain.user.account.enums.user.Department;
import net.causw.app.main.shared.dto.ProfileImageDto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserInfoSummaryResponseDto {

	private String id;
	private String userId;
	private String name;
	private String email;
	private Integer admissionYear;
	private ProfileImageDto profileImage;
	private String major; // TODO: user 테이블의 major 필드 삭제 후 함께 제거
	private Department department;

	private String description;
	private String job;

	// private List<UserCareerDto> userCareer;
}
