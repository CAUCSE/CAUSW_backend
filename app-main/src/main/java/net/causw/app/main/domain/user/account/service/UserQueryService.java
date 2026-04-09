package net.causw.app.main.domain.user.account.service;

import static java.time.LocalTime.MAX;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.user.account.service.dto.request.UserQueryCondition;
import net.causw.app.main.domain.user.account.service.dto.result.UserSearchListResult;
import net.causw.app.main.domain.user.account.service.implementation.UserReader;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserQueryService {

	private final UserReader userReader;

	public UserSearchListResult searchUsers(UserQueryCondition condition) {
		return UserSearchListResult.from(userReader.searchByCondition(condition));
	}

	public Long getDailySignupStats(LocalDate targetDate) {
		LocalDateTime startOfDay = targetDate.atStartOfDay();
		LocalDateTime endOfDay = targetDate.atTime(MAX);

		return userReader.countByCreatedAtBetween(startOfDay, endOfDay);
	}

	public Long getTotalUserCount() {
		return userReader.getTotalUserCount();
	}
}
