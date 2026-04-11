package net.causw.app.main.domain.notification.notification.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import net.causw.app.main.domain.notification.notification.entity.QUserNotificationSetting;
import net.causw.app.main.domain.notification.notification.enums.UserNotificationSettingKey;
import net.causw.app.main.domain.user.account.entity.user.QUser;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.enums.user.UserState;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class UserNotificationSettingQueryRepository {

	private final JPAQueryFactory jpaQueryFactory;

	/**
	 * 알림 설정을 고려하여 발송 대상 유저를 단일 쿼리로 조회한다.
	 * <ul>
	 *   <li>ACTIVE + deletedAt IS NULL 조건 기본 적용</li>
	 *   <li>admissionYears 비어있으면 전체 활성 유저 대상</li>
	 *   <li>key.defaultEnabled=true → 해당 key로 enabled=false인 row 가진 유저 제외</li>
	 *   <li>key.defaultEnabled=false → 해당 key로 enabled=true인 row 가진 유저만 포함</li>
	 * </ul>
	 *
	 * @param admissionYears 대상 입학년도 목록 (비어있으면 전체)
	 * @param key            판별할 알림 설정 키
	 * @return 발송 대상 User 목록
	 */
	public List<User> findNotificationTargetsByAdmissionYears(List<Integer> admissionYears,
		UserNotificationSettingKey key) {
		QUser user = QUser.user;
		QUserNotificationSetting setting = QUserNotificationSetting.userNotificationSetting;

		if (key.isDefaultEnabled()) {
			// 기본값 ON → 명시적 disabled(false) row 가진 유저를 제외
			List<String> disabledIds = jpaQueryFactory
				.select(setting.userId)
				.from(setting)
				.where(setting.settingKey.eq(key), setting.enabled.isFalse())
				.fetch();

			return jpaQueryFactory
				.selectFrom(user)
				.where(
					user.state.eq(UserState.ACTIVE),
					user.deletedAt.isNull(),
					admissionYearCondition(user, admissionYears),
					disabledIds.isEmpty() ? null : user.id.notIn(disabledIds))
				.fetch();
		} else {
			// 기본값 OFF → 명시적 enabled(true) row 가진 유저만 포함
			List<String> enabledIds = jpaQueryFactory
				.select(setting.userId)
				.from(setting)
				.where(setting.settingKey.eq(key), setting.enabled.isTrue())
				.fetch();

			if (enabledIds.isEmpty()) {
				return List.of();
			}

			return jpaQueryFactory
				.selectFrom(user)
				.where(
					user.state.eq(UserState.ACTIVE),
					user.deletedAt.isNull(),
					admissionYearCondition(user, admissionYears),
					user.id.in(enabledIds))
				.fetch();
		}
	}

	private BooleanExpression admissionYearCondition(QUser user, List<Integer> admissionYears) {
		if (admissionYears == null || admissionYears.isEmpty()) {
			return null;
		}
		return user.admissionYear.in(admissionYears);
	}
}
