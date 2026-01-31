package net.causw.app.main.domain.notification.notification.service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.notification.notification.api.v2.dto.NotificationCountResponseDto;
import net.causw.app.main.domain.notification.notification.api.v2.dto.NotificationResponseDto;
import net.causw.app.main.domain.notification.notification.api.v1.mapper.NotificationDtoMapper;
import net.causw.app.main.domain.notification.notification.entity.NotificationLog;
import net.causw.app.main.domain.notification.notification.enums.NoticeType;
import net.causw.app.main.domain.notification.notification.repository.NotificationLogRepository;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.shared.pageable.PageableFactory;
import net.causw.global.constant.MessageUtil;
import net.causw.global.constant.StaticValue;
import net.causw.global.exception.BadRequestException;
import net.causw.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationLogService {
	private final NotificationLogRepository notificationLogRepository;
	private final PageableFactory pageableFactory;

	@Transactional(readOnly = true)
	public NotificationResponseDto getGeneralNotificationTop4(User user) {
		List<NoticeType> types = Arrays.asList(NoticeType.BOARD, NoticeType.POST, NoticeType.COMMENT);
		List<NotificationLog> notificationLogs = notificationLogRepository.findByUserAndIsReadFalseNotificationTypes(
			user, types, pageableFactory.create(0, StaticValue.SIDE_NOTIFICATION_PAGE_SIZE));

		return notificationLogs.stream()
			.map(log -> NotificationDtoMapper.INSTANCE.toNotificationResponseDto(log.getId(), log.getNotification(),
				log.getIsRead()))
			.collect(Collectors.toList());
	}

	@Transactional
	public void readNotification(User user, String id) {
		NotificationLog notificationLog = notificationLogRepository.findByIdAndUser(id, user).orElseThrow(
			() -> new BadRequestException(
				ErrorCode.ROW_DOES_NOT_EXIST,
				MessageUtil.NOTIFICATION_LOG_NOT_FOUND));
		notificationLog.setIsRead(true);
	}

	@Transactional(readOnly = true)
	public NotificationCountResponseDto getNotificationLogCount(User user) {
		List<NotificationLog> unreadNotificationLogs = notificationLogRepository.findUnreadLogsUpToLimit(user,
			pageableFactory.create(0, StaticValue.MAX_NOTIFICATION_COUNT));

		return NotificationCountResponseDto.builder()
			.notificationLogCount(unreadNotificationLogs.size())
			.build();
	}

}