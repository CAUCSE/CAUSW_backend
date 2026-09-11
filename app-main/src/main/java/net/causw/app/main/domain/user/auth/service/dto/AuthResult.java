package net.causw.app.main.domain.user.auth.service.dto;

import java.util.Set;

import net.causw.app.main.domain.user.academic.enums.userAcademicRecord.AcademicStatus;
import net.causw.app.main.domain.user.account.enums.user.Role;
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
	AcademicStatus academicStatus,
	Set<Role> roles) {

	public AuthResult {
		roles = Set.copyOf(roles);
	}

	public static AuthResult of(String accessToken,
		String name,
		String email,
		ProfileImageDto profileImage,
		String refreshToken,
		boolean isGuest,
		boolean hasAllRequiredLatestTerms,
		boolean isAcademicCertified,
		AcademicStatus academicStatus,
		Set<Role> roles) {
		return new AuthResult(accessToken, name, email, profileImage, refreshToken, isGuest, hasAllRequiredLatestTerms,
			isAcademicCertified, academicStatus, roles);
	}
}
