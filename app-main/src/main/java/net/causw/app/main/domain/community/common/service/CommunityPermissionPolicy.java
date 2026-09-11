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

	/**
	 * 사용자가 활성 상태이며 권한이 부여되었는지 검증합니다.
	 *
	 * @param user 검증할 사용자
	 * @throws net.causw.app.main.shared.exception.BaseException 사용자가 없거나 비활성 상태 또는 역할이 없는 경우
	 */
	public static void validateActiveUser(User user) {
		if (user == null || user.getState() != UserState.ACTIVE) {
			throw AuthErrorCode.INVALID_REGISTRATION_STATUS.toBaseException();
		}
		if (user.getRoles().contains(Role.NONE)) {
			throw AuthErrorCode.USER_ROLE_NONE.toBaseException();
		}
	}

	/**
	 * 사용자가 활성 상태이며 역할이 부여되었는지 반환합니다.
	 *
	 * @param user 확인할 사용자
	 * @return 활성 사용자이면 {@code true}, 그렇지 않으면 {@code false}
	 */
	public static boolean isActiveUser(User user) {
		return user != null
			&& user.getState() == UserState.ACTIVE
			&& !user.getRoles().contains(Role.NONE);
	}

	/**
	 * 사용자가 시스템 관리자인지 반환합니다.
	 *
	 * @param user 확인할 사용자
	 * @return 시스템 관리자 역할을 보유하면 {@code true}
	 */
	public static boolean isSystemAdmin(User user) {
		return user != null && user.getRoles().contains(Role.SYSTEM_ADMIN);
	}

	/**
	 * 사용자가 해당 게시판의 관리자 권한을 보유하는지 반환합니다.
	 *
	 * @param user 확인할 사용자
	 * @param boardAdminIds 게시판 관리자 사용자 식별자 목록
	 * @return 목록에 포함되고 {@link Role#ADMIN} 역할을 보유하면 {@code true}
	 */
	public static boolean isBoardAdmin(User user, Collection<String> boardAdminIds) {
		return user != null
			&& boardAdminIds != null
			&& boardAdminIds.contains(user.getId())
			&& user.getRoles().contains(Role.ADMIN);
	}

	/**
	 * 사용자가 시스템 관리자 또는 해당 게시판 관리자인지 반환합니다.
	 *
	 * @param user 확인할 사용자
	 * @param boardAdminIds 게시판 관리자 사용자 식별자 목록
	 * @return 게시판 운영 권한이 있으면 {@code true}
	 */
	public static boolean isModerator(User user, Collection<String> boardAdminIds) {
		return isSystemAdmin(user) || isBoardAdmin(user, boardAdminIds);
	}

	/**
	 * 사용자가 게시판을 읽을 수 있는지 계산합니다.
	 * 모든 사용자는 활성 상태, 게시판 생존 상태, 학과 제한, 공개 상태 및 학적 읽기 범위를 만족해야 합니다.
	 *
	 * @param viewer 조회할 사용자
	 * @param board 대상 게시판
	 * @param boardConfig 게시판 접근 설정
	 * @param boardAdminIds 게시판 관리자 사용자 식별자 목록
	 * @return 읽기가 허용되면 {@code true}
	 */
	public static boolean canReadBoard(
		User viewer,
		Board board,
		BoardConfig boardConfig,
		Collection<String> boardAdminIds) {

		if (!isActiveUser(viewer) || !isAlive(board) || boardConfig == null) {
			return false;
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

	/**
	 * 사용자가 게시글을 읽을 수 있는지 계산합니다.
	 *
	 * @param viewer 조회할 사용자
	 * @param post 대상 게시글
	 * @param boardConfig 게시판 접근 설정
	 * @param boardAdminIds 게시판 관리자 사용자 식별자 목록
	 * @return 게시글이 삭제되지 않았고 게시판 읽기가 허용되면 {@code true}
	 */
	public static boolean canReadPost(
		User viewer,
		Post post,
		BoardConfig boardConfig,
		Collection<String> boardAdminIds) {

		return isAlive(post)
			&& canReadBoard(viewer, post.getBoard(), boardConfig, boardAdminIds);
	}

	/**
	 * 사용자가 게시글을 수정할 수 있는지 계산합니다.
	 *
	 * @param updater 수정할 사용자
	 * @param post 대상 게시글
	 * @param boardConfig 게시판 접근 설정
	 * @param boardAdminIds 게시판 관리자 사용자 식별자 목록
	 * @return 게시글을 읽을 수 있으며 작성자 본인이면 {@code true}
	 */
	public static boolean canUpdatePost(
		User updater,
		Post post,
		BoardConfig boardConfig,
		Collection<String> boardAdminIds) {

		return canReadPost(updater, post, boardConfig, boardAdminIds)
			&& canUpdateReadableContent(updater, post.getWriter() != null ? post.getWriter().getId() : null);
	}

	/**
	 * 사용자가 삭제되지 않은 게시글을 삭제할 수 있는지 계산합니다.
	 *
	 * @param deleter 삭제할 사용자
	 * @param post 대상 게시글
	 * @param boardConfig 게시판 접근 설정
	 * @param boardAdminIds 게시판 관리자 사용자 식별자 목록
	 * @return 삭제가 허용되면 {@code true}
	 */
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

	/**
	 * 사용자가 댓글을 읽을 수 있는지 계산합니다.
	 *
	 * @param viewer 조회할 사용자
	 * @param comment 대상 댓글
	 * @param boardConfig 게시판 접근 설정
	 * @param boardAdminIds 게시판 관리자 사용자 식별자 목록
	 * @return 댓글과 상위 게시글이 삭제되지 않았고 게시판 읽기가 허용되면 {@code true}
	 */
	public static boolean canReadComment(
		User viewer,
		Comment comment,
		BoardConfig boardConfig,
		Collection<String> boardAdminIds) {

		return isAlive(comment)
			&& canReadPost(viewer, comment.getPost(), boardConfig, boardAdminIds);
	}

	/**
	 * 사용자가 댓글을 수정할 수 있는지 계산합니다.
	 *
	 * @param updater 수정할 사용자
	 * @param comment 대상 댓글
	 * @param boardConfig 게시판 접근 설정
	 * @param boardAdminIds 게시판 관리자 사용자 식별자 목록
	 * @return 댓글을 읽을 수 있으며 작성자 본인이면 {@code true}
	 */
	public static boolean canUpdateComment(
		User updater,
		Comment comment,
		BoardConfig boardConfig,
		Collection<String> boardAdminIds) {

		return canReadComment(updater, comment, boardConfig, boardAdminIds)
			&& canUpdateReadableContent(
				updater, comment.getWriter() != null ? comment.getWriter().getId() : null);
	}

	/**
	 * 사용자가 댓글을 삭제할 수 있는지 계산합니다.
	 *
	 * @param deleter 삭제할 사용자
	 * @param comment 대상 댓글
	 * @param boardConfig 게시판 접근 설정
	 * @param boardAdminIds 게시판 관리자 사용자 식별자 목록
	 * @return 댓글 삭제가 허용되면 {@code true}
	 */
	public static boolean canDeleteComment(
		User deleter,
		Comment comment,
		BoardConfig boardConfig,
		Collection<String> boardAdminIds) {

		return canReadComment(deleter, comment, boardConfig, boardAdminIds)
			&& canDeleteReadableContent(
				deleter, comment.getWriter() != null ? comment.getWriter().getId() : null, boardAdminIds);
	}

	/**
	 * 읽기 및 대상 생존이 이미 보장된 콘텐츠의 수정 가능 여부를 계산합니다.
	 *
	 * @param updater 수정할 사용자
	 * @param writerId 콘텐츠 작성자 식별자
	 * @return 활성 사용자가 작성자 본인이면 {@code true}
	 */
	public static boolean canUpdateReadableContent(User updater, String writerId) {
		return isActiveUser(updater)
			&& writerId != null
			&& writerId.equals(updater.getId());
	}

	/**
	 * 읽기 및 대상 생존이 이미 보장된 콘텐츠의 삭제 가능 여부를 계산합니다.
	 *
	 * @param deleter 삭제할 사용자
	 * @param writerId 콘텐츠 작성자 식별자
	 * @param boardAdminIds 게시판 관리자 사용자 식별자 목록
	 * @return 작성자 본인, 게시판 관리자 또는 전역 삭제 권한자이면 {@code true}
	 */
	public static boolean canDeleteReadableContent(
		User deleter,
		String writerId,
		Collection<String> boardAdminIds) {

		return isActiveUser(deleter)
			&& (canUpdateReadableContent(deleter, writerId)
				|| isBoardAdmin(deleter, boardAdminIds)
				|| deleter.getRoles().stream().anyMatch(GLOBAL_DELETE_ROLES::contains));
	}

	/**
	 * 게시판이 존재하고 삭제되지 않았는지 반환합니다.
	 *
	 * @param board 확인할 게시판
	 * @return 생존한 게시판이면 {@code true}
	 */
	public static boolean isAlive(Board board) {
		return board != null && !Boolean.TRUE.equals(board.getIsDeleted());
	}

	/**
	 * 게시글과 상위 게시판이 존재하고 삭제되지 않았는지 반환합니다.
	 *
	 * @param post 확인할 게시글
	 * @return 게시글과 상위 게시판이 모두 생존하면 {@code true}
	 */
	public static boolean isAlive(Post post) {
		return post != null
			&& !Boolean.TRUE.equals(post.getIsDeleted())
			&& isAlive(post.getBoard());
	}

	/**
	 * 댓글, 상위 게시글 및 게시판이 존재하고 삭제되지 않았는지 반환합니다.
	 *
	 * @param comment 확인할 댓글
	 * @return 댓글과 모든 상위 리소스가 생존하면 {@code true}
	 */
	public static boolean isAlive(Comment comment) {
		return comment != null
			&& !Boolean.TRUE.equals(comment.getIsDeleted())
			&& isAlive(comment.getPost());
	}

	/**
	 * department가 null인 유저는 학과 미확정 레거시 유저로 간주하여 제한 없이 허용한다.
	 * 이 정책은 UserBoardSubscribeQueryRepository.departmentCondition()과 동일하게 유지되어야 한다.
	 * TODO: department 백필 완료 후 null 허용 조건 제거
	 */
	private static boolean isDepartmentAllowed(Set<Department> allowed, Department department) {
		return allowed.isEmpty() || department == null || allowed.contains(department);
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
