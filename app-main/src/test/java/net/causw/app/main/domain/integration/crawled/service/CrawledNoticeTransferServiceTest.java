package net.causw.app.main.domain.integration.crawled.service;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.verify;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.causw.app.main.domain.community.board.entity.Board;
import net.causw.app.main.domain.community.board.service.implementation.BoardReader;
import net.causw.app.main.domain.community.post.entity.Post;
import net.causw.app.main.domain.community.post.service.implementation.PostWriter;
import net.causw.app.main.domain.integration.crawled.entity.CrawledNotice;
import net.causw.app.main.domain.integration.crawled.service.implementation.CrawledNoticeReader;
import net.causw.app.main.domain.integration.crawled.service.implementation.CrawledNoticeWriter;
import net.causw.app.main.domain.integration.crawled.service.implementation.CrawledPostImageWriter;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.service.implementation.UserReader;
import net.causw.global.constant.StaticValue;

@ExtendWith(MockitoExtension.class)
@DisplayName("CrawledNoticeTransferService 테스트")
class CrawledNoticeTransferServiceTest {
	@InjectMocks
	private CrawledNoticeTransferService transferService;

	@Mock
	private CrawledNoticeReader crawledNoticeReader;
	@Mock
	private CrawledNoticeWriter crawledNoticeWriter;
	@Mock
	private PostWriter postWriter;
	@Mock
	private UserReader userReader;
	@Mock
	private BoardReader boardReader;
	@Mock
	private CrawledPostImageWriter crawledPostImageWriter;
	@Mock
	private org.springframework.context.ApplicationEventPublisher applicationEventPublisher;

	@Test
	@DisplayName("연결된 Post가 없는 전송 대기 공지는 공지일과 관계없이 새 Post로 생성한다")
	void transfer_shouldCreatePost_whenUnlinkedNoticeIsPending() {
		// given
		CrawledNotice notice = notice(LocalDate.of(2026, 8, 10));
		given(crawledNoticeReader.findById(notice.getId())).willReturn(notice);
		given(userReader.findByEmail(StaticValue.SYSTEM_CRAWLER_ACCOUNT))
			.willReturn(Optional.of(org.mockito.Mockito.mock(User.class)));
		given(boardReader.getById("board-id")).willReturn(org.mockito.Mockito.mock(Board.class));

		// when
		transferService.transfer(notice.getId());

		// then
		verify(postWriter).save(org.mockito.ArgumentMatchers.any(Post.class));
		verify(crawledNoticeWriter).markTransferred(org.mockito.ArgumentMatchers.eq(notice),
			org.mockito.ArgumentMatchers.any(Post.class));
	}

	@Test
	@DisplayName("연결된 기존 Post는 공지일과 관계없이 갱신한다")
	void transfer_shouldUpdatePost_whenLinkedNoticeExists() {
		// given
		CrawledNotice notice = notice(LocalDate.of(2026, 8, 10));
		Post existingPost = org.mockito.Mockito.mock(Post.class);
		Board board = org.mockito.Mockito.mock(Board.class);
		notice.linkPost(existingPost);
		given(crawledNoticeReader.findById(notice.getId())).willReturn(notice);
		given(existingPost.getIsDeleted()).willReturn(false);
		given(existingPost.getBoard()).willReturn(board);
		given(board.getId()).willReturn("board-id");
		given(existingPost.getIsAnonymous()).willReturn(false);
		given(existingPost.getPostAttachImageList()).willReturn(List.of());
		given(userReader.findByEmail(StaticValue.SYSTEM_CRAWLER_ACCOUNT))
			.willReturn(Optional.of(org.mockito.Mockito.mock(User.class)));
		given(boardReader.getById("board-id")).willReturn(board);

		// when
		transferService.transfer(notice.getId());

		// then
		verify(existingPost).update(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString(),
			org.mockito.ArgumentMatchers.eq(false), org.mockito.ArgumentMatchers.eq(List.of()));
		verify(postWriter).save(existingPost);
		verify(crawledNoticeWriter).markTransferred(notice, existingPost);
	}

	private CrawledNotice notice(LocalDate announceDate) {
		return CrawledNotice.of(
			"site", "external-id", "board-id", "공지", "제목", "본문", "https://example.com/notice", "관리자",
			announceDate, null, List.of(), "content-hash");
	}
}
