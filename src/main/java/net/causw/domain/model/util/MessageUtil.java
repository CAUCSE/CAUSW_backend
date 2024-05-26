package net.causw.domain.model.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

// FIXME: 추후 MessageUtil에서 ResponseCode 형식으로 Code와 Message를 동시 관리하도록 수정해야 함
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MessageUtil {

    // 400
    public static final String INVALID_PARAMETER = "Invalid action parameter";
    public static final String INVALID_TOKEN = "RefreshToken 유효성 검증 실패";

    // Circle & Post
    public static final String NOT_CIRCLE_LEADER = "사용자가 해당 동아리의 동아리장이 아닙니다.";
    public static final String NOT_CIRCLE_MEMBER = "로그인된 사용자가 동아리 멤버가 아닙니다.";
    public static final String LOGIN_USER_NOT_FOUND = "로그인된 사용자를 찾을 수 없습니다.";
    public static final String BOARD_NOT_FOUND = "게시판을 찾을 수 없습니다.";
    public static final String CIRCLE_LEADER_NOR_FOUND = "해당 동아리의 동아리장을 찾을 수 없습니다.";
    public static final String CIRCLE_MEMBER_NOT_FOUND = "동아리를 찾을 수 없습니다.";
    public static final String INQUIRY_NOT_FOUND = "문의글을 찾을 수 없습니다.";
    public static final String POST_NOT_FOUND = "게시글을 찾을 수 없습니다.";
    public static final String COMMENT_NOT_FOUND = "댓글을 찾을 수 없습니다.";
    public static final String NOTICE_NOT_FOUND = "앱 공지 게시판을 찾을 수 없습니다.";
    public static final String SMALL_CLUB_NOT_FOUND = "소모임을 찾을 수 없습니다.";
    public static final String USER_APPLY_NOT_FOUND = "사용자의 가입 신청을 찾을 수 없습니다.";
    public static final String CIRCLE_WITHOUT_LEADER = "The board has circle without circle leader";
    public static final String CIRCLE_APPLY_INVALID = "사용자가 가입 신청한 소모임이 아닙니다.";
    public static final String CIRCLE_DUPLICATE_NAME = "중복된 소모임 이름입니다.";

    // Locker
    public static final String LOCKER_WRONG_POSITION = "등록된 사물함 위치가 아닙니다.";
    public static final String LOCKER_FIRST_CREATED = "사물함 최초 생성";
    public static final String LOCKER_NOT_FOUND = "사물함을 찾을 수 없습니다.";
    public static final String LOCKER_DUPLICATE_NUMBER = "중복된 사물함 번호입니다.";
    public static final String LOCKER_ALREADY_REGISTERED = "이미 등록된 사물함 위치입니다.";
    public static final String LOCKER_ALREADY_EXIST = "사물함 위치에 사물함이 존재합니다.";
    public static final String LOCKER_RETURN_TIME_NOT_SET = "사물함 반납 시간이 설정되지 않았습니다.";
    public static final String LOCKER_UNUSED = "사용 중인 사물함이 아닙니다.";
    public static final String LOCKER_DELETED = "사물함 삭제";
    public static final String LOCKER_ACTION_ERROR = "사물함 액션 실행 중 에러가 발생하였습니다.";


    // User
    public static final String USER_NOT_FOUND = "해당 사용자를 찾을 수 없습니다.";
    public static final String ADMISSION_EXCEPTION = "User id of the admission checked, but exception occurred";
    public static final String NO_ASSIGNED_CIRCLE_FOR_LEADER = "해당 동아리장이 배정된 동아리가 없습니다.";
    public static final String CIRCLE_ID_REQUIRED_FOR_LEADER_DELEGATION = "소모임장을 위임할 소모임 입력이 필요합니다.";
    public static final String EMAIL_ALREADY_EXIST = "이미 존재하는 이메일입니다.";
    public static final String EMAIL_INVALID = "잘못된 이메일입니다.";
    public static final String USER_ALREADY_APPLY = "이미 신청한 사용자 입니다.";
    public static final String NO_APPLICATION = "신청서를 작성하지 않았습니다.";
    public static final String CONCURRENT_JOB_IMPOSSIBLE = "부회장은 동아리장 겸직이 불가합니다.";

    // Flag
    public static final String FLAG_UPDATE_FAILED = "플래그 업데이트에 실패했습니다.";
    public static final String FLAG_ALREADY_EXIST = "이미 존재하는 플래그 입니다.";

    // 500
    public static final String INTERNAL_SERVER_ERROR = "Internal Server Error";
}
