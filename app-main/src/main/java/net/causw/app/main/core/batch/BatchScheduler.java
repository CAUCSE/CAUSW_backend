package net.causw.app.main.core.batch;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Page;
import org.springframework.scheduling.annotation.Scheduled;

import net.causw.app.main.domain.community.ceremony.service.implementation.CeremonyWriter;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.repository.user.UserRepository;
import net.causw.app.main.domain.user.account.service.UserProfileImageService;
import net.causw.app.main.domain.user.account.service.implementation.AdmissionWriter;
import net.causw.app.main.domain.user.account.service.implementation.SocialAccountWriter;
import net.causw.app.main.domain.user.account.service.implementation.UserInfoWriter;
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

	private final UserRepository userRepository;
	private final PageableFactory pageableFactory;
	private final UserInfoWriter userInfoWriter;
	private final CeremonyWriter ceremonyWriter;
	private final SocialAccountWriter socialAccountWriter;
	private final UserWriter userWriter;
	private final AdmissionWriter admissionWriter;
	private final UserProfileImageService userProfileImageService;

	@Scheduled(cron = "0 10 3 * * ?") // 매일 새벽 3시 10분
	public void scheduleCleanupDeactivatedUsers() {
		try {
			log.info("[유저 정리 배치] 비활성(탈퇴/추방) 유저 후처리 시작");

			LocalDateTime dueDate = LocalDateTime.now().minusDays(30);

			while (true) {
				Page<User> userPage =
					userRepository.findAllByDeletedAtIsNotNullAndDeletedAtBeforeAndEmailNotStartingWith(
						dueDate,
						"deleted_",
						pageableFactory.create(0, StaticValue.BATCH_USER_LIST_SIZE)
					);

				List<User> withdrawnUsers = userPage.getContent();

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

}
