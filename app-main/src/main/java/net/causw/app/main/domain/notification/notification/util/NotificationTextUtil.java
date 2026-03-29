package net.causw.app.main.domain.notification.notification.util;

/**
 * 알림 텍스트 처리 유틸리티 클래스
 */
public class NotificationTextUtil {

	public static final int PUSH_BODY_MAX_LENGTH = 60;
	public static final int SERVICE_TITLE_MAX_LENGTH = 50;

	/**
	 * 문자열이 maxLength보다 길면 ...으로 줄여서 반환하는 메소드
	 * @param str 원본 문자열
	 * @param maxLength 최대 길이 (3글자 이상이어야 함)
	 * @return 줄임 처리된 문자열
	 * todo: 이모지, 한글 등 멀티바이트 문자 처리 고려
	 */
	public static String ellipsis(String str, int maxLength) {
		if (str.length() <= maxLength) {
			return str;
		}
		return str.substring(0, maxLength - 3) + "...";
	}

	/**
	 * HTML 태그, 줄바꿈, 연속 공백을 제거하여 단순 텍스트로 변환하는 메소드
	 * @param text 원본 텍스트
	 * @return 정제된 텍스트
	 */
	public static String sanitize(String text) {
		if (text == null)
			return "";
		return text
			.replaceAll("<[^>]*>", "")
			.replaceAll("[\\r\\n]+", " ")
			.replaceAll("\\s{2,}", " ")
			.trim();
	}
}
