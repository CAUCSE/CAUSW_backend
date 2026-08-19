package net.causw.app.main.domain.community.common.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import net.causw.app.main.domain.community.board.entity.Board;
import net.causw.app.main.domain.community.board.entity.BoardConfig;
import net.causw.app.main.domain.community.board.entity.BoardReadScope;
import net.causw.app.main.domain.community.board.entity.BoardVisibility;
import net.causw.app.main.domain.community.board.entity.BoardWriteScope;
import net.causw.app.main.domain.community.comment.entity.Comment;
import net.causw.app.main.domain.community.post.entity.Post;
import net.causw.app.main.domain.user.academic.enums.userAcademicRecord.AcademicStatus;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.enums.user.Role;
import net.causw.app.main.domain.user.account.enums.user.UserState;
import net.causw.app.main.shared.exception.BaseRunTimeV2Exception;
import net.causw.app.main.shared.exception.errorcode.AuthErrorCode;
import net.causw.app.main.util.ObjectFixtures;

@DisplayName("CommunityPermissionPolicy")
class CommunityPermissionPolicyTest {

	private static final String BOARD_ID = "board-id";
	private static final String OWNER_ID = "owner-id";
	private static final String BOARD_ADMIN_ID = "board-admin-id";
	private static final List<String> BOARD_ADMIN_IDS = List.of(BOARD_ADMIN_ID);

	private Board board;
	private BoardConfig visibleAllUserBoardConfig;
	private User owner;
	private Post post;
	private Comment rootComment;
	private Comment childComment;

	@BeforeEach
	void setUp() {
		board = aliveBoard();
		visibleAllUserBoardConfig = boardConfig(
			BoardReadScope.BOTH,
			BoardWriteScope.ALL_USER,
			BoardVisibility.VISIBLE);
		owner = activeUser(OWNER_ID, AcademicStatus.ENROLLED, Role.COMMON);
		post = ObjectFixtures.getPost(owner, board);
		rootComment = Comment.ofRoot("root comment", false, null, owner, post);
		childComment = Comment.ofChildComment("child comment", false, null, owner, rootComment);
	}

	@Test
	@DisplayName("ACTIVE이며 NONE 역할이 없는 사용자만 Community 유효 사용자이다")
	void activeUserWithoutNoneRoleIsValid() {
		User activeUser = activeUser("active-id", AcademicStatus.ENROLLED, Role.COMMON);

		assertThat(CommunityPermissionPolicy.isActiveUser(activeUser)).isTrue();
		assertThatCode(() -> CommunityPermissionPolicy.validateActiveUser(activeUser)).doesNotThrowAnyException();
	}

	@ParameterizedTest(name = "{0} 상태 사용자는 유효하지 않다")
	@EnumSource(value = UserState.class, names = "ACTIVE", mode = EnumSource.Mode.EXCLUDE)
	void nonActiveUserIsInvalid(UserState state) {
		User user = activeUser("invalid-state-id", AcademicStatus.ENROLLED, Role.COMMON);
		user.setState(state);

		assertThat(CommunityPermissionPolicy.isActiveUser(user)).isFalse();
		assertThatThrownBy(() -> CommunityPermissionPolicy.validateActiveUser(user))
			.isInstanceOf(BaseRunTimeV2Exception.class)
			.hasFieldOrPropertyWithValue("errorCode", AuthErrorCode.INVALID_REGISTRATION_STATUS);
	}

	@Test
	@DisplayName("ACTIVE여도 NONE 역할 사용자는 유효하지 않다")
	void activeUserWithNoneRoleIsInvalid() {
		User user = activeUser("none-role-id", AcademicStatus.ENROLLED, Role.NONE);

		assertThat(CommunityPermissionPolicy.isActiveUser(user)).isFalse();
		assertThatThrownBy(() -> CommunityPermissionPolicy.validateActiveUser(user))
			.isInstanceOf(BaseRunTimeV2Exception.class)
			.hasFieldOrPropertyWithValue("errorCode", AuthErrorCode.USER_ROLE_NONE);
	}

	@Test
	@DisplayName("null 사용자는 유효하지 않다")
	void nullUserIsInvalid() {
		assertThat(CommunityPermissionPolicy.isActiveUser(null)).isFalse();
		assertThatThrownBy(() -> CommunityPermissionPolicy.validateActiveUser(null))
			.isInstanceOf(BaseRunTimeV2Exception.class)
			.hasFieldOrPropertyWithValue("errorCode", AuthErrorCode.INVALID_REGISTRATION_STATUS);
	}

	@Test
	@DisplayName("시스템 관리자는 Role.ADMIN만을 의미한다")
	void onlyAdminRoleIsSystemAdmin() {
		User admin = activeUser("admin-id", AcademicStatus.ENROLLED, Role.ADMIN);
		User president = activeUser("president-id", AcademicStatus.ENROLLED, Role.PRESIDENT);
		User vicePresident = activeUser("vice-president-id", AcademicStatus.ENROLLED, Role.VICE_PRESIDENT);
		User boardAdmin = activeUser(BOARD_ADMIN_ID, AcademicStatus.ENROLLED, Role.COMMON);

		assertThat(CommunityPermissionPolicy.isSystemAdmin(admin)).isTrue();
		assertThat(CommunityPermissionPolicy.isSystemAdmin(president)).isFalse();
		assertThat(CommunityPermissionPolicy.isSystemAdmin(vicePresident)).isFalse();
		assertThat(CommunityPermissionPolicy.isSystemAdmin(boardAdmin)).isFalse();
	}

	@Test
	@DisplayName("게시판 관리자는 역할이 아니라 BoardAdmin ID 목록으로 판단한다")
	void boardAdminIsDeterminedByBoardAdminIds() {
		User listedCommonUser = activeUser(BOARD_ADMIN_ID, AcademicStatus.ENROLLED, Role.COMMON);
		User unlistedPrivilegedUser = activeUser("council-id", AcademicStatus.ENROLLED, Role.COUNCIL);

		assertThat(CommunityPermissionPolicy.isBoardAdmin(listedCommonUser, BOARD_ADMIN_IDS)).isTrue();
		assertThat(CommunityPermissionPolicy.isBoardAdmin(unlistedPrivilegedUser, BOARD_ADMIN_IDS)).isFalse();
	}

	@ParameterizedTest(name = "actor={0}, academic={1}, scope={2}, visibility={3} -> readable={4}")
	@CsvSource({
		"COMMON, ENROLLED, ENROLLED, VISIBLE, true",
		"COMMON, GRADUATED, ENROLLED, VISIBLE, false",
		"COMMON, ENROLLED, BOTH, HIDDEN, false",
		"BOARD_ADMIN, GRADUATED, ENROLLED, HIDDEN, true",
		"SYSTEM_ADMIN, GRADUATED, ENROLLED, HIDDEN, true",
		"PRESIDENT, GRADUATED, ENROLLED, HIDDEN, false",
		"VICE_PRESIDENT, GRADUATED, ENROLLED, HIDDEN, false"
	})
	void canReadBoardFollowsScopeVisibilityAndModeratorPolicy(
		Actor actor,
		AcademicStatus academicStatus,
		BoardReadScope readScope,
		BoardVisibility visibility,
		boolean expected) {

		User viewer = actorUser(actor, academicStatus);
		BoardConfig boardConfig = boardConfig(readScope, BoardWriteScope.ALL_USER, visibility);

		assertThat(CommunityPermissionPolicy.canReadBoard(viewer, board, boardConfig, BOARD_ADMIN_IDS))
			.isEqualTo(expected);
	}

	@ParameterizedTest(name = "{0}도 삭제된 게시판은 읽을 수 없다")
	@EnumSource(value = Actor.class, names = {"BOARD_ADMIN", "SYSTEM_ADMIN"})
	void moderatorCannotReadDeletedBoard(Actor actor) {
		board.setIsDeleted(true);

		assertThat(CommunityPermissionPolicy.canReadBoard(
			actorUser(actor, AcademicStatus.ENROLLED),
			board,
			visibleAllUserBoardConfig,
			BOARD_ADMIN_IDS)).isFalse();
	}

	@ParameterizedTest(name = "actor={0}, academic={1}, scope={2}, writeScope={3}, visibility={4} -> writable={5}")
	@CsvSource({
		"COMMON, ENROLLED, BOTH, ALL_USER, VISIBLE, true",
		"COMMON, ENROLLED, BOTH, ONLY_ADMIN, VISIBLE, false",
		"COMMON, ENROLLED, GRADUATED, ALL_USER, VISIBLE, false",
		"BOARD_ADMIN, ENROLLED, GRADUATED, ONLY_ADMIN, VISIBLE, true",
		"SYSTEM_ADMIN, ENROLLED, GRADUATED, ONLY_ADMIN, VISIBLE, true",
		"PRESIDENT, ENROLLED, BOTH, ONLY_ADMIN, VISIBLE, false",
		"VICE_PRESIDENT, ENROLLED, BOTH, ONLY_ADMIN, VISIBLE, false",
		"COMMON, ENROLLED, BOTH, ALL_USER, HIDDEN, false",
		"BOARD_ADMIN, ENROLLED, BOTH, ONLY_ADMIN, HIDDEN, false",
		"SYSTEM_ADMIN, ENROLLED, BOTH, ONLY_ADMIN, HIDDEN, false"
	})
	void canWriteBoardRequiresReadAndWriteScopeAndRejectsHidden(
		Actor actor,
		AcademicStatus academicStatus,
		BoardReadScope readScope,
		BoardWriteScope writeScope,
		BoardVisibility visibility,
		boolean expected) {

		User writer = actorUser(actor, academicStatus);
		BoardConfig boardConfig = boardConfig(readScope, writeScope, visibility);

		assertThat(CommunityPermissionPolicy.canWriteBoard(writer, board, boardConfig, BOARD_ADMIN_IDS))
			.isEqualTo(expected);
	}

	@ParameterizedTest(name = "{0}: update={1}, delete={2}")
	@CsvSource({
		"OWNER, true, true",
		"BOARD_ADMIN, false, true",
		"SYSTEM_ADMIN, false, true",
		"PRESIDENT, false, true",
		"VICE_PRESIDENT, false, true",
		"COMMON, false, false"
	})
	void postUpdateAndDeleteFollowActorMatrix(Actor actor, boolean updatable, boolean deletable) {
		User actorUser = actorUser(actor, AcademicStatus.ENROLLED);

		assertThat(CommunityPermissionPolicy.canUpdatePost(
			actorUser, post, visibleAllUserBoardConfig, BOARD_ADMIN_IDS)).isEqualTo(updatable);
		assertThat(CommunityPermissionPolicy.canDeletePost(
			actorUser, post, visibleAllUserBoardConfig, BOARD_ADMIN_IDS)).isEqualTo(deletable);
		assertThat(CommunityPermissionPolicy.canUpdateReadableContent(actorUser, OWNER_ID)).isEqualTo(updatable);
		assertThat(CommunityPermissionPolicy.canDeleteReadableContent(
			actorUser, OWNER_ID, BOARD_ADMIN_IDS)).isEqualTo(deletable);
	}

	@ParameterizedTest(name = "{0}: update={1}, delete={2}")
	@CsvSource({
		"OWNER, true, true",
		"BOARD_ADMIN, false, true",
		"SYSTEM_ADMIN, false, true",
		"PRESIDENT, false, true",
		"VICE_PRESIDENT, false, true",
		"COMMON, false, false"
	})
	void rootAndChildCommentPermissionsFollowSameActorMatrix(
		Actor actor,
		boolean updatable,
		boolean deletable) {

		User actorUser = actorUser(actor, AcademicStatus.ENROLLED);

		assertThat(CommunityPermissionPolicy.canUpdateComment(
			actorUser, rootComment, visibleAllUserBoardConfig, BOARD_ADMIN_IDS)).isEqualTo(updatable);
		assertThat(CommunityPermissionPolicy.canDeleteComment(
			actorUser, rootComment, visibleAllUserBoardConfig, BOARD_ADMIN_IDS)).isEqualTo(deletable);
		assertThat(CommunityPermissionPolicy.canUpdateComment(
			actorUser, childComment, visibleAllUserBoardConfig, BOARD_ADMIN_IDS)).isEqualTo(updatable);
		assertThat(CommunityPermissionPolicy.canDeleteComment(
			actorUser, childComment, visibleAllUserBoardConfig, BOARD_ADMIN_IDS)).isEqualTo(deletable);
	}

	@Test
	@DisplayName("회장·부회장은 본인이 읽을 수 없는 게시글과 댓글을 삭제할 수 없다")
	void executivesCannotDeleteUnreadableContent() {
		BoardConfig hiddenConfig = boardConfig(
			BoardReadScope.BOTH,
			BoardWriteScope.ALL_USER,
			BoardVisibility.HIDDEN);

		for (Actor actor : List.of(Actor.PRESIDENT, Actor.VICE_PRESIDENT)) {
			User executive = actorUser(actor, AcademicStatus.ENROLLED);
			assertThat(CommunityPermissionPolicy.canDeletePost(
				executive, post, hiddenConfig, BOARD_ADMIN_IDS)).isFalse();
			assertThat(CommunityPermissionPolicy.canDeleteComment(
				executive, rootComment, hiddenConfig, BOARD_ADMIN_IDS)).isFalse();
		}
	}

	@Test
	@DisplayName("시스템 관리자와 해당 게시판 관리자는 HIDDEN 콘텐츠를 읽고 삭제할 수 있지만 수정할 수 없다")
	void moderatorsCanReadAndDeleteButCannotUpdateHiddenContent() {
		BoardConfig hiddenConfig = boardConfig(
			BoardReadScope.GRADUATED,
			BoardWriteScope.ONLY_ADMIN,
			BoardVisibility.HIDDEN);

		for (Actor actor : List.of(Actor.SYSTEM_ADMIN, Actor.BOARD_ADMIN)) {
			User moderator = actorUser(actor, AcademicStatus.ENROLLED);
			assertThat(CommunityPermissionPolicy.canReadPost(
				moderator, post, hiddenConfig, BOARD_ADMIN_IDS)).isTrue();
			assertThat(CommunityPermissionPolicy.canUpdatePost(
				moderator, post, hiddenConfig, BOARD_ADMIN_IDS)).isFalse();
			assertThat(CommunityPermissionPolicy.canDeletePost(
				moderator, post, hiddenConfig, BOARD_ADMIN_IDS)).isTrue();
			assertThat(CommunityPermissionPolicy.canReadComment(
				moderator, rootComment, hiddenConfig, BOARD_ADMIN_IDS)).isTrue();
			assertThat(CommunityPermissionPolicy.canUpdateComment(
				moderator, rootComment, hiddenConfig, BOARD_ADMIN_IDS)).isFalse();
			assertThat(CommunityPermissionPolicy.canDeleteComment(
				moderator, rootComment, hiddenConfig, BOARD_ADMIN_IDS)).isTrue();
		}
	}

	@Test
	@DisplayName("삭제된 게시글은 누구도 읽거나 수정하거나 삭제할 수 없다")
	void deletedPostHasNoPermissions() {
		post.setIsDeleted(true);

		for (Actor actor : Actor.values()) {
			User user = actorUser(actor, AcademicStatus.ENROLLED);
			assertThat(CommunityPermissionPolicy.canReadPost(
				user, post, visibleAllUserBoardConfig, BOARD_ADMIN_IDS)).isFalse();
			assertThat(CommunityPermissionPolicy.canUpdatePost(
				user, post, visibleAllUserBoardConfig, BOARD_ADMIN_IDS)).isFalse();
			assertThat(CommunityPermissionPolicy.canDeletePost(
				user, post, visibleAllUserBoardConfig, BOARD_ADMIN_IDS)).isFalse();
		}
	}

	@ParameterizedTest(name = "{0}: deleted post delete authority={1}")
	@CsvSource({
		"OWNER, true",
		"BOARD_ADMIN, true",
		"SYSTEM_ADMIN, true",
		"PRESIDENT, true",
		"VICE_PRESIDENT, true",
		"COMMON, false"
	})
	@DisplayName("삭제된 게시글의 멱등 삭제 권한은 기존 삭제 주체 매트릭스를 유지한다")
	void deletedPostIdempotentDeleteAuthorityFollowsActorMatrix(Actor actor, boolean deletable) {
		post.setIsDeleted(true);
		User actorUser = actorUser(actor, AcademicStatus.ENROLLED);

		assertThat(CommunityPermissionPolicy.canDeletePostIgnoringTargetDeletion(
			actorUser, post, visibleAllUserBoardConfig, BOARD_ADMIN_IDS)).isEqualTo(deletable);
	}

	@Test
	@DisplayName("게시판이 삭제되면 게시글 멱등 삭제 권한도 사라진다")
	void deletedBoardRemovesIdempotentPostDeleteAuthority() {
		post.setIsDeleted(true);
		board.setIsDeleted(true);

		for (Actor actor : Actor.values()) {
			User user = actorUser(actor, AcademicStatus.ENROLLED);
			assertThat(CommunityPermissionPolicy.canDeletePostIgnoringTargetDeletion(
				user, post, visibleAllUserBoardConfig, BOARD_ADMIN_IDS)).isFalse();
		}
	}

	@Test
	@DisplayName("삭제된 댓글은 누구도 읽거나 수정하거나 삭제할 수 없다")
	void deletedCommentHasNoPermissions() {
		rootComment.delete();

		for (Actor actor : Actor.values()) {
			User user = actorUser(actor, AcademicStatus.ENROLLED);
			assertThat(CommunityPermissionPolicy.canReadComment(
				user, rootComment, visibleAllUserBoardConfig, BOARD_ADMIN_IDS)).isFalse();
			assertThat(CommunityPermissionPolicy.canUpdateComment(
				user, rootComment, visibleAllUserBoardConfig, BOARD_ADMIN_IDS)).isFalse();
			assertThat(CommunityPermissionPolicy.canDeleteComment(
				user, rootComment, visibleAllUserBoardConfig, BOARD_ADMIN_IDS)).isFalse();
		}
	}

	@Test
	@DisplayName("부모 댓글이 삭제되어도 미삭제 답글의 권한은 유지된다")
	void deletedRootCommentDoesNotRemoveChildCommentPermissions() {
		rootComment.delete();

		assertThat(CommunityPermissionPolicy.canReadComment(
			owner, childComment, visibleAllUserBoardConfig, BOARD_ADMIN_IDS)).isTrue();
		assertThat(CommunityPermissionPolicy.canUpdateComment(
			owner, childComment, visibleAllUserBoardConfig, BOARD_ADMIN_IDS)).isTrue();
		assertThat(CommunityPermissionPolicy.canDeleteComment(
			owner, childComment, visibleAllUserBoardConfig, BOARD_ADMIN_IDS)).isTrue();
	}

	@Test
	@DisplayName("게시판이 삭제되면 하위 게시글과 댓글의 모든 권한도 사라진다")
	void deletedBoardRemovesAllContentPermissions() {
		board.setIsDeleted(true);

		for (Actor actor : Actor.values()) {
			User user = actorUser(actor, AcademicStatus.ENROLLED);
			assertThat(CommunityPermissionPolicy.canReadPost(
				user, post, visibleAllUserBoardConfig, BOARD_ADMIN_IDS)).isFalse();
			assertThat(CommunityPermissionPolicy.canUpdatePost(
				user, post, visibleAllUserBoardConfig, BOARD_ADMIN_IDS)).isFalse();
			assertThat(CommunityPermissionPolicy.canDeletePost(
				user, post, visibleAllUserBoardConfig, BOARD_ADMIN_IDS)).isFalse();
			assertThat(CommunityPermissionPolicy.canReadComment(
				user, rootComment, visibleAllUserBoardConfig, BOARD_ADMIN_IDS)).isFalse();
			assertThat(CommunityPermissionPolicy.canUpdateComment(
				user, rootComment, visibleAllUserBoardConfig, BOARD_ADMIN_IDS)).isFalse();
			assertThat(CommunityPermissionPolicy.canDeleteComment(
				user, rootComment, visibleAllUserBoardConfig, BOARD_ADMIN_IDS)).isFalse();
		}
	}

	@Test
	@DisplayName("비활성 사용자는 작성자나 관리자여도 콘텐츠 권한이 없다")
	void inactiveUserHasNoContentPermissions() {
		User inactiveOwner = actorUser(Actor.OWNER, AcademicStatus.ENROLLED);
		inactiveOwner.setState(UserState.INACTIVE);
		User inactiveAdmin = actorUser(Actor.SYSTEM_ADMIN, AcademicStatus.ENROLLED);
		inactiveAdmin.setState(UserState.INACTIVE);

		for (User user : List.of(inactiveOwner, inactiveAdmin)) {
			assertThat(CommunityPermissionPolicy.canReadPost(
				user, post, visibleAllUserBoardConfig, BOARD_ADMIN_IDS)).isFalse();
			assertThat(CommunityPermissionPolicy.canUpdatePost(
				user, post, visibleAllUserBoardConfig, BOARD_ADMIN_IDS)).isFalse();
			assertThat(CommunityPermissionPolicy.canDeletePost(
				user, post, visibleAllUserBoardConfig, BOARD_ADMIN_IDS)).isFalse();
			assertThat(CommunityPermissionPolicy.canUpdateReadableContent(user, OWNER_ID)).isFalse();
			assertThat(CommunityPermissionPolicy.canDeleteReadableContent(
				user, OWNER_ID, BOARD_ADMIN_IDS)).isFalse();
		}
	}

	@Test
	@DisplayName("답글 생존 판단은 게시판·게시글 상태만 상속하고 부모 댓글 삭제 상태는 상속하지 않는다")
	void aliveChecksIncludeAncestorDeletion() {
		assertThat(CommunityPermissionPolicy.isAlive(board)).isTrue();
		assertThat(CommunityPermissionPolicy.isAlive(post)).isTrue();
		assertThat(CommunityPermissionPolicy.isAlive(rootComment)).isTrue();
		assertThat(CommunityPermissionPolicy.isAlive(childComment)).isTrue();

		rootComment.delete();

		assertThat(CommunityPermissionPolicy.isAlive(rootComment)).isFalse();
		assertThat(CommunityPermissionPolicy.isAlive(childComment)).isTrue();
	}

	private static Board aliveBoard() {
		return ObjectFixtures.getBoardV2WithId(BOARD_ID);
	}

	private static BoardConfig boardConfig(
		BoardReadScope readScope,
		BoardWriteScope writeScope,
		BoardVisibility visibility) {

		return BoardConfig.of(
			BOARD_ID,
			false,
			readScope,
			writeScope,
			false,
			visibility,
			10,
			null,
			null);
	}

	private User actorUser(Actor actor, AcademicStatus academicStatus) {
		return switch (actor) {
			case OWNER -> ownerWithAcademicStatus(academicStatus);
			case BOARD_ADMIN -> activeUser(BOARD_ADMIN_ID, academicStatus, Role.COMMON);
			case SYSTEM_ADMIN -> activeUser("system-admin-id", academicStatus, Role.ADMIN);
			case PRESIDENT -> activeUser("president-id", academicStatus, Role.PRESIDENT);
			case VICE_PRESIDENT -> activeUser("vice-president-id", academicStatus, Role.VICE_PRESIDENT);
			case COMMON -> activeUser("common-id", academicStatus, Role.COMMON);
		};
	}

	private User ownerWithAcademicStatus(AcademicStatus academicStatus) {
		owner.setAcademicStatus(academicStatus);
		return owner;
	}

	private static User activeUser(String id, AcademicStatus academicStatus, Role... roles) {
		User user = ObjectFixtures.getCertifiedUserWithId(id);
		user.setAcademicStatus(academicStatus);
		user.setRoles(Set.of(roles));
		return user;
	}

	private enum Actor {
		OWNER,
		BOARD_ADMIN,
		SYSTEM_ADMIN,
		PRESIDENT,
		VICE_PRESIDENT,
		COMMON
	}
}
