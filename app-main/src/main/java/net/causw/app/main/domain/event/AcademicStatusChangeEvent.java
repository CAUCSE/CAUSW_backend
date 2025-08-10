package net.causw.app.main.domain.event;

import net.causw.app.main.domain.model.enums.userAcademicRecord.AcademicStatus;

/**
 * 사용자의 학적 상태가 변경될 때, 후속 처리가 필요한 경우 발행되는 이벤트입니다.
 *
 * @param userId 학적 상태가 변경된 사용자의 ID
 * @param oldStatus 변경 전 학적 상태
 * @param newStatus 변경 후 학적 상태
 */
public record AcademicStatusChangeEvent(String userId, AcademicStatus oldStatus, AcademicStatus newStatus) {}
