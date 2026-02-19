package net.causw.app.main.domain.asset.locker.service.v2.dto.result;

import java.time.LocalDateTime;

import lombok.Builder;

/**
 * 내 사물함 정보 조회 결과 DTO.
 *
 * @param hasLocker  사물함 보유 여부
 * @param lockerId   사물함 ID (보유하지 않은 경우 null)
 * @param displayName 사물함 표시 이름(위치+번호 등)
 * @param expiredAt  사물함 만료 일시
 */
@Builder
public record MyLockerResult(
	boolean hasLocker,
	String lockerId,
	String displayName,
	LocalDateTime expiredAt) {

	/**
	 * 사물함을 보유하지 않은 상태를 나타내는 결과를 반환한다.
	 *
	 * @return 사물함 미보유 상태 결과
	 */
	public static MyLockerResult empty() {
		return new MyLockerResult(false, null, null, null);
	}

	/**
	 * 사물함을 보유한 상태를 나타내는 결과를 생성한다.
	 *
	 * @param lockerId    사물함 ID
	 * @param displayName 사물함 표시 이름
	 * @param expiredAt   사물함 만료 일시
	 * @return 사물함 보유 상태 결과
	 */
	public static MyLockerResult of(String lockerId, String displayName, LocalDateTime expiredAt) {
		return new MyLockerResult(true, lockerId, displayName, expiredAt);
	}
}
