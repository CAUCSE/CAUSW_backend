package net.causw.app.main.domain.notification.notification.util;

/**
 * 문자열 줄임 처리 유틸리티 클래스
 */
public class EllipsisUtil {

    /**
     * 문자열이 maxLength보다 길면 ...으로 줄여서 반환하는 메소드
     * @param str 원본 문자열
     * @param maxLength 최대 길이 (3글자 이상이어야 함)
     * @return 줄임 처리된 문자열
     */
    public static String ellipsis(String str, int maxLength) {
        if (str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength - 3) + "...";
    }
}
