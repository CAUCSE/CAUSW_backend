package net.causw.app.main.domain.notification.notification.service.handler;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import net.causw.app.main.domain.community.ceremony.entity.Ceremony;
import net.causw.app.main.domain.community.ceremony.enums.CeremonyCategory;
import net.causw.app.main.domain.community.ceremony.enums.CeremonyType;
import net.causw.app.main.domain.community.ceremony.service.implementation.CeremonyReader;
import net.causw.app.main.domain.notification.notification.entity.Notification;
import net.causw.app.main.domain.notification.notification.enums.NoticeType;
import net.causw.app.main.domain.notification.notification.enums.UserNotificationSettingKey;
import net.causw.app.main.domain.notification.notification.event.CeremonyNotificationEvent;
import net.causw.app.main.domain.notification.notification.service.implementation.NotificationPushSender;
import net.causw.app.main.domain.notification.notification.service.implementation.NotificationSettingReader;
import net.causw.app.main.domain.notification.notification.service.implementation.NotificationWriter;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.shared.exception.errorcode.CeremonyErrorCode;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CeremonyNotificationHandler {

	private final CeremonyReader ceremonyReader;
	private final NotificationWriter notificationWriter;
	private final NotificationPushSender notificationPushSender;
	private final NotificationSettingReader notificationSettingReader;

	/**
	 * 경조사 알림 이벤트 핸들러.
	 * <p>
	 * 경조사 등록 시 대상 입학년도 유저들에게 푸시 알림과 서비스 알림을 발송합니다.
	 * <ul>
	 *   <li>대상: {@code ceremony.targetAdmissionYears} 에 해당하는 유저
	 *       (isSetAll=true 이면 전체 활성 유저)</li>
	 *   <li>필터: 경조사 알림 설정 ON — ACTIVE 유저 조회 시 DB 쿼리로 함께 처리</li>
	 * </ul>
	 *
	 * @param event 경조사 알림 이벤트
	 */
	@Async("asyncExecutor")
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void handle(CeremonyNotificationEvent event) {
		Ceremony ceremony = ceremonyReader.findById(event.ceremonyId())
			.orElseThrow(CeremonyErrorCode.CEREMONY_NOT_FOUND::toBaseException);

		User ceremonyUser = ceremony.getUser();

		// 입학년도 + 알림 설정을 단일 쿼리로 처리
		List<Integer> admissionYears = resolveAdmissionYears(ceremony);
		List<User> targets = notificationSettingReader.findCeremonyNotificationTargets(
			admissionYears, UserNotificationSettingKey.CEREMONY_NOTIFICATION_ENABLED);

		String pushTitle = buildPushTitle(ceremony);
		String pushBody = buildPushBody(ceremony);
		String serviceTitle = buildServiceTitle(ceremony);

		Notification notification = notificationWriter.save(
			Notification.of(ceremonyUser, serviceTitle, pushBody, NoticeType.CEREMONY_V2, ceremony.getId(), null));

		notificationPushSender.sendToUsers(targets, pushTitle, pushBody);
		notificationWriter.saveLogs(targets, notification);
	}

	/**
	 * 경조사 대상 범위에 따라 입학년도 목록을 반환합니다.
	 * <ul>
	 *   <li>isSetAll=true: 빈 리스트 (전체 활성 유저 대상)</li>
	 *   <li>isSetAll=false: 대상 입학년도 정수 목록</li>
	 * </ul>
	 */
	private static List<Integer> resolveAdmissionYears(Ceremony ceremony) {
		if (ceremony.isSetAll()) {
			return List.of();
		}
		return parseAdmissionYears(ceremony.getTargetAdmissionYears());
	}

	/**
	 * 경조사 대상 입학년도 문자열 집합을 정수 리스트로 변환합니다.
	 * <p>
	 * 두 자리 연도를 처리합니다: 72 이상이면 1900년대, 미만이면 2000년대로 간주합니다.
	 *
	 * @param rawYears 원본 입학년도 문자열 집합
	 * @return 변환된 입학년도 정수 리스트
	 * todo: 대상 입학년도 4자리로 변경 시 이 로직 제거
	 */
	private static List<Integer> parseAdmissionYears(Set<String> rawYears) {
		return rawYears.stream()
			.map(s -> {
				int year = Integer.parseInt(s);
				return year >= 72 ? 1900 + year : 2000 + year;
			})
			.collect(Collectors.toList());
	}

	/**
	 * 푸시 알림 타이틀을 생성합니다.
	 * <p>경사/조사 타입으로만 통일합니다.
	 */
	private static String buildPushTitle(Ceremony ceremony) {
		if (ceremony.getCeremonyType() == CeremonyType.CELEBRATION) {
			return "경사 소식";
		}
		return "조사 소식";
	}

	/**
	 * 푸시 알림 내용을 생성합니다.
	 * <p>누구의 어떤 소식인지 간결하게 표현합니다.
	 */
	private static String buildPushBody(Ceremony ceremony) {
		String subject = resolveSubject(ceremony);
		return String.format("%s 소식이 등록되었습니다.", subject);
	}

	/**
	 * 서비스 알림 제목(앱 내 알림함에 저장되는 상세 제목)을 생성합니다.
	 * <ul>
	 *   <li>경사: 일시 + 장소 포함</li>
	 *   <li>장례식(FUNERAL): 빈소 + 발인 포함</li>
	 *   <li>기타 조사(사고/투병 등): 일시 포함</li>
	 * </ul>
	 */
	private static String buildServiceTitle(Ceremony ceremony) {
		String subject = resolveSubject(ceremony);
		String location = ceremony.getAddress() != null ? ceremony.getAddress() : "";

		if (ceremony.getCeremonyType() == CeremonyType.CELEBRATION) {
			String dateTime = formatDateTime(ceremony.getStartDate(), ceremony.getStartTime());
			return String.format("%s\n일시: %s\n장소: %s", subject, dateTime, location);
		} else if (ceremony.getCeremonyCategory() == CeremonyCategory.FUNERAL) {
			String endDate = ceremony.getEndDate() != null ? ceremony.getEndDate().toString() : "";
			return String.format("%s\n빈소: %s\n발인: %s", subject, location, endDate);
		} else {
			String dateTime = formatDateTime(ceremony.getStartDate(), ceremony.getStartTime());
			return String.format("%s\n일시: %s", subject, dateTime);
		}
	}

	/**
	 * 알림 주어를 생성합니다. 관계 정보를 포함하여 맥락을 명확히 합니다.
	 * <ul>
	 *   <li>ME:      "{이름}님의 {분류}"           예) 홍길동님의 결혼식</li>
	 *   <li>FAMILY:  "{이름}님의 {관계} {분류}"     예) 홍길동님의 외조부 장례식</li>
	 *   <li>INSTEAD: "{동문이름}님의 {분류}"         예) 김철수님의 결혼식</li>
	 * </ul>
	 */
	private static String resolveSubject(Ceremony ceremony) {
		String categoryLabel = resolveCategoryLabel(ceremony);
		String name = ceremony.getUser().getName();

		return switch (ceremony.getRelationType()) {
			case ME -> String.format("%s님의 %s", name, categoryLabel);
			case FAMILY -> String.format("%s님의 %s %s", name, ceremony.getFamilyRelation(), categoryLabel);
			case INSTEAD -> String.format("%s님의 %s", ceremony.getAlumniName(), categoryLabel);
		};
	}

	/**
	 * 경조사 카테고리 레이블을 반환합니다.
	 * ETC 카테고리인 경우 커스텀 입력값을 우선 사용합니다.
	 */
	private static String resolveCategoryLabel(Ceremony ceremony) {
		if (ceremony.getCeremonyCategory() == CeremonyCategory.ETC) {
			String custom = ceremony.getCeremonyCustomCategory();
			return custom != null ? custom : ceremony.getCeremonyCategory().getLabel();
		}
		return ceremony.getCeremonyCategory().getLabel();
	}

	/**
	 * 날짜와 시간을 문자열로 포맷합니다. 시간이 없으면 날짜만 반환합니다.
	 */
	private static String formatDateTime(LocalDate date, LocalTime time) {
		if (time == null)
			return date.toString();
		return date + " " + time;
	}
}
