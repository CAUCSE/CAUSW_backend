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
	boolean isTermsAgreed,
	boolean isAcademicCertified,
	AcademicStatus academicStatus,
	boolean isKeepLogin) {
	public static AuthResult of(String accessToken,
		String name,
		String email,
		ProfileImageDto profileImage,
		String refreshToken,
		boolean isGuest,
		boolean isTermsAgreed,
		boolean isAcademicCertified,
		AcademicStatus academicStatus,
		boolean isKeepLogin) {
		return new AuthResult(accessToken, name, email, profileImage, refreshToken, isGuest, isTermsAgreed,
			isAcademicCertified, academicStatus, isKeepLogin);
	}
}
