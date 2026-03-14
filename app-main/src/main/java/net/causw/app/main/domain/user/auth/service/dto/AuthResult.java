package net.causw.app.main.domain.user.auth.service.dto;

import net.causw.app.main.domain.user.academic.enums.userAcademicRecord.AcademicStatus;

public record AuthResult(
	String accessToken,
	String name,
	String email,
	String profileImgUrl,
	String refreshToken,
	boolean isTermsAgreed,
	boolean isAcademicCertified,
	AcademicStatus academicStatus) {
	public static AuthResult of(String accessToken,
		String name,
		String email,
		String profileImgUrl,
		String refreshToken,
		boolean isTermsAgreed,
		boolean isAcademicCertified,
		AcademicStatus academicStatus) {
		return new AuthResult(accessToken, name, email, profileImgUrl, refreshToken, isTermsAgreed,
			isAcademicCertified, academicStatus);
	}
}
