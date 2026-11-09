package net.causw.app.main.domain.campus.event.event;

/**
 * 이벤트 배너 이미지 교체 시 기존 이미지 파일 삭제를 요청하는 이벤트입니다.
 *
 * @param fileKey 삭제할 파일의 스토리지 키
 */
public record EventImageDeletionRequestedEvent(
	String fileKey) {
}
