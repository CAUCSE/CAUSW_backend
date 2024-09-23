package net.causw.domain.model.enums;

public enum AcademicStatus {
    ENROLLED, // 재학
    LEAVE_OF_ABSENCE, // 휴학
    GRADUATED, // 졸업
    DROPPED_OUT, // 중퇴
    PROBATION, // 퇴학
    PROFESSOR, // 교수
    MEMBERSHIP_FEE_PAID, // 학생회비 납부자
    UNDEFINED, // 상관없음
    UNDETERMINED // 미정 (학적상태가 인증 필요)
}
