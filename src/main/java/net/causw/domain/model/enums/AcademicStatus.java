package net.causw.domain.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AcademicStatus {
    ENROLLED("재학"), // 재학
    LEAVE_OF_ABSENCE("휴학"), // 휴학
    GRADUATED("졸업"), // 졸업
    DROPPED_OUT("중퇴"), // 중퇴
    PROBATION("퇴학"), // 퇴학
    PROFESSOR("교수"), // 교수
    UNDETERMINED("미정"); // 미정 (학적상태가 인증 필요)

    private final String value;

}
