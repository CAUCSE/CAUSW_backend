package net.causw.app.main.domain.community.systemnotice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.never;
import static org.mockito.BDDMockito.verify;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.causw.app.main.domain.community.board.entity.Board;
import net.causw.app.main.domain.community.board.entity.BoardConfig;
import net.causw.app.main.domain.community.board.entity.BoardReadScope;
import net.causw.app.main.domain.community.board.entity.BoardVisibility;
import net.causw.app.main.domain.community.board.service.implementation.BoardConfigReader;
import net.causw.app.main.domain.community.post.entity.Post;
import net.causw.app.main.domain.community.post.service.PostService;
import net.causw.app.main.domain.community.post.service.dto.PostCreateCommand;
import net.causw.app.main.domain.community.post.service.dto.PostCreateResult;
import net.causw.app.main.domain.community.post.service.dto.PostUpdateCommand;
import net.causw.app.main.domain.community.systemnotice.service.dto.SystemNoticeCreateCommand;
import net.causw.app.main.domain.community.systemnotice.service.dto.SystemNoticeCreateResult;
import net.causw.app.main.domain.community.systemnotice.service.dto.SystemNoticeResult;
import net.causw.app.main.domain.community.systemnotice.service.dto.SystemNoticeUpdateCommand;
import net.causw.app.main.domain.community.systemnotice.service.implementation.SystemNoticeReader;
import net.causw.app.main.domain.community.systemnotice.service.implementation.SystemNoticeWriter;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.enums.user.Role;
import net.causw.app.main.domain.user.account.enums.user.UserState;
import net.causw.app.main.shared.exception.BaseRunTimeV2Exception;
import net.causw.app.main.shared.exception.errorcode.AuthErrorCode;
import net.causw.app.main.shared.exception.errorcode.BoardErrorCode;
import net.causw.app.main.shared.exception.errorcode.PostErrorCode;

@ExtendWith(MockitoExtension.class)
class SystemNoticeServiceTest {

	@InjectMocks
	private SystemNoticeService systemNoticeService;

	@Mock
	private SystemNoticeReader systemNoticeReader;

	@Mock
	private SystemNoticeWriter systemNoticeWriter;

	@Mock
	private PostService postService;

	@Mock
	private BoardConfigReader boardConfigReader;

	@Mock
	private User viewer;

	@Mock
	private Post latestPost;

	@Mock
	private Board systemNoticeBoard;

	@Mock
	private BoardConfig systemNoticeBoardConfig;

	@Test
	void createPassesContentToDedicatedPostCommandAndReturnsIt() {
		User writer = org.mockito.Mockito.mock(User.class);
		LocalDateTime createdAt = LocalDateTime.of(2026, 8, 9, 12, 0);
		PostCreateResult postResult = PostCreateResult.builder()
			.id("post-id")
			.content("content")
			.createdAt(createdAt)
			.build();
		given(systemNoticeReader.getSystemNoticeBoardId()).willReturn("system-notice-board-id");
		given(writer.getNickname()).willReturn("admin");
		given(postService.createSystemNotice(org.mockito.ArgumentMatchers.any(PostCreateCommand.class)))
			.willReturn(postResult);

		SystemNoticeCreateResult result = systemNoticeService.create(
			new SystemNoticeCreateCommand("테스트 제목", "content", writer));

		ArgumentCaptor<PostCreateCommand> captor = ArgumentCaptor.forClass(PostCreateCommand.class);
		verify(postService).createSystemNotice(captor.capture());
		assertThat(captor.getValue().content()).isEqualTo("content");
		assertThat(result.content()).isEqualTo("content");
	}

	@Test
	void updatePassesContentToDedicatedPostCommand() {
		User updater = org.mockito.Mockito.mock(User.class);
		given(systemNoticeReader.getSystemNoticePost("post-id")).willReturn(latestPost);

		systemNoticeService.update("post-id", new SystemNoticeUpdateCommand("테스트 제목", "updated content", updater));

		ArgumentCaptor<PostUpdateCommand> captor = ArgumentCaptor.forClass(PostUpdateCommand.class);
		verify(postService).updateSystemNotice(captor.capture());
		assertThat(captor.getValue().content()).isEqualTo("updated content");
	}

	@Test
	void deleteUsesDedicatedSystemNoticePostFlow() {
		User updater = org.mockito.Mockito.mock(User.class);
		given(systemNoticeReader.getSystemNoticePost("post-id")).willReturn(latestPost);

		systemNoticeService.delete("post-id", updater);

		verify(postService).deleteSystemNotice(updater, "post-id");
	}

	@Test
	void getLatestReturnsPostContent() {
		User writer = org.mockito.Mockito.mock(User.class);
		givenActiveViewer();
		given(viewer.getId()).willReturn("user-id");
		given(systemNoticeReader.findLatestPost()).willReturn(Optional.of(latestPost));
		givenReadableSystemNotice(latestPost);
		given(systemNoticeReader.findReadByUserId("user-id")).willReturn(Optional.empty());
		given(latestPost.getId()).willReturn("post-id");
		given(latestPost.getContent()).willReturn("content");
		given(latestPost.getWriter()).willReturn(writer);
		given(writer.getNickname()).willReturn("admin");

		SystemNoticeResult result = systemNoticeService.getLatest(viewer).orElseThrow();

		assertThat(result.content()).isEqualTo("content");
	}

	@Test
	void markAsReadUpdatesPointerWhenRequestedPostIsLatest() {
		givenActiveViewer();
		given(viewer.getId()).willReturn("user-id");
		given(latestPost.getId()).willReturn("latest-post-id");
		given(systemNoticeReader.findLatestPost()).willReturn(Optional.of(latestPost));
		givenReadableSystemNotice(latestPost);

		systemNoticeService.markAsRead(viewer, "latest-post-id");

		verify(systemNoticeWriter).upsertLastReadPost("user-id", "latest-post-id");
	}

	@Test
	void readingOlderNoticeAfterLatestDoesNotMoveReadPointerBack() {
		givenActiveViewer();
		given(viewer.getId()).willReturn("user-id");
		given(latestPost.getId()).willReturn("latest-post-id");
		given(systemNoticeReader.findLatestPost()).willReturn(Optional.of(latestPost));
		givenReadableSystemNotice(latestPost);

		systemNoticeService.markAsRead(viewer, "latest-post-id");

		assertThatThrownBy(() -> systemNoticeService.markAsRead(viewer, "old-post-id"))
			.isInstanceOf(BaseRunTimeV2Exception.class)
			.extracting("errorCode")
			.isEqualTo(PostErrorCode.POST_NOT_FOUND);

		verify(systemNoticeWriter).upsertLastReadPost("user-id", "latest-post-id");
	}

	@Test
	void markAsReadRejectsDeletedNoticeExcludedFromLatestLookup() {
		givenActiveViewer();
		given(systemNoticeReader.findLatestPost()).willReturn(Optional.empty());

		assertThatThrownBy(() -> systemNoticeService.markAsRead(viewer, "deleted-post-id"))
			.isInstanceOf(BaseRunTimeV2Exception.class)
			.extracting("errorCode")
			.isEqualTo(PostErrorCode.POST_NOT_FOUND);

		verify(systemNoticeReader, never()).findReadByUserId("user-id");
		verify(systemNoticeWriter, never()).upsertLastReadPost(
			org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString());
	}

	@Test
	void getLatestRejectsInactiveViewerBeforeLookingUpPost() {
		given(viewer.getState()).willReturn(UserState.AWAIT);

		assertThatThrownBy(() -> systemNoticeService.getLatest(viewer))
			.isInstanceOf(BaseRunTimeV2Exception.class)
			.extracting("errorCode")
			.isEqualTo(AuthErrorCode.INVALID_REGISTRATION_STATUS);

		verify(systemNoticeReader, never()).findLatestPost();
	}

	@Test
	void markAsReadRejectsViewerWithoutBoardReadPermission() {
		givenActiveViewer();
		given(viewer.getId()).willReturn("user-id");
		given(latestPost.getId()).willReturn("latest-post-id");
		given(systemNoticeReader.findLatestPost()).willReturn(Optional.of(latestPost));
		given(latestPost.getBoard()).willReturn(systemNoticeBoard);
		given(latestPost.getIsDeleted()).willReturn(false);
		given(systemNoticeBoard.getId()).willReturn("system-notice-board-id");
		given(systemNoticeBoard.getIsDeleted()).willReturn(false);
		given(boardConfigReader.getByBoardId("system-notice-board-id"))
			.willReturn(systemNoticeBoardConfig);
		given(boardConfigReader.getAdminIdsByBoardId("system-notice-board-id"))
			.willReturn(java.util.List.of());
		given(systemNoticeBoardConfig.getVisibility()).willReturn(BoardVisibility.HIDDEN);

		assertThatThrownBy(() -> systemNoticeService.markAsRead(viewer, "latest-post-id"))
			.isInstanceOf(BaseRunTimeV2Exception.class)
			.extracting("errorCode")
			.isEqualTo(BoardErrorCode.BOARD_FORBIDDEN);

		verify(systemNoticeWriter, never()).upsertLastReadPost(
			org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString());
	}

	private void givenActiveViewer() {
		given(viewer.getState()).willReturn(UserState.ACTIVE);
		given(viewer.getRoles()).willReturn(Set.of(Role.COMMON));
	}

	private void givenReadableSystemNotice(Post post) {
		given(post.getBoard()).willReturn(systemNoticeBoard);
		given(post.getIsDeleted()).willReturn(false);
		given(systemNoticeBoard.getId()).willReturn("system-notice-board-id");
		given(systemNoticeBoard.getIsDeleted()).willReturn(false);
		given(boardConfigReader.getByBoardId("system-notice-board-id"))
			.willReturn(systemNoticeBoardConfig);
		given(boardConfigReader.getAdminIdsByBoardId("system-notice-board-id"))
			.willReturn(java.util.List.of());
		given(systemNoticeBoardConfig.getVisibility()).willReturn(BoardVisibility.VISIBLE);
		given(systemNoticeBoardConfig.getReadScope()).willReturn(BoardReadScope.BOTH);
	}
}
