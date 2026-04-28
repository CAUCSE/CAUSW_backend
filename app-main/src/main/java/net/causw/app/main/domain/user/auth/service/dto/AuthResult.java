package net.causw.app.main.domain.user.auth.service.dto;

import net.causw.app.main.domain.user.academic.enums.userAcademicRecord.AcademicStatus;
import net.causw.app.main.shared.dto.ProfileImageDto;

public record AuthResult(
	String accessToken,
	String name,
	String email,
	ProfileImageDto profileImage,
	String refreshToken,
	boolean isGuest,
	boolean hasAllRequiredLatestTerms,
	boolean isAcademicCertified,
	AcademicStatus academicStatus) {
	public static AuthResult of(String accessToken,
		String name,
		String email,
		ProfileImageDto profileImage,
		String refreshToken,
		boolean isGuest,
		boolean hasAllRequiredLatestTerms,
		boolean isAcademicCertified,
		AcademicStatus academicStatus) {
		return new AuthResult(accessToken, name, email, profileImage, refreshToken, isGuest, hasAllRequiredLatestTerms,
			isAcademicCertified, academicStatus);
	}
}
