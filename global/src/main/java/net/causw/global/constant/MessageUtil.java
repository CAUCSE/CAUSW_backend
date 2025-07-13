package net.causw.global.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

// FIXME: 추후 MessageUtil에서 ResponseCode 형식으로 Code와 Message를 동시 관리하도록 수정해야 함
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MessageUtil {

    // 400
    public static final String INVALID_PARAMETER = "Invalid action parameter";
    public static final String INVALID_REFRESH_TOKEN = "RefreshToken 유효성 검증 실패";
    public static final String INVALID_TOKEN = "잘못된 AccessToken 입니다";
    public static final String EXPIRED_TOKEN = "만료된 AccessToken 입니다";
    public static final String DOES_NOT_HAVE_PERMISSION = "권한이 없습니다.";
    public static final String INVALID_USER_DATA_REQUEST = "유저의 가입 및 수정 정보가 유효하지 않습니다.";

    // Form
    public static final String IS_NEED_COUNCIL_FEE_REQUIRED = "학생회비 납부 여부를 선택해 주세요.";
    public static final String INVALID_REGISTERED_SEMESTER_INFO = "재학/휴학생 답변 가능을 선택했다면 답변할 수 있는 등록 완료 학기에 대한 정보가 필요합니다.";
    public static final String INVALID_QUESTION_INFO = "유효하지 않은 문항 정보입니다.";
    public static final String EMPTY_QUESTION_INFO = "문항 정보가 비어있습니다.";
    public static final String EMPTY_OPTION_INFO = "문항 옵션 정보가 비어있습니다.";
    public static final String FORM_NOT_FOUND = "신청 폼을 찾을 수 없습니다.";
    public static final String QUESTION_NOT_FOUND = "질문을 찾을 수 없습니다.";
    public static final String OPTION_NOT_FOUND = "해당 문항을 찾을 수 없습니다.";
    public static final String NOT_ALLOWED_TO_REPLY_FORM = "답변이 허용되지 않은 사용자입니다.";
    public static final String INVALID_REPLY_INFO = "유효하지 않은 신청서 답변 정보입니다.";
    public static final String ALREADY_REPLIED = "이미 답변한 신청서입니다.";
    public static final String NOT_ALLOWED_TO_ACCESS_REPLY = "신청서 답변을 조회할 수 없는 사용자입니다.";
    public static final String FORM_CLOSED = "신청 기간이 종료되었습니다.";
    public static final String REPLY_SIZE_INVALID = "답변 개수가 유효하지 않습니다.";

    // Calendar & Event
    public static final String CALENDAR_NOT_FOUND = "캘린더를 찾을 수 없습니다.";
    public static final String CALENDAR_ALREADY_EXIST = "이미 존재하는 캘린더 입니다.";
    public static final String EVENT_NOT_FOUND = "이벤트를 찾을 수 없습니다.";
    public static final String EVENT_MAX_CREATED = "10개의 이벤트가 이미 존재합니다.";

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
    public static final String NEW_CIRCLE_LEADER_NOT_FOUND = "등록할 동아리장을 다시 확인해주세요.";
    public static final String CIRCLE_NOT_FOUND = "해당 동아리를 찾을 수 없습니다.";
    public static final String POST_DELETED = "삭제된 게시물입니다.";
    public static final String BOARD_NAME_ALREADY_EXISTS = "게시판 이름이 이미 존재합니다.";
    public static final String INVALID_BOARD_CATEGORY = "유효하지 않은 게시판 카테고리입니다.";

    // Like & favorite
    public static final String POST_ALREADY_LIKED = "좋아요를 이미 누른 게시글 입니다.";
    public static final String POST_NOT_LIKED = "좋아요을 누르지 않은 게시글입니다.";
    public static final String POST_ALREADY_FAVORITED = "즐겨찾기를 이미 누른 게시글 입니다.";
    public static final String COMMENT_ALREADY_LIKED = "좋아요를 이미 누른 댓글입니다.";
    public static final String CHILD_COMMENT_ALREADY_LIKED = "좋아요를 이미 누른 대댓글입니다.";
    public static final String FAVORITE_POST_NOT_FOUND = "즐겨찾기가 되어 있지 않습니다.";
    public static final String FAVORITE_POST_ALREADY_DELETED = "즐겨찾기가 이미 취소되어 있습니다.";

    // BoardApply
    public static final String APPLY_ALREADY_ACCEPTED = "이미 승인된 게시판 신청입니다.";
    public static final String APPLY_ALREADY_REJECTED = "이미 거부된 게시판 신청입니다.";
    public static final String APPLY_NOT_FOUND = "게시판 신청을 찾을 수 없습니다.";

    // Locker
    public static final String LOCKER_WRONG_POSITION = "등록된 사물함 위치가 아닙니다.";
    public static final String LOCKER_FIRST_CREATED = "사물함 최초 생성";
    public static final String LOCKER_NOT_FOUND = "사물함을 찾을 수 없습니다.";
    public static final String LOCKER_DUPLICATE_NUMBER = "중복된 사물함 번호입니다.";
    public static final String LOCKER_ALREADY_REGISTERED = "이미 등록된 사물함 위치입니다.";
    public static final String LOCKER_ALREADY_EXIST = "사물함 위치에 사물함이 존재합니다.";
    public static final String LOCKER_RETURN_TIME_NOT_SET = "사물함 반납 시간이 설정되지 않았습니다.";
    public static final String LOCKER_UNUSED = "사용 중인 사물함이 아닙니다.";
    public static final String LOCKER_USED = "사용 중인 사물함입니다.";
    public static final String LOCKER_DELETED = "사물함 삭제";
    public static final String LOCKER_ACTION_ERROR = "사물함 액션 실행 중 에러가 발생하였습니다.";
    public static final String LOCKER_EXTEND_NOT_ALLOWED = "사물함 연장 신청 기간이 아닙니다. 공지를 확인해주세요.";
    public static final String LOCKER_INVALID_EXPIRE_DATE = "잘못된 반납일 입니다.";


    // User
    public static final String API_NOT_ACCESSIBLE = "접근 권한이 없습니다.";
    public static final String USER_NOT_FOUND = "해당 사용자를 찾을 수 없습니다.";
    public static final String ADMISSION_EXCEPTION = "User id of the admission checked, but exception occurred";
    public static final String NO_ASSIGNED_CIRCLE_FOR_LEADER = "해당 동아리장이 배정된 동아리가 없습니다.";
    public static final String CIRCLE_ID_REQUIRED_FOR_LEADER_DELEGATION = "소모임장을 위임할 소모임 입력이 필요합니다.";
    public static final String EMAIL_ALREADY_EXIST = "이미 존재하는 이메일입니다.";
    public static final String EMAIL_INVALID = "잘못된 이메일입니다.";
    public static final String USER_ALREADY_APPLY = "이미 신청한 사용자 입니다.";
    public static final String NO_APPLICATION = "신청서를 작성하지 않았습니다.";
    public static final String CONCURRENT_JOB_IMPOSSIBLE = "겸직이 불가합니다.";
    public static final String NICKNAME_ALREADY_EXIST = "이미 존재하는 닉네임입니다.";
    public static final String USER_ADMISSION_MUST_HAVE_IMAGE = "입학 증빙 사진은 필수 입니다.";
    public static final String STUDENT_ID_ALREADY_EXIST = "이미 존재하는 학번입니다.";
    public static final String PHONE_NUMBER_ALREADY_EXIST = "이미 존재하는 전화번호입니다.";
    public static final String INVALID_USER_APPLICATION_USER_STATE = "사용자 인증을 할 수 없는 상태의 사용자입니다(AWAIT / REJECT 상태만 인증 가능합니다).";
    public static final String GRANT_ROLE_NOT_ALLOWED = "권한을 부여할 수 없습니다.";
    public static final String DELEGATE_ROLE_NOT_ALLOWED = "권한을 위임할 수 없습니다.";

    // Flag
    public static final String FLAG_UPDATE_FAILED = "플래그 업데이트에 실패했습니다.";
    public static final String FLAG_ALREADY_EXIST = "이미 존재하는 플래그 입니다.";

    // UserAcademicRecord
    public static final String USER_ACADEMIC_RECORD_APPLICATION_NOT_FOUND = "해당 사용자의 학적 변경 신청을 찾을 수 없습니다.";
    public static final String USER_ACADEMIC_RECORD_APPLICATION_AND_USER_NOT_MATCH = "해당 사용자의 학적 변경 신청이 아닙니다.";
    public static final String INVALID_ACADEMIC_RECORD_REQUEST_STATUS = "유효하지 않은 학적 인증 변경 목표 상태입니다. (ACCEPT, REJECT 로만 변경이 가능합니다.)";

    public static final String ADMIN_UPDATE_ACADEMIC_RECORD_MESSAGE = "관리자 업데이트";

    // UserAcademicRecordApplication
    public static final String INVALID_TARGET_COMPLETED_SEMESTER = "유효하지 않은 목표 학기입니다.";
    public static final String INVALID_TARGET_ACADEMIC_STATUS = "유효하지 않은 목표 학적 상태입니다.";
    public static final String FILE_UPLOAD_NOT_ALLOWED = "파일 업로드가 허용되지 않습니다.";
    public static final String FILE_UPLOAD_REQUIRED = "파일 업로드가 필요합니다.";
    public static final String USER_ACADEMIC_RECORD_APPLICATION_DUPLICATED = "대기 중인 학적 인증 신청이 두 개 이상 존재합니다.";
    public static final String TARGET_CURRENT_COMPLETED_SEMESTER_NOT_EXIST = "목표 현재 학기가 필요합니다.";
    public static final String GRADUATION_INFORMATION_NOT_EXIST = "졸업 정보가 필요합니다.";
    public static final String USER_NOTE_NOW_ALLOWED = "사용자가 노트를 작성할 수 없습니다.";

    // UuidFile
    public static final String FILE_NOT_FOUND = "파일을 찾을 수 없습니다.";
    public static final String FILE_IS_NULL = "파일이 비어있습니다.";
    public static final String IMAGE_MUST_NOT_NULL = "이미지는 필수입니다.";
    public static final String INVALID_FILE_PATH = "유효하지 않은 파일 경로입니다.";
    public static final String FILE_SIZE_EXCEEDED = "파일 크기가 초과되었습니다.";
    public static final String NUMBER_OF_FILES_EXCEEDED = "파일 개수가 초과되었습니다.";
    public static final String FILE_NAME_IS_NULL = "파일 이름이 비어있습니다.";
    public static final String FILE_EXTENSION_IS_NULL = "파일 확장자가 비어있습니다.";
    public static final String INVALID_FILE_EXTENSION = "유효하지 않은 파일 확장자입니다.";

    // Semester
    public static final String PRIOR_SEMESTER_NOT_FOUND = "이전 학기를 찾을 수 없습니다.";
    public static final String ACTIVE_SEMESTER_IS_DUPLICATED = "활성화된 학기가 중복되어 있습니다.";
    public static final String CURRENT_SEMESTER_DOES_NOT_EXIST = "현재 학기가 존재하지 않습니다. 현재 학기를 등록해주세요";

    // UserCouncilFee
    public static final String INVALID_USER_COUNCIL_FEE_INFO = "유효하지 않은 학생회비 납부자 정보입니다.";
    public static final String USER_COUNCIL_FEE_NOT_FOUND = "해당 학생회비 납부자 정보를 찾을 수 없습니다.";
    public static final String USER_ALREADY_EXISTS = "해당 사용자는 이미 동문네트워크 서비스에 가입했습니다.";
    public static final String REFUND_DATE_IS_NULL = "환불 날짜가 비어있습니다.";
    public static final String USER_COUNCIL_FEE_INFO_ALREADY_EXISTS = "이미 등록된 학생회비 납부자 정보입니다.";

    // CouncilFeeFakeUser
    public static final String INVALID_COUNCIL_FEE_FAKE_USER_INFO = "유효하지 않은 가짜 사용자 정보입니다.";

    // Ceremony
    public static final String CEREMONY_NOT_FOUND = "존재하지 않는 경조사입니다.";
    public static final String CEREMONY_NOTIFICATION_SETTING_NOT_FOUND = "유저의 경조사 알람 설정이 되어있지 않습니다.";

    // Notification
    public static final String NOTIFICATION_NOT_FOUND = "존재하지 않는 알람입니다.";
    public static final String NOTIFICATION_LOG_NOT_FOUND = "존재하지 않는 알람 로그입니다.";


    // Vote
    public static final String VOTE_OPTION_NOT_FOUND = "존재하지 않는 투표 옵션입니다.";
    public static final String VOTE_OPTION_NOT_PROVIDED = "투표 옵션이 제공되지 않았습니다.";
    public static final String VOTE_NOT_MULTIPLE = "이 투표는 여러 항목을 선택할 수 없습니다.";
    public static final String VOTE_ALREADY_DONE = "해당 투표에 이미 참여한 이력이 있습니다.";
    public static final String VOTE_ALREADY_END = "이미 종료된 투표입니다.";
    public static final String VOTE_NOT_END = "종료되지 않은 투표입니다.";

    public static final String VOTE_NOT_FOUND = "투표가 존재하지 않습니다.";
    public static final String VOTE_END_NOT_ACCESSIBLE = "투표 종료 권한이 존재하지 않습니다.";
    public static final String VOTE_RESTART_NOT_ACCESSIBLE = "투표 재시작 권한이 존재하지 않습니다.";
    public static final String VOTE_START_NOT_ACCESSIBLE = "투표 시작 권한이 존재하지 않습니다.";
    // 500
    public static final String INTERNAL_SERVER_ERROR = "Internal Server Error";
    public static final String FILE_UPLOAD_FAIL = "파일 업로드에 실패했습니다.";
    public static final String FILE_DELETE_FAIL = "파일 삭제에 실패했습니다.";
    public static final String FAIL_TO_GENERATE_EXCEL_FILE = "엑셀 파일 생성에 실패했습니다.";
    public static final String BATCH_FAIL = "[배치 실행에 실패했습니다.]";

    public static final String FAIL_TO_CRAWL_CAU_SW_NOTICE_SITE = "소프트웨어학부 공지사항 크롤링 실패";

    public static final String USER_CURRENT_COMPLETE_SEMESTER_DOES_NOT_EXIST = "사용자의 현재 등록 완료 학기 정보가 존재하지 않습니다.";

    //Hash
    public static final String HASH_ALGORITHM_NOT_FOUND = "SHA-256 알고리즘을 찾을 수 없습니다";
}
