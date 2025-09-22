package net.causw.app.main.dto.userInfo;

import java.util.List;

import net.causw.app.main.domain.model.enums.user.Department;

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
	private String profileImageUrl;
	private String major; // TODO: user 테이블의 major 필드 삭제 후 함께 제거
	private Department department;

	private String description;
	private String job;

	private List<UserCareerDto> userCareer;
}
