package net.causw.app.main.domain.community.systemnotice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.never;
import static org.mockito.BDDMockito.verify;

import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import net.causw.app.main.domain.community.systemnotice.entity.UserSystemNoticeRead;
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
	void markAsReadUpdatesPointerWhenRequestedPostIsLatest() {
		givenActiveViewer();
		given(viewer.getId()).willReturn("user-id");
		given(latestPost.getId()).willReturn("latest-post-id");
		given(systemNoticeReader.findLatestPost()).willReturn(Optional.of(latestPost));
		givenReadableSystemNotice(latestPost);
		given(systemNoticeReader.findReadByUserId("user-id")).willReturn(Optional.empty());

		systemNoticeService.markAsRead(viewer, "latest-post-id");

		verify(systemNoticeWriter).save(org.mockito.ArgumentMatchers.argThat(read ->
			read.getUserId().equals("user-id") && read.getLastReadPost() == latestPost));
	}

	@Test
	void readingOlderNoticeAfterLatestDoesNotMoveReadPointerBack() {
		givenActiveViewer();
		given(viewer.getId()).willReturn("user-id");
		given(latestPost.getId()).willReturn("latest-post-id");
		given(systemNoticeReader.findLatestPost()).willReturn(Optional.of(latestPost));
		givenReadableSystemNotice(latestPost);
		given(systemNoticeReader.findReadByUserId("user-id")).willReturn(Optional.empty());

		systemNoticeService.markAsRead(viewer, "latest-post-id");

		org.mockito.ArgumentCaptor<UserSystemNoticeRead> captor =
			org.mockito.ArgumentCaptor.forClass(UserSystemNoticeRead.class);
		verify(systemNoticeWriter).save(captor.capture());
		UserSystemNoticeRead read = captor.getValue();

		assertThatThrownBy(() -> systemNoticeService.markAsRead(viewer, "old-post-id"))
			.isInstanceOf(BaseRunTimeV2Exception.class)
			.extracting("errorCode")
			.isEqualTo(PostErrorCode.POST_NOT_FOUND);

		assertThat(read.getLastReadPost()).isSameAs(latestPost);
		verify(systemNoticeWriter).save(org.mockito.ArgumentMatchers.any(UserSystemNoticeRead.class));
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
		verify(systemNoticeWriter, never()).save(org.mockito.ArgumentMatchers.any(UserSystemNoticeRead.class));
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

		verify(systemNoticeWriter, never()).save(org.mockito.ArgumentMatchers.any(UserSystemNoticeRead.class));
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
