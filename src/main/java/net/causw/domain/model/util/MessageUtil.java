package net.causw.domain.model.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MessageUtil {

    public static final String NOT_CIRCLE_LEADER = "사용자가 해당 동아리의 동아리장이 아닙니다.";
    public static final String NOT_CIRCLE_MEMBER = "로그인된 사용자가 동아리 멤버가 아닙니다.";
    public static final String LOGIN_USER_NOT_FOUND = "로그인된 사용자를 찾을 수 없습니다.";
    public static final String RESTORE_BOARD_NOT_FOUND = "복구할 게시판을 찾을 수 없습니다.";
    public static final String DELETE_BOARD_NOT_FOUND = "삭제할 게시판을 찾을 수 없습니다.";
    public static final String UPDATE_BOARD_NOT_FOUND = "수정할 게시판을 찾을 수 없습니다.";
    public static final String BOARD_NOT_FOUND = "게시판을 찾을 수 없습니다.";
    public static final String CIRCLE_NOT_FOUND = "동아리를 찾을 수 없습니다.";
    public static final String INQUIRY_NOT_FOUND = "문의글을 찾을 수 없습니다.";
    public static final String POST_NOT_FOUND = "게시판을 찾을 수 없습니다.";
    public static final String NOTICE_NOT_FOUND = "앱 공지 게시판을 찾을 수 없습니다.";

    public static String exceptionOccur(String target) {
        return target + " id checked, but exception occurred";
    }
}
