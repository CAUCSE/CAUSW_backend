package net.causw.app.main.domain.user.academic.enums.userAcademicRecord;

import net.causw.app.main.shared.exception.errorcode.UserErrorCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AcademicStatus {
	ENROLLED("재학"), // 재학
	GRADUATED("졸업"), // 졸업
	UNDETERMINED("미정"), // 미정 (학적상태가 인증 필요)
	/**
	 * @deprecated V1에서 사용하던 status "휴학", "중퇴", "정학", "퇴학", "교수"는 V2에서 사용되지 않음
	 */
	@Deprecated
	LEAVE_OF_ABSENCE("휴학"), // 휴학
	@Deprecated
	DROPPED_OUT("중퇴"), // 중퇴
	@Deprecated
	SUSPEND("정학"), // 정학
	@Deprecated
	EXPEL("퇴학"), // 퇴학
	@Deprecated
	PROFESSOR("교수"); // 교수


	private final String value;

	public static AcademicStatus fromString(String academicStatus) {
		if (academicStatus == null || academicStatus.isEmpty()) {
			throw UserErrorCode.INVALID_ACADEMIC_STATUS.toBaseException();
		}
		for (AcademicStatus status : AcademicStatus.values()) {
			if (status.name().equalsIgnoreCase(academicStatus)) {
				return status;
			}
		}
		throw UserErrorCode.INVALID_ACADEMIC_STATUS.toBaseException();
	}
}
