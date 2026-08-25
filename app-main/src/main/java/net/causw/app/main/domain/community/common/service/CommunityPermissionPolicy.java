package net.causw.app.main.domain.community.common.service;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;

import net.causw.app.main.domain.community.board.entity.Board;
import net.causw.app.main.domain.community.board.entity.BoardConfig;
import net.causw.app.main.domain.community.board.entity.BoardReadScope;
import net.causw.app.main.domain.community.board.entity.BoardVisibility;
import net.causw.app.main.domain.community.board.entity.BoardWriteScope;
import net.causw.app.main.domain.community.comment.entity.Comment;
import net.causw.app.main.domain.community.post.entity.Post;
import net.causw.app.main.domain.user.academic.enums.userAcademicRecord.AcademicStatus;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.enums.user.Department;
import net.causw.app.main.domain.user.account.enums.user.Role;
import net.causw.app.main.domain.user.account.enums.user.UserState;
import net.causw.app.main.shared.exception.errorcode.AuthErrorCode;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Community 도메인의 게시판·게시글·댓글 권한을 계산하는 단일 정책 클래스입니다.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CommunityPermissionPolicy {

	private static final EnumSet<Role> GLOBAL_DELETE_ROLES = EnumSet.of(
		Role.SYSTEM_ADMIN,
		Role.PRESIDENT,
		Role.VICE_PRESIDENT);

	public static void validateActiveUser(User user) {
		if (user == null || user.getState() != UserState.ACTIVE) {
			throw AuthErrorCode.INVALID_REGISTRATION_STATUS.toBaseException();
		}
		if (user.getRoles().contains(Role.NONE)) {
			throw AuthErrorCode.USER_ROLE_NONE.toBaseException();
		}
	}

	public static boolean isActiveUser(User user) {
		return user != null
			&& user.getState() == UserState.ACTIVE
			&& !user.getRoles().contains(Role.NONE);
	}

	public static boolean isSystemAdmin(User user) {
		return user != null && user.getRoles().contains(Role.SYSTEM_ADMIN);
	}

	public static boolean isBoardAdmin(User user, Collection<String> boardAdminIds) {
		return user != null
			&& boardAdminIds != null
			&& boardAdminIds.contains(user.getId())
			&& user.getRoles().contains(Role.ADMIN);
	}

	public static boolean isModerator(User user, Collection<String> boardAdminIds) {
		return isSystemAdmin(user) || isBoardAdmin(user, boardAdminIds);
	}

	public static boolean canReadBoard(
		User viewer,
		Board board,
		BoardConfig boardConfig,
		Collection<String> boardAdminIds) {

		if (!isActiveUser(viewer) || !isAlive(board) || boardConfig == null) {
			return false;
		}

		if (isModerator(viewer, boardAdminIds)) {
			return true;
		}

		if (!isDepartmentAllowed(boardConfig.getDepartments(), viewer.getDepartment())) {
			return false;
		}

		return boardConfig.getVisibility() == BoardVisibility.VISIBLE
			&& matchesReadScope(viewer.getAcademicStatus(), boardConfig.getReadScope());
	}

	/**
	 * 게시글·댓글 신규 작성 권한입니다.
	 * 읽기 권한과 쓰기 범위를 모두 요구하며 HIDDEN 게시판에는 누구도 작성할 수 없습니다.
	 */
	public static boolean canWriteBoard(
		User writer,
		Board board,
		BoardConfig boardConfig,
		Collection<String> boardAdminIds) {

		if (!canReadBoard(writer, board, boardConfig, boardAdminIds)
			|| boardConfig.getVisibility() == BoardVisibility.HIDDEN) {
			return false;
		}

		if (isModerator(writer, boardAdminIds)) {
			return true;
		}

		return boardConfig.getWriteScope() == BoardWriteScope.ALL_USER;
	}

	public static boolean canReadPost(
		User viewer,
		Post post,
		BoardConfig boardConfig,
		Collection<String> boardAdminIds) {

		return isAlive(post)
			&& canReadBoard(viewer, post.getBoard(), boardConfig, boardAdminIds);
	}

	public static boolean canUpdatePost(
		User updater,
		Post post,
		BoardConfig boardConfig,
		Collection<String> boardAdminIds) {

		return canReadPost(updater, post, boardConfig, boardAdminIds)
			&& canUpdateReadableContent(updater, post.getWriter() != null ? post.getWriter().getId() : null);
	}

	public static boolean canDeletePost(
		User deleter,
		Post post,
		BoardConfig boardConfig,
		Collection<String> boardAdminIds) {

		return isAlive(post)
			&& canDeletePostIgnoringTargetDeletion(deleter, post, boardConfig, boardAdminIds);
	}

	/**
	 * 이미 삭제된 게시글의 멱등 삭제 요청에서 사용하는 권한입니다.
	 * 게시글 자체의 삭제 상태만 무시하며, 상위 게시판의 생존·읽기 권한과 삭제 주체 권한은 그대로 검사합니다.
	 */
	public static boolean canDeletePostIgnoringTargetDeletion(
		User deleter,
		Post post,
		BoardConfig boardConfig,
		Collection<String> boardAdminIds) {

		return post != null
			&& canReadBoard(deleter, post.getBoard(), boardConfig, boardAdminIds)
			&& canDeleteReadableContent(
				deleter, post.getWriter() != null ? post.getWriter().getId() : null, boardAdminIds);
	}

	public static boolean canReadComment(
		User viewer,
		Comment comment,
		BoardConfig boardConfig,
		Collection<String> boardAdminIds) {

		return isAlive(comment)
			&& canReadPost(viewer, comment.getPost(), boardConfig, boardAdminIds);
	}

	public static boolean canUpdateComment(
		User updater,
		Comment comment,
		BoardConfig boardConfig,
		Collection<String> boardAdminIds) {

		return canReadComment(updater, comment, boardConfig, boardAdminIds)
			&& canUpdateReadableContent(
				updater, comment.getWriter() != null ? comment.getWriter().getId() : null);
	}

	public static boolean canDeleteComment(
		User deleter,
		Comment comment,
		BoardConfig boardConfig,
		Collection<String> boardAdminIds) {

		return canReadComment(deleter, comment, boardConfig, boardAdminIds)
			&& canDeleteReadableContent(
				deleter, comment.getWriter() != null ? comment.getWriter().getId() : null, boardAdminIds);
	}

	/** 읽기 및 대상 생존이 이미 보장된 응답 목록에서 수정 가능 여부를 계산합니다. */
	public static boolean canUpdateReadableContent(User updater, String writerId) {
		return isActiveUser(updater)
			&& writerId != null
			&& writerId.equals(updater.getId());
	}

	/** 읽기 및 대상 생존이 이미 보장된 응답 목록에서 삭제 가능 여부를 계산합니다. */
	public static boolean canDeleteReadableContent(
		User deleter,
		String writerId,
		Collection<String> boardAdminIds) {

		return isActiveUser(deleter)
			&& (canUpdateReadableContent(deleter, writerId)
				|| isBoardAdmin(deleter, boardAdminIds)
				|| deleter.getRoles().stream().anyMatch(GLOBAL_DELETE_ROLES::contains));
	}

	public static boolean isAlive(Board board) {
		return board != null && !Boolean.TRUE.equals(board.getIsDeleted());
	}

	public static boolean isAlive(Post post) {
		return post != null
			&& !Boolean.TRUE.equals(post.getIsDeleted())
			&& isAlive(post.getBoard());
	}

	public static boolean isAlive(Comment comment) {
		return comment != null
			&& !Boolean.TRUE.equals(comment.getIsDeleted())
			&& isAlive(comment.getPost());
	}

	private static boolean isDepartmentAllowed(Set<Department> allowed, Department department) {
		return allowed.isEmpty() || (department != null && allowed.contains(department));
	}

	private static boolean matchesReadScope(AcademicStatus academicStatus, BoardReadScope readScope) {
		if (readScope == BoardReadScope.BOTH) {
			return true;
		}
		if (readScope == BoardReadScope.ENROLLED) {
			return academicStatus == AcademicStatus.ENROLLED;
		}
		if (readScope == BoardReadScope.GRADUATED) {
			return academicStatus == AcademicStatus.GRADUATED;
		}
		return false;
	}
}
