package net.causw.app.main.domain.community.post.service.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.causw.app.main.domain.community.board.entity.Board;
import net.causw.app.main.domain.community.board.entity.BoardConfig;
import net.causw.app.main.domain.community.common.service.CommunityPermissionPolicy;
import net.causw.app.main.domain.community.post.entity.Post;
import net.causw.app.main.domain.community.post.service.dto.ImageCreateMeta;
import net.causw.app.main.domain.community.post.service.dto.ImageUpdateMeta;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.shared.exception.errorcode.BoardErrorCode;
import net.causw.app.main.shared.exception.errorcode.PostErrorCode;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PostValidator {

	public static void validateCreate(User creator, Board board, BoardConfig boardConfig,
		List<String> boardAdminIds, Boolean isAnonymous) {
		CommunityPermissionPolicy.validateActiveUser(creator);
		validateBoardAlive(board);
		if (!CommunityPermissionPolicy.canWriteBoard(creator, board, boardConfig, boardAdminIds)) {
			throw BoardErrorCode.BOARD_FORBIDDEN.toBaseException();
		}
		validateAnonymousBoard(boardConfig, isAnonymous);
	}

	public static void validateDelete(User deleter, Post post, List<String> boardAdminIds,
		BoardConfig boardConfig) {
		CommunityPermissionPolicy.validateActiveUser(deleter);
		validatePostAlive(post);
		if (!CommunityPermissionPolicy.canDeletePost(deleter, post, boardConfig, boardAdminIds)) {
			throw PostErrorCode.POST_FORBIDDEN.toBaseException();
		}
	}

	public static void validateUpdate(User updater, Post post, List<String> adminIds, BoardConfig boardConfig,
		Boolean isAnonymous) {
		CommunityPermissionPolicy.validateActiveUser(updater);
		validatePostAlive(post);
		if (!CommunityPermissionPolicy.canUpdatePost(updater, post, boardConfig, adminIds)) {
			throw PostErrorCode.POST_FORBIDDEN.toBaseException();
		}
		validateAnonymousBoard(boardConfig, isAnonymous);
	}

	private static void validateAnonymousBoard(BoardConfig boardConfig, Boolean isAnonymous) {
		// 공지 게시판에서는 익명 게시글 작성 불가
		if (boardConfig.isNotice() && Boolean.TRUE.equals(isAnonymous)) {
			throw PostErrorCode.POST_NOTICE_BOARD_NOT_ALLOW_ANONYMOUS.toBaseException();
		}

		// 익명 비허용 게시판에서 익명 게시글 작성 불가
		if (!boardConfig.isAnonymous() && Boolean.TRUE.equals(isAnonymous)) {
			throw PostErrorCode.POST_ANONYMOUS_FORBIDDEN.toBaseException();
		}
	}

	public static void validateRead(User viewer, Board board, BoardConfig boardConfig,
		Collection<String> boardAdminIds) {
		CommunityPermissionPolicy.validateActiveUser(viewer);
		validateBoardAlive(board);
		if (!CommunityPermissionPolicy.canReadBoard(viewer, board, boardConfig, boardAdminIds)) {
			throw BoardErrorCode.BOARD_FORBIDDEN.toBaseException();
		}
	}

	public static void validateRead(User viewer, Post post, BoardConfig boardConfig,
		Collection<String> boardAdminIds) {
		CommunityPermissionPolicy.validateActiveUser(viewer);
		validatePostAlive(post);
		if (!CommunityPermissionPolicy.canReadPost(viewer, post, boardConfig, boardAdminIds)) {
			throw BoardErrorCode.BOARD_FORBIDDEN.toBaseException();
		}
	}

	private static void validateBoardAlive(Board board) {
		if (!CommunityPermissionPolicy.isAlive(board)) {
			throw BoardErrorCode.BOARD_DELETED.toBaseException();
		}
	}

	private static void validatePostAlive(Post post) {
		if (post == null || Boolean.TRUE.equals(post.getIsDeleted())) {
			throw PostErrorCode.POST_NOT_FOUND.toBaseException();
		}
		validateBoardAlive(post.getBoard());
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
