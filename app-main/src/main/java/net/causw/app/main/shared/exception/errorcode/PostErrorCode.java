package net.causw.app.main.shared.exception.errorcode;

import org.springframework.http.HttpStatus;

import net.causw.app.main.shared.exception.BaseResponseCode;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public enum PostErrorCode implements BaseResponseCode {
	POST_NOT_FOUND(HttpStatus.NOT_FOUND, "POST_404_001", "게시글을 찾을 수 없습니다"),
	DELETED_WRITER(HttpStatus.NOT_FOUND, "POST_404_002", "작성자가 삭제된 사용자입니다"),
	POST_FORBIDDEN(HttpStatus.FORBIDDEN, "POST_403_001", "게시글에 대한 권한이 없습니다"),
	BLOCKED_USER_CONTENT(HttpStatus.FORBIDDEN, "POST_403_002", "차단한 유저의 컨텐츠입니다."),
	POST_ANONYMOUS_FORBIDDEN(HttpStatus.BAD_REQUEST, "POST_400_001", "비익명 게시판에서 익명으로 작성할 수 없습니다."),
	INVALID_CURSOR_FORMAT(HttpStatus.BAD_REQUEST, "POST_400_002", "올바르지 않은 커서 형식입니다."),
	IMAGE_REPRESENTATIVE_MUST_BE_ONE(HttpStatus.BAD_REQUEST, "POST_400_003", "대표 이미지는 정확히 1개여야 합니다."),
	IMAGE_ORDER_DUPLICATED(HttpStatus.BAD_REQUEST, "POST_400_004", "이미지 순서(order)가 중복됩니다."),
	IMAGE_FILE_INDEX_DUPLICATED(HttpStatus.BAD_REQUEST, "POST_400_005", "파일 인덱스(fileIndex)가 중복됩니다."),
	IMAGE_FILE_INDEX_OUT_OF_RANGE(HttpStatus.BAD_REQUEST, "POST_400_006", "파일 인덱스(fileIndex)가 업로드된 파일 수를 초과합니다."),
	IMAGE_EXISTING_URL_REQUIRED(HttpStatus.BAD_REQUEST, "POST_400_007", "type=existing인 이미지는 url이 필수입니다."),
	IMAGE_NEW_FILE_INDEX_REQUIRED(HttpStatus.BAD_REQUEST, "POST_400_008", "type=new인 이미지는 fileIndex가 필수입니다."),
	IMAGE_EXISTING_URL_NOT_FOUND(HttpStatus.BAD_REQUEST, "POST_400_009", "기존 게시글에 존재하지 않는 이미지 URL입니다."),
	POST_NOTICE_BOARD_NOT_ALLOW_ANONYMOUS(HttpStatus.BAD_REQUEST, "POST_400_010", "공지사항 게시판에서 익명으로 작성할 수 없습니다.");

	private final HttpStatus status;
	private final String code;
	private final String message;

	@Override
	public String getCode() {
		return code;
	}

	@Override
	public String getMessage() {
		return message;
	}

	@Override
	public HttpStatus getStatus() {
		return status;
	}
}
