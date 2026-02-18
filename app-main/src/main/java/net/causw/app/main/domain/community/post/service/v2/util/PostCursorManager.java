package net.causw.app.main.domain.community.post.service.v2.util;

import java.time.LocalDateTime;

import net.causw.app.main.shared.exception.errorcode.PostErrorCode;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 게시글 커서 기반 페이징을 위한 커서 파싱 및 생성 유틸리티 클래스
 * 커서 형식: "createdAt|postId"
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PostCursorManager {

	private static final String CURSOR_DELIMITER = "|";
	private static final int EXPECTED_PARTS_LENGTH = 2;
	private static final int CREATED_AT_INDEX = 0;
	private static final int POST_ID_INDEX = 1;

	/**
	 * 커서 문자열을 파싱하여 ParsedCursor 객체로 변환합니다.
	 *
	 * @param cursor 커서 문자열 (null 또는 빈 문자열 가능)
	 * @return ParsedCursor 객체 (커서가 없으면 null 값을 가진 객체 반환)
	 */
	public static ParsedCursor parseCursor(String cursor) {
		if (cursor == null || cursor.isBlank()) {
			return ParsedCursor.empty();
		}

		String[] parts = cursor.split("\\" + CURSOR_DELIMITER);
		if (parts.length != EXPECTED_PARTS_LENGTH) {
			throw PostErrorCode.INVALID_CURSOR_FORMAT.toBaseException();
		}

		return ParsedCursor.of(parts[CREATED_AT_INDEX], parts[POST_ID_INDEX]);
	}

	/**
	 * createdAt과 postId를 기반으로 다음 커서를 생성합니다.
	 *
	 * @param createdAt 게시글 생성 일시
	 * @param postId 게시글 ID
	 * @return 생성된 커서 문자열 (createdAt|postId 형식)
	 */
	public static String createNextCursor(LocalDateTime createdAt, String postId) {
		if (createdAt == null || postId == null) {
			return null;
		}
		return createdAt.toString() + CURSOR_DELIMITER + postId;
	}

	/**
	 * 파싱된 커서 정보를 담는 레코드 클래스
	 */
	public record ParsedCursor(
		String createdAt,
		String postId) {
		public static ParsedCursor of(String createdAt, String postId) {
			return new ParsedCursor(createdAt, postId);
		}

		public static ParsedCursor empty() {
			return new ParsedCursor(null, null);
		}

		public boolean isEmpty() {
			return createdAt == null && postId == null;
		}
	}
}
