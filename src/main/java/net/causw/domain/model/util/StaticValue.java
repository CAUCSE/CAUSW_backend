package net.causw.domain.model.util;

import java.util.List;

public class StaticValue {
    public static final String CONTENT_DELETED_COMMENT = "삭제된 댓글입니다";
    public static final String BOARD_NAME_APP_NOTICE = "APP_NOTICE";

    public static final String BOARD_NAME_APP_FREE = "APP_FREE";

    public static final Integer CAUSW_CREATED = 1972;
    public static final Integer MAX_NUM_FILE_ATTACHMENTS = 3;

    // Pagination
    public static final Integer DEFAULT_PAGE_SIZE = 20;
    public static final Integer DEFAULT_POST_PAGE_SIZE = 20;
    public static final Integer DEFAULT_COMMENT_PAGE_SIZE = 20;
    public static final Integer HOME_POST_PAGE_SIZE = 3;
    public final static Integer USER_LIST_PAGE_SIZE = 30;

    // Event
    public static final Integer MAX_NUM_EVENT = 10;

    // JWT Token
    public static final Long JWT_ACCESS_TOKEN_VALID_TIME = 1000L * 60 * 30;    // 30min
    public static final Long JWT_REFRESH_TOKEN_VALID_TIME = 1000L * 60 * 60 * 24 * 7;   // 7day
    public static final Integer JWT_ACCESS_THRESHOLD = 60 * 60 * 24;  // 1 day

    // Swagger configuration
    public static final String SWAGGER_API_NAME = "CAU_SW API";
    public static final String SWAGGER_API_VERSION = "0.0.1";
    public static final String SWAGGER_API_DESCRIPTION = "2024 RENEW ver. 중앙대학교 소프트웨어학부 동문네트워크 BACKEND API 스웨거 문서입니다.";

    // UuidFile
    // 파일 당 크기 제한
    public static final Long USER_PROFILE_IMAGE_SIZE = (long) (10 * 1024 * 1024);
    public static final Long USER_ADMISSION_IMAGE_SIZE = (long) (10 * 1024 * 1024);
    public static final Long USER_ACADEMIC_RECORD_APPLICATION_IMAGE_SIZE = (long) (10 * 1024 * 1024);
    public static final Long CIRCLE_PROFILE_IMAGE_SIZE = (long) (10 * 1024 * 1024);
    public static final Long POST_IMAGE_SIZE = (long) (10 * 1024 * 1024);
    public static final Long CALENDAR_IMAGE_SIZE = (long) (50 * 1024 * 1024);
    public static final Long EVENT_IMAGE_SIZE = (long) (50 * 1024 * 1024);
    public static final Long ETC_FILE_SIZE = (long) (100 * 1024 * 1024);
    // 파일 개수 제한
    public static final Integer MAX_NUM_USER_PROFILE_IMAGE = 1;
    public static final Integer MAX_NUM_USER_ADMISSION_IMAGE = 5;
    public static final Integer MAX_NUM_USER_ACADEMIC_RECORD_APPLICATION_IMAGE = 5;
    public static final Integer MAX_NUM_CIRCLE_PROFILE_IMAGE = 1;
    public static final Integer MAX_NUM_POST_IMAGE = 10;
    public static final Integer MAX_NUM_CALENDAR_IMAGE = 1;
    public static final Integer MAX_NUM_EVENT_IMAGE = 1;
    public static final Integer MAX_NUM_ETC_FILE = 10;
    // 파일 확장자 제한
    public static final List<String> IMAGE_FILE_EXTENSION_LIST = List.of("JPEG", "JPG", "PNG");

    // Domain
    public static final String DOMAIN_BOARD = "게시판";
    public static final String DOMAIN_POST = "게시글";
    public static final String DOMAIN_CIRCLE = "소모임";
    public static final String DOMAIN_COMMENT = "댓글";
    public static final String DOMAIN_CHILD_COMMENT = "답글";
    public static final String DOMAIN_INQUIRY = "문의글";

    // Flag
    public static final String LOCKER_ACCESS = "LOCKER_ACCESS";
    public static final String LOCKER_EXTEND = "LOCKER_EXTEND";

    // Text Field
    public static final String EXPIRED_AT = "EXPIRE_DATE";

    // UserAcademicRecordApplication
    public static final String USER_APPLIED = "[사용자 신청]";
    public static final String USER_CLOSED = "인증 서류 재제출로 인한 신청 서류 종료";

    // CleanUnusedUuidFile
    public static final Integer SELECT_UNUSED_UUID_FILE_PAGE_SIZE = 1000;
    public static final Long CLEAN_UNUSED_UUID_FILE_REDIS_EXPIRED_TIME = 1000L * 60 * 60;   // 1hour
}
