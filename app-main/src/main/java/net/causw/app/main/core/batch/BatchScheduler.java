package net.causw.app.main.core.batch;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Slice;
import org.springframework.scheduling.annotation.Scheduled;

import net.causw.app.main.domain.asset.file.entity.UuidFile;
import net.causw.app.main.domain.asset.file.service.implementation.FileReader;
import net.causw.app.main.domain.asset.file.service.implementation.FileWriter;
import net.causw.app.main.shared.storage.StorageClient;
import net.causw.app.main.domain.community.ceremony.service.implementation.CeremonyWriter;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.enums.user.UserState;
import net.causw.app.main.domain.user.account.service.UserProfileImageService;
import net.causw.app.main.domain.user.account.service.implementation.AdmissionWriter;
import net.causw.app.main.domain.user.account.service.implementation.SocialAccountWriter;
import net.causw.app.main.domain.user.account.service.implementation.UserInfoWriter;
import net.causw.app.main.domain.user.account.service.implementation.UserReader;
import net.causw.app.main.domain.user.account.service.implementation.UserWriter;
import net.causw.app.main.shared.pageable.PageableFactory;
import net.causw.global.constant.MessageUtil;
import net.causw.global.constant.StaticValue;
import net.causw.global.exception.ErrorCode;
import net.causw.global.exception.InternalServerException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class BatchScheduler {

	private final FileReader fileReader;
	private final FileWriter fileWriter;
	private final StorageClient storageClient;
	private final UserReader userReader;
	private final PageableFactory pageableFactory;
	private final UserInfoWriter userInfoWriter;
	private final CeremonyWriter ceremonyWriter;
	private final SocialAccountWriter socialAccountWriter;
	private final UserWriter userWriter;
	private final AdmissionWriter admissionWriter;
	private final UserProfileImageService userProfileImageService;

	@Scheduled(cron = "0 0 * * * ?") // 매시 정각
	public void scheduleCleanupPendingFiles() {
		log.info("[PENDING 파일 정리 배치] 시작");
		LocalDateTime cutoff = LocalDateTime.now().minusHours(1);

		List<UuidFile> pendingFiles = fileReader.findAllPendingBefore(cutoff);
		if (pendingFiles.isEmpty()) {
			log.info("[PENDING 파일 정리 배치] 정리 대상 없음");
			return;
		}

		int cleanedCount = 0;
		int s3FailCount = 0;
		for (UuidFile file : pendingFiles) {
			try {
				storageClient.delete(file.getFileKey());
			} catch (Exception e) {
				s3FailCount++;
				log.warn("[PENDING 파일 정리 배치] S3 삭제 실패. 재시도 예정. FileKey: {}", file.getFileKey(), e);
				continue;
			}
			fileWriter.deleteFromDb(file.getId());
			cleanedCount++;
		}

		log.info("[PENDING 파일 정리 배치] 완료. 총: {}, 정리: {}, S3 실패(재시도 예정): {}",
			pendingFiles.size(), cleanedCount, s3FailCount);
	}

	@Scheduled(cron = "0 10 3 * * ?") // 매일 새벽 3시 10분
	public void scheduleCleanupDeactivatedUsers() {
		try {
			log.info("[유저 정리 배치] 비활성(탈퇴/추방) 유저 후처리 시작");

			LocalDateTime dueDate = LocalDateTime.now().minusDays(30);

			while (true) {
				List<User> withdrawnUsers = userReader.findCleanupTargets(
					dueDate,
					pageableFactory.create(0, StaticValue.BATCH_USER_LIST_SIZE));

				if (withdrawnUsers.isEmpty()) {
					break;
				}

				userProfileImageService.cleanupProfileImagesForBatch(withdrawnUsers);
				userInfoWriter.deleteUserInfoByUsers(withdrawnUsers);
				ceremonyWriter.deleteCeremonyByUsers(withdrawnUsers);
				socialAccountWriter.deleteSocialAccountsByUsers(withdrawnUsers);
				admissionWriter.deleteAdmissionByUsers(withdrawnUsers);
				userWriter.cleanupWithdrawnUsers(withdrawnUsers);

				log.info("[유저 정리 배치] {}명 처리 완료", withdrawnUsers.size());
			}

			log.info("[유저 정리 배치] 탈퇴 유저 후처리 완료");
		} catch (Exception e) {
			log.error("유저 정리 배치 실패: {}", e.getMessage(), e);
			throw new InternalServerException(
				ErrorCode.INTERNAL_SERVER,
				MessageUtil.BATCH_FAIL + e.getMessage());
		}
	}

	@Scheduled(cron = "0 20 3 * * ?") // 매일 새벽 3시 20분 (다른 배치와 시각 충돌 방지)
	public void scheduleCleanupStaleGuestUsers() {
		try {
			log.info("[GUEST 정리 배치] 소셜로그인 대기 방치 유저 정리 시작");

			LocalDateTime dueDate = LocalDateTime.now().minusDays(7);

			boolean hasNext;
			do {
				Slice<User> userSlice = userReader.findUsersByStateAndUpdatedAtBefore(
					UserState.GUEST,
					dueDate,
					pageableFactory.create(0, StaticValue.BATCH_USER_LIST_SIZE));

				List<User> staleGuests = userSlice.getContent();

				if (staleGuests.isEmpty()) {
					break;
				}

				// FK 참조 데이터를 먼저 정리한 뒤 User를 하드 삭제한다. (FcmToken은 cascade로 함께 삭제됨)
				userProfileImageService.cleanupProfileImagesForBatch(staleGuests);
				socialAccountWriter.deleteSocialAccountsByUsers(staleGuests);
				userWriter.hardDeleteUsers(staleGuests);

				hasNext = userSlice.hasNext();
				log.info("[GUEST 정리 배치] {}명 삭제 완료", staleGuests.size());

			} while (hasNext);
			log.info("[GUEST 정리 배치] 소셜로그인 대기 방치 유저 정리 완료");
		} catch (Exception e) {
			log.error("GUEST 정리 배치 실패: {}", e.getMessage(), e);
			throw new InternalServerException(
				ErrorCode.INTERNAL_SERVER,
				MessageUtil.BATCH_FAIL + e.getMessage());
		}
	}

}
