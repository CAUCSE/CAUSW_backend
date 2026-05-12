package net.causw.app.main.domain.user.account.event;

/**
 * 탈퇴 처리 시 커스텀 프로필 이미지 파일 삭제를 요청하는 이벤트입니다.
 *
 * @param fileKey 삭제할 파일의 스토리지 키
 */
public record UserProfileImageDeletionRequestedEvent(
	String fileKey) {
}
