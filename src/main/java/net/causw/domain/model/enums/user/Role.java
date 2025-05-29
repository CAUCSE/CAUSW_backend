package net.causw.domain.model.enums.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum Role {
    ADMIN("ADMIN", "관리자"),
    PRESIDENT("PRESIDENT", "학생회장"),
    VICE_PRESIDENT("VICE_PRESIDENT", "부학생회장"),
    COUNCIL("COUNCIL", "학생회"),
    LEADER_1("LEADER_1", "1학년 대표"),
    LEADER_2("LEADER_2", "2학년 대표"),
    LEADER_3("LEADER_3", "3학년 대표"),
    LEADER_4("LEADER_4", "4학년 대표"),
    LEADER_ALUMNI("LEADER_ALUMNI", "동문회장"),
    COMMON("COMMON", "일반"),
    NONE("NONE", "없음"),

    // Deprecated Roles
    @Deprecated LEADER_CIRCLE("LEADER_CIRCLE", "동아리장"),
    @Deprecated PROFESSOR("PROFESSOR", "교수");

    private final String value;
    private final String description;

    public static Role of(String value) {
        return Arrays.stream(values())
                .filter(v -> value.equalsIgnoreCase(v.value))
                .findFirst()
                .orElseThrow(
                        () -> new BadRequestException(
                                ErrorCode.INVALID_REQUEST_ROLE,
                                String.format("'%s' is invalid : not supported", value)
                        )
                );
    }

    public String authority() {
        return "ROLE_" + value;
    }

    @Component("Role")
    public static class RoleComponent {
        public static final Role ADMIN = Role.ADMIN;
        public static final Role PRESIDENT = Role.PRESIDENT;
        public static final Role VICE_PRESIDENT = Role.VICE_PRESIDENT;
        public static final Role COUNCIL = Role.COUNCIL;
        public static final Role LEADER_1 = Role.LEADER_1;
        public static final Role LEADER_2 = Role.LEADER_2;
        public static final Role LEADER_3 = Role.LEADER_3;
        public static final Role LEADER_4 = Role.LEADER_4;
        public static final Role LEADER_ALUMNI = Role.LEADER_ALUMNI;
        public static final Role COMMON = Role.COMMON;
        public static final Role NONE = Role.NONE;

        public static final Role LEADER_CIRCLE = Role.LEADER_CIRCLE;
        public static final Role PROFESSOR = Role.PROFESSOR;
    }
}
