package net.causw.app.main.domain.user.account.event;

/**
 * 유저 탈퇴 시 발생하는 이벤트 객체
 * S3 삭제에 필요한 파일 키 정보를 담고 있습니다.
 */
public record UserWithdrawalEvent(
	String fileKey) {
}
