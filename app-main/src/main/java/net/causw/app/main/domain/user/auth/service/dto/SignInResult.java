package net.causw.app.main.domain.user.auth.service.dto;

import net.causw.app.main.domain.user.academic.enums.userAcademicRecord.AcademicStatus;
import net.causw.app.main.shared.dto.ProfileImageDto;

public record SignInResult(
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
	public static SignInResult of(String accessToken,
		String name,
		String email,
		ProfileImageDto profileImage,
		String refreshToken,
		boolean isGuest,
		boolean isTermsAgreed,
		boolean isAcademicCertified,
		AcademicStatus academicStatus,
		boolean isKeepLogin) {
		return new SignInResult(accessToken, name, email, profileImage, refreshToken, isGuest, isTermsAgreed,
			isAcademicCertified, academicStatus, isKeepLogin);
	}
}
