package net.causw.app.main.domain.asset.locker.service.dto.result;

/**
 * 만료 사물함 일괄 반납 처리 결과 DTO.
 *
 * @param releasedCount 반납 처리된 사물함 수
 * @param skippedCount  처리 시점에 이미 반납되었거나 만료 상태가 아니어서 스킵된 사물함 수
 * @param failedCount   처리 중 예외가 발생한 사물함 수
 */
public record LockerExpirationResult(
	int releasedCount,
	int skippedCount,
	int failedCount) {
}
