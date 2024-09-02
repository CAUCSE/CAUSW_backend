package net.causw.domain.model.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MessageUtil {

    // Locker
    public static final String LOCKER_USED = "사용 중인 사물함입니다.";
    public static final String LOCKER_ALREADY_EXTENDED = "이미 사물함 반납을 연장하였습니다.";
    public static final String LOCKER_EXTEND_NOT_ALLOWED = "사물함 연장 신청 기간이 아닙니다. 공지를 확인해주세요.";
    public static final String LOCKER_INVALID_EXPIRE_DATE = "잘못된 반납일 입니다.";
    public static final String LOCKER_EXTEND_START_AT_NOT_SET = "사물함 반납 연장 시작일이 설정되지 않았습니다.";
    public static final String LOCKER_EXTEND_END_AT_NOT_SET = "사물함 반납 연장 종료일이 설정되지 않았습니다.";

}
