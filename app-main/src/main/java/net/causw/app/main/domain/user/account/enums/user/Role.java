package net.causw.app.main.domain.user.account.enums.user;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Component;

import net.causw.global.exception.BadRequestException;
import net.causw.global.exception.ErrorCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Role {
	SYSTEM_ADMIN("SYSTEM_ADMIN", "시스템 관리자"),
	ADMIN("ADMIN", "관리자"),
	COMMON("COMMON", "일반"),
	NONE("NONE", "없음");

	private final String value;
	private final String description;

	private static final List<Role> PRIVILEGED_ROLES = List.of(
		SYSTEM_ADMIN,
		ADMIN);

	public static Role of(String value) {
		return Arrays.stream(values())
			.filter(v -> value.equalsIgnoreCase(v.value))
			.findFirst()
			.orElseThrow(
				() -> new BadRequestException(
					ErrorCode.INVALID_REQUEST_ROLE,
					String.format("'%s' is invalid : not supported", value)));
	}

	public String authority() {
		return "ROLE_" + value;
	}

	public static List<Role> getPrivilegedRoles() {
		return PRIVILEGED_ROLES;
	}

	@Component("Role")
	public static class RoleComponent {
		public static final Role SYSTEM_ADMIN = Role.SYSTEM_ADMIN;
		public static final Role ADMIN = Role.ADMIN;
		public static final Role COMMON = Role.COMMON;
		public static final Role NONE = Role.NONE;
	}
}
