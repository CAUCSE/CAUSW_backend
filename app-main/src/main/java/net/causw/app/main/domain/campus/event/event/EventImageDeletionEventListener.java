package net.causw.app.main.domain.campus.event.event;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import net.causw.app.main.shared.storage.StorageClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventImageDeletionEventListener {

	private final StorageClient storageClient;

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void deleteEventImageFile(EventImageDeletionRequestedEvent event) {
		try {
			storageClient.delete(event.fileKey());

			log.info("[Event] 기존 배너 이미지 파일 삭제 완료. fileKey: {}", event.fileKey());
		} catch (Exception e) {
			log.error("[Event] 기존 배너 이미지 파일 삭제 실패. fileKey: {}", event.fileKey(), e);
		}
	}
}
