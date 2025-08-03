package net.causw.global.constant;

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
    public static final Integer ALUMNI_HOME_POST_PAGE_SIZE = 4;
    public final static Integer USER_LIST_PAGE_SIZE = 30;
    public final static Integer DEFAULT_NOTIFICATION_PAGE_SIZE = 10;
    public final static Integer SIDE_NOTIFICATION_PAGE_SIZE = 4;
    public final static Integer MAX_NOTIFICATION_COUNT = 10;


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
    public static final Integer SELECT_UNUSED_UUID_FILE_PAGE_SIZE = 10000;
    public static final Long CLEAN_UNUSED_UUID_FILE_REDIS_EXPIRED_TIME = 1000L * 60 * 60;   // 1hour

    // Crawling
    public static final String CrawlingBoard = "소프트웨어학부 학부 공지";
    public static final String ORIGINAL_NOTICE_SITE_NAME = "중앙대학교 소프트웨어학부 공지사항";
    public static final String CAU_CSE_BASE_URL = "https://cse.cau.ac.kr/sub05/sub0501.php?offset=";
    public static final String CAU_CSE_DOWNLOAD_URL_FORMAT = "https://cse.cau.ac.kr/_module/bbs/download.php?uid=%s&code=%s";
    public static final String ADMIN_STUDENT_ID = "20220881";
    public static final int CRAWLING_MAX_NOTICES = 30;
    public static final int CRAWLING_MAX_RETRIES = 3;
    public static final int CRAWLING_REQUEST_DELAY_MS = 2000;
    public static final String[] CRAWLING_USER_AGENTS = {
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:121.0) Gecko/20100101 Firefox/121.0",
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.1 Safari/605.1.15",
        "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
    };

    // Hash
    public static final String HASH_ALGORITHM = "SHA-256";
}
