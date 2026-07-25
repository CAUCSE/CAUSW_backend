package net.causw.app.main.domain.user.account.event;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import net.causw.app.main.shared.storage.StorageClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserProfileImageDeletionEventListener {

	private final StorageClient storageClient;

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void deleteProfileImageFile(UserProfileImageDeletionRequestedEvent event) {
		try {
			storageClient.delete(event.fileKey());

			log.info("[User Withdraw] 프로필 이미지 파일 삭제 완료. fileKey: {}", event.fileKey());
		} catch (Exception e) {
			log.error("[User Withdraw] 프로필 이미지 파일 삭제 실패. fileKey: {}", event.fileKey(), e);
		}
	}
}
