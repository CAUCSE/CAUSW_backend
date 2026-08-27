package net.causw.app.main.domain.integration.crawled.service.implementation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.lenient;
import static org.mockito.BDDMockito.mock;
import static org.mockito.BDDMockito.verify;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import net.causw.app.main.domain.community.board.entity.Board;
import net.causw.app.main.domain.community.board.service.implementation.BoardReader;
import net.causw.app.main.domain.community.post.entity.Post;
import net.causw.app.main.domain.community.post.enums.PostCategory;
import net.causw.app.main.domain.community.post.service.implementation.PostWriter;
import net.causw.app.main.domain.community.post.util.PostCategoryClassifier;
import net.causw.app.main.domain.integration.crawled.entity.CrawledNotice;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.service.implementation.UserReader;

@ExtendWith(MockitoExtension.class)
@DisplayName("CrawledNoticeTransferProcessor 테스트")
class CrawledNoticeTransferProcessorTest {

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
	private ApplicationEventPublisher applicationEventPublisher;

	private CrawledNoticeTransferProcessor processor;

	@BeforeEach
	void setUp() {
		processor = new CrawledNoticeTransferProcessor(
			crawledNoticeReader,
			crawledNoticeWriter,
			postWriter,
			userReader,
			boardReader,
			crawledPostImageWriter,
			applicationEventPublisher,
			new PostCategoryClassifier());
	}

	@Test
	@DisplayName("새 Post 생성 시 제목으로 카테고리를 분류해 저장한다")
	void transfer_shouldClassifyCategoryWhenCreatingNewPost() {
		// given
		CrawledNotice notice = notice("2026 하반기 신입사원 채용 안내", null);
		given(crawledNoticeReader.findById("notice-1")).willReturn(notice);
		given(userReader.findByEmail(any())).willReturn(Optional.of(mock(User.class)));
		Board board = board("board-1");
		given(boardReader.getById("board-1")).willReturn(board);

		// when
		processor.transfer("notice-1");

		// then
		ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);
		verify(postWriter).save(postCaptor.capture());
		assertThat(postCaptor.getValue().getCategory()).isEqualTo(PostCategory.RECRUIT);
	}

	@Test
	@DisplayName("기존 Post 갱신 시 카테고리를 덮어쓰지 않는다")
	void transfer_shouldNotOverwriteCategoryWhenUpdatingExistingPost() {
		// given
		Board board = board("board-1");
		Post existingPost = Post.of("기존 제목", "기존 내용", mock(User.class), false, board, new ArrayList<>());
		existingPost.updateCategory(PostCategory.RESEARCH);

		CrawledNotice notice = notice("수강신청 일정 안내", existingPost);
		given(crawledNoticeReader.findById("notice-1")).willReturn(notice);
		given(userReader.findByEmail(any())).willReturn(Optional.of(mock(User.class)));
		given(boardReader.getById("board-1")).willReturn(board);

		// when
		processor.transfer("notice-1");

		// then
		assertThat(existingPost.getCategory()).isEqualTo(PostCategory.RESEARCH);
	}

	private CrawledNotice notice(String title, Post linkedPost) {
		CrawledNotice notice = mock(CrawledNotice.class);
		lenient().when(notice.getTitle()).thenReturn(title);
		lenient().when(notice.getContent()).thenReturn("<p>본문</p>");
		lenient().when(notice.getLink()).thenReturn("https://example.com/1");
		lenient().when(notice.getCrawledFileLinks()).thenReturn(List.of());
		lenient().when(notice.getTargetBoardId()).thenReturn("board-1");
		lenient().when(notice.getPost()).thenReturn(linkedPost);
		return notice;
	}

	private Board board(String id) {
		Board board = mock(Board.class);
		lenient().when(board.getId()).thenReturn(id);
		return board;
	}
}
