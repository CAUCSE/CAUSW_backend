package net.causw.app.main.domain.event;

/**
 * 사용자의 학적 최초 인증 시 발행되는 이벤트입니다.
 * <ul>
 *   <li>재학생의 경우, 학적 인증 신청 후 증빙 자료가 승인될 때 발행됩니다.</li>
 *   <li>졸업생 및 휴학생의 경우, 학적 인증 신청 시 발행됩니다.</li>
 * </ul>
 *
 * @param userId 학적 인증된 사용자의 ID
 */
public record CertifiedUserCreatedEvent(String userId) {
}