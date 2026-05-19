package net.causw.app.main.domain.community.post.service.v2.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.causw.app.main.domain.community.board.entity.Board;
import net.causw.app.main.domain.community.board.entity.BoardConfig;
import net.causw.app.main.domain.community.board.entity.BoardReadScope;
import net.causw.app.main.domain.community.board.entity.BoardVisibility;
import net.causw.app.main.domain.community.board.entity.BoardWriteScope;
import net.causw.app.main.domain.community.post.entity.Post;
import net.causw.app.main.domain.community.post.service.v2.dto.ImageCreateMeta;
import net.causw.app.main.domain.community.post.service.v2.dto.ImageUpdateMeta;
import net.causw.app.main.domain.user.academic.enums.userAcademicRecord.AcademicStatus;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.enums.user.Role;
import net.causw.app.main.domain.user.account.enums.user.UserState;
import net.causw.app.main.shared.exception.errorcode.AuthErrorCode;
import net.causw.app.main.shared.exception.errorcode.BoardErrorCode;
import net.causw.app.main.shared.exception.errorcode.PostErrorCode;
import net.causw.app.main.shared.exception.errorcode.UserErrorCode;
import net.causw.global.constant.StaticValue;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PostValidator {

	public static void validateCreate(User creator, Board board, BoardConfig boardConfig,
		List<String> boardAdminIds, Boolean isAnonymous) {
		validateUserAndBoard(creator, board);
		validateWriteScope(creator, boardConfig, boardAdminIds);
		validateAnonymousBoard(boardConfig, isAnonymous);
	}

	public static void validateDelete(User deleter, Post post, List<String> adminIds) {
		if (post.getBoard().getCategory().equals(StaticValue.BOARD_NAME_APP_NOTICE)) {
			// 관리자 역할이 없고, 게시글의 작성자가 아니면 오류 발생
			if (!adminIds.contains(deleter.getId())
				&& !post.getWriter().getId().equals(deleter.getId())) {
				throw PostErrorCode.POST_FORBIDDEN.toBaseException();
			}
		}
		validateUserAndBoard(deleter, post.getBoard());
	}

	public static void validateUpdate(User updater, Post post, List<String> adminIds, BoardConfig boardConfig,
		Boolean isAnonymous) {
		validateUserAndBoard(updater, post.getBoard());

		// 게시글이 삭제된 경우
		if (post.getIsDeleted()) {
			throw PostErrorCode.POST_NOT_FOUND.toBaseException();
		}

		// 작성자만 수정 가능 (관리자도 타인의 게시글은 수정 불가)
		if (!post.getWriter().getId().equals(updater.getId())) {
			throw PostErrorCode.POST_FORBIDDEN.toBaseException();
		}

		// 익명 게시판 검증
		validateAnonymousBoard(boardConfig, isAnonymous);
	}

	public static void validateUserAndBoard(User user, Board board) {
		// User State 검증
		UserState userState = user.getState();
		if (userState == UserState.DROP) {
			throw UserErrorCode.USER_DROPPED.toBaseException();
		}
		if (user.isInactive()) {
			throw UserErrorCode.USER_INACTIVE_CAN_REJOIN.toBaseException();
		}

		// User Role 검증 (NONE 역할이 있으면 안됨)
		if (user.getRoles().contains(Role.NONE)) {
			throw AuthErrorCode.USER_ROLE_NONE.toBaseException();
		}

		// Board 삭제 여부 검증
		if (board.getIsDeleted()) {
			throw BoardErrorCode.BOARD_DELETED.toBaseException();
		}
	}

	private static void validateWriteScope(User creator, BoardConfig boardConfig, List<String> boardAdminIds) {
		BoardWriteScope writeScope = boardConfig.getWriteScope();
		if (writeScope == BoardWriteScope.ALL_USER) {
			return;
		}

		if (boardAdminIds.contains(creator.getId())) {
			return;
		}

		throw BoardErrorCode.BOARD_FORBIDDEN.toBaseException();
	}

	private static void validateAnonymousBoard(BoardConfig boardConfig, Boolean isAnonymous) {
		// 익명 게시판인데 비익명 게시글을 작성하려고 할 때 에러 발생
		if (boardConfig.isAnonymous() && Boolean.FALSE.equals(isAnonymous)) {
			throw PostErrorCode.POST_ANONYMOUS_BOARD_NOT_ALLOWED.toBaseException();
		}
	}

	public static void validateRead(User viewer, BoardConfig boardConfig, Collection<String> boardAdminIds) {
		// 게시판 관리자는 무조건 조회 가능
		if (boardAdminIds.contains(viewer.getId())) {
			return;
		}

		// BoardVisibility 체크 - HIDDEN인 경우 조회 불가
		if (boardConfig.getVisibility() == BoardVisibility.HIDDEN) {
			// 관리자는 조회 가능
			if (!boardAdminIds.contains(viewer.getId())) {
				throw BoardErrorCode.BOARD_FORBIDDEN.toBaseException();
			}
		}

		BoardReadScope readScope = boardConfig.getReadScope();
		AcademicStatus academicStatus = viewer.getAcademicStatus();

		// BOTH인 경우 모두 조회 가능
		if (readScope == BoardReadScope.BOTH) {
			return;
		}

		// ENROLLED인 경우 재학생만 가능
		if (readScope == BoardReadScope.ENROLLED) {
			if (academicStatus == AcademicStatus.ENROLLED
				|| academicStatus == AcademicStatus.LEAVE_OF_ABSENCE
				|| academicStatus == AcademicStatus.SUSPEND
				|| academicStatus == AcademicStatus.PROFESSOR) {
				return;
			}
			throw BoardErrorCode.BOARD_FORBIDDEN.toBaseException();
		}

		// GRADUATED인 경우 졸업생만 가능
		if (readScope == BoardReadScope.GRADUATED) {
			if (academicStatus == AcademicStatus.GRADUATED) {
				return;
			}
			throw BoardErrorCode.BOARD_FORBIDDEN.toBaseException();
		}

		throw BoardErrorCode.BOARD_FORBIDDEN.toBaseException();
	}

	// ── 이미지 메타데이터 검증 ──────────────────────────────────────────────

	/**
	 * 게시글 생성 시 이미지 메타데이터를 검증합니다.
	 *
	 * @param imageMetas 이미지 메타데이터 목록
	 * @param fileCount  업로드된 파일 수
	 */
	public static void validateCreateImageMetas(List<ImageCreateMeta> imageMetas, int fileCount) {
		if (imageMetas == null || imageMetas.isEmpty()) {
			return;
		}

		validateOrderUnique(imageMetas.stream().map(ImageCreateMeta::order).toList());

		Set<Integer> fileIndices = new HashSet<>();
		for (ImageCreateMeta meta : imageMetas) {
			// fileIndex 범위 검증
			if (meta.fileIndex() < 0 || meta.fileIndex() >= fileCount) {
				throw PostErrorCode.IMAGE_FILE_INDEX_OUT_OF_RANGE.toBaseException();
			}
			// fileIndex 중복 검증
			if (!fileIndices.add(meta.fileIndex())) {
				throw PostErrorCode.IMAGE_FILE_INDEX_DUPLICATED.toBaseException();
			}
		}

		validateRepresentativeCount(
			imageMetas.stream().filter(ImageCreateMeta::isRepresentative).count());
	}

	/**
	 * 게시글 수정 시 이미지 메타데이터를 검증합니다.
	 *
	 * @param imageMetas       이미지 메타데이터 목록
	 * @param newFileCount     새로 업로드된 파일 수
	 * @param existingImageUrls 기존 게시글의 이미지 URL 집합
	 */
	public static void validateUpdateImageMetas(List<ImageUpdateMeta> imageMetas, int newFileCount,
		Set<String> existingImageUrls) {
		if (imageMetas == null || imageMetas.isEmpty()) {
			return;
		}

		validateOrderUnique(imageMetas.stream().map(ImageUpdateMeta::order).toList());

		Set<Integer> newFileIndices = new HashSet<>();
		for (ImageUpdateMeta meta : imageMetas) {
			if (meta.type() == ImageUpdateMeta.Type.EXISTING) {
				// url 필수 검증
				if (meta.url() == null || meta.url().isBlank()) {
					throw PostErrorCode.IMAGE_EXISTING_URL_REQUIRED.toBaseException();
				}
				// url이 실제 기존 이미지에 존재하는지 검증
				if (!existingImageUrls.contains(meta.url())) {
					throw PostErrorCode.IMAGE_EXISTING_URL_NOT_FOUND.toBaseException();
				}
			} else {
				// fileIndex 필수 검증
				if (meta.fileIndex() == null) {
					throw PostErrorCode.IMAGE_NEW_FILE_INDEX_REQUIRED.toBaseException();
				}
				// fileIndex 범위 검증
				if (meta.fileIndex() < 0 || meta.fileIndex() >= newFileCount) {
					throw PostErrorCode.IMAGE_FILE_INDEX_OUT_OF_RANGE.toBaseException();
				}
				// fileIndex 중복 검증
				if (!newFileIndices.add(meta.fileIndex())) {
					throw PostErrorCode.IMAGE_FILE_INDEX_DUPLICATED.toBaseException();
				}
			}
		}

		validateRepresentativeCount(
			imageMetas.stream().filter(ImageUpdateMeta::isRepresentative).count());
	}

	private static void validateOrderUnique(List<Integer> orders) {
		if (orders.size() != new HashSet<>(orders).size()) {
			throw PostErrorCode.IMAGE_ORDER_DUPLICATED.toBaseException();
		}
	}

	private static void validateRepresentativeCount(long count) {
		if (count != 1) {
			throw PostErrorCode.IMAGE_REPRESENTATIVE_MUST_BE_ONE.toBaseException();
		}
	}
}
