package net.causw.global.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

// FIXME: 추후 MessageUtil에서 ResponseCode 형식으로 Code와 Message를 동시 관리하도록 수정해야 함
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MessageUtil {

	// 400
	public static final String INVALID_TOKEN = "잘못된 AccessToken 입니다";
	public static final String EXPIRED_TOKEN = "만료된 AccessToken 입니다";

	// Security
	public static final String ACCESS_DENIED = "접근이 거부되었습니다.";

	public static final String LOGIN_USER_NOT_FOUND = "로그인된 사용자를 찾을 수 없습니다.";
	public static final String BOARD_NOT_FOUND = "게시판을 찾을 수 없습니다.";

	// USER
	public static final String USER_NOT_FOUND = "해당 사용자를 찾을 수 없습니다.";
	public static final String USER_DROPPED_CONTACT_EMAIL = "추방된 계정입니다. 재가입 문의는 caucsedongne@gmail.com으로 연락해주세요";
	public static final String USER_INACTIVE_CAN_REJOIN = "탈퇴한 계정입니다. 계정 복구를 진행해주세요";
	public static final String DEPARTMENT_EXPLICITLY_REQUIRED = "2021년 이후 입학생은 학과/학부를 반드시 선택해야 합니다.";
	public static final String INVALID_ADMISSION_YEAR = "해당 입학년도에 맞는 학과를 찾을 수 없습니다.";

	// UuidFile
	public static final String FILE_NOT_FOUND = "파일을 찾을 수 없습니다.";
	public static final String FILE_IS_NULL = "파일이 비어있습니다.";
	public static final String FILE_SIZE_EXCEEDED = "파일 크기가 초과되었습니다.";
	public static final String NUMBER_OF_FILES_EXCEEDED = "파일 개수가 초과되었습니다.";
	public static final String FILE_NAME_IS_NULL = "파일 이름이 비어있습니다.";
	public static final String FILE_EXTENSION_IS_NULL = "파일 확장자가 비어있습니다.";
	public static final String INVALID_FILE_EXTENSION = "유효하지 않은 파일 확장자입니다.";

	public static final String FILE_UPLOAD_FAIL = "파일 업로드에 실패했습니다.";
	public static final String FILE_DELETE_FAIL = "파일 삭제에 실패했습니다.";
	public static final String BATCH_FAIL = "[배치 실행에 실패했습니다.]";

	public static final String FAIL_TO_CRAWL_CAU_SW_NOTICE_SITE = "소프트웨어학부 공지사항 크롤링 실패";

	//Hash
	public static final String HASH_ALGORITHM_NOT_FOUND = "SHA-256 알고리즘을 찾을 수 없습니다";
}
