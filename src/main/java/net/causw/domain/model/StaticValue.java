package net.causw.domain.model;

public class StaticValue {
    public static final String CONTENT_DELETED_COMMENT = "삭제된 댓글입니다";
    public static final String BOARD_NAME_APP_NOTICE = "APP_NOTICE";

    public static final Integer CAUSW_CREATED = 1972;

    // Pagination
    public static final Integer DEFAULT_PAGE_SIZE = 20;
    public static final Integer DEFAULT_POST_PAGE_SIZE = 20;
    public static final Integer DEFAULT_COMMENT_PAGE_SIZE = 20;
    public static final Integer HOME_POST_PAGE_SIZE = 3;
    public final static Integer USER_LIST_PAGE_SIZE = 30;

    // JWT Token
    public static final Long JWT_TOKEN_VALID_TIME = 1000L * 60 * 60 * 24 * 7;
    public static final Integer JWT_ACCESS_THRESHOLD = 60 * 60 * 24;  // 1 day

    // Swagger configuration
    public static final String SWAGGER_API_NAME = "CAU_SW API";
    public static final String SWAGGER_API_VERSION = "0.0.1";
    public static final String SWAGGER_API_DESCRIPTION = "중앙대학교 소프트웨어학부 동문네트워크 API";

    // Google mail
    public static final String GOOGLE_MAIL = "causwdev2021@gmail.com";

    // Domain
    public static final String DOMAIN_BOARD = "게시판";
    public static final String DOMAIN_POST = "게시글";
    public static final String DOMAIN_CIRCLE = "소모임";
    public static final String DOMAIN_COMMENT = "댓글";
    public static final String DOMAIN_CHILD_COMMENT = "답글";
}
