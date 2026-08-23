package net.causw.app.main.domain.integration.crawled.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
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
import net.causw.app.main.domain.integration.crawled.entity.CrawledFileLink;
import net.causw.app.main.domain.integration.crawled.entity.CrawledNotice;
import net.causw.app.main.domain.integration.crawled.service.implementation.CrawledNoticeReader;
import net.causw.app.main.domain.integration.crawled.service.implementation.CrawledNoticeWriter;
import net.causw.app.main.domain.integration.crawled.service.implementation.CrawledPostImageWriter;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.service.implementation.UserReader;
import net.causw.app.main.shared.exception.BaseRunTimeV2Exception;
import net.causw.app.main.shared.exception.errorcode.IntegrationErrorCode;
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
	@DisplayName("새 크롤링 게시물 본문에는 제목, 첨부파일, 원문 링크를 포함하지 않는다")
	void transfer_shouldStoreOnlyBodyHtml_whenCreatingCrawledPost() {
		// given
		CrawledNotice notice = CrawledNotice.of(
			"site", "external-id", "board-id", "공지", "제목", "<p>본문</p><img src=\"image.png\">",
			"https://example.com/notice", "관리자", LocalDate.now(), null,
			List.of(CrawledFileLink.of("첨부.pdf", "https://example.com/attachment.pdf")), "content-hash");
		given(crawledNoticeReader.findById(notice.getId())).willReturn(notice);
		given(userReader.findByEmail(StaticValue.SYSTEM_CRAWLER_ACCOUNT))
			.willReturn(Optional.of(org.mockito.Mockito.mock(User.class)));
		given(boardReader.getById("board-id")).willReturn(org.mockito.Mockito.mock(Board.class));

		// when
		transferService.transfer(notice.getId());

		// then
		org.mockito.ArgumentCaptor<Post> postCaptor = org.mockito.ArgumentCaptor.forClass(Post.class);
		verify(postWriter).save(postCaptor.capture());
		assertThat(postCaptor.getValue().getContent()).isEqualTo("<p>본문</p>");
	}

	@Test
	@DisplayName("연결된 Post가 없는 오늘 공지는 새 Post로 생성한다")
	void transfer_shouldCreatePost_whenUnlinkedNoticeIsAnnouncedToday() {
		// given
		CrawledNotice notice = notice(LocalDate.now());
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
	@DisplayName("연결된 Post가 없는 과거 공지는 전송 완료만 기록한다")
	void transfer_shouldMarkTransferredWithoutCreatingPost_whenUnlinkedNoticeIsNotAnnouncedToday() {
		// given
		CrawledNotice notice = notice(LocalDate.now().minusDays(1));
		given(crawledNoticeReader.findById(notice.getId())).willReturn(notice);

		// when
		transferService.transfer(notice.getId());

		// then
		verify(crawledNoticeWriter).markTransferred(notice);
		then(postWriter).shouldHaveNoInteractions();
		then(userReader).shouldHaveNoInteractions();
		then(boardReader).shouldHaveNoInteractions();
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

	@Test
	@DisplayName("크롤링 시스템 계정이 없으면 통합 오류 코드로 예외를 발생시킨다")
	void transfer_shouldThrowIntegrationException_whenSystemUserDoesNotExist() {
		// given
		CrawledNotice notice = notice(LocalDate.now());
		given(crawledNoticeReader.findById(notice.getId())).willReturn(notice);
		given(userReader.findByEmail(StaticValue.SYSTEM_CRAWLER_ACCOUNT)).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> transferService.transfer(notice.getId()))
			.isInstanceOf(BaseRunTimeV2Exception.class)
			.extracting(exception -> ((BaseRunTimeV2Exception)exception).getErrorCode())
			.isEqualTo(IntegrationErrorCode.CRAWL_SYSTEM_USER_NOT_FOUND);
	}

	private CrawledNotice notice(LocalDate announceDate) {
		return CrawledNotice.of(
			"site", "external-id", "board-id", "공지", "제목", "본문", "https://example.com/notice", "관리자",
			announceDate, null, List.of(), "content-hash");
	}
}
