package net.causw.app.main.dto.userInfo;

import java.util.List;
import java.util.Set;

import net.causw.app.main.domain.model.enums.user.Department;
import net.causw.app.main.domain.model.enums.user.Role;
import net.causw.app.main.domain.model.enums.userAcademicRecord.AcademicStatus;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserInfoResponseDto {

	private String id;
	private String userId;
	private String name;
	private String email;
	private String phoneNumber;
	private Integer admissionYear;
	private String profileImageUrl;
	private String major; // TODO: user 테이블의 major 필드 삭제 후 함께 제거
	private Department department;

	private Set<Role> roles;
	private AcademicStatus academicStatus;

	private String description;
	private String job;

	private List<UserCareerDto> userCareer;

	private List<String> socialLinks;

	private Boolean isPhoneNumberVisible;
}
