package net.causw.application.crawler;

import net.causw.adapter.persistence.board.Board;
import net.causw.adapter.persistence.crawled.CrawledFileLink;
import net.causw.adapter.persistence.crawled.CrawledNotice;
import net.causw.adapter.persistence.post.Post;
import net.causw.adapter.persistence.repository.board.BoardRepository;
import net.causw.adapter.persistence.repository.crawled.CrawledNoticeRepository;
import net.causw.adapter.persistence.repository.post.PostRepository;
import net.causw.adapter.persistence.repository.user.UserRepository;
import net.causw.adapter.persistence.user.User;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.model.util.MessageUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class CrawledToPostTransferServiceTest {

    @InjectMocks
    private CrawledToPostTransferService service;

    @Mock
    private BoardRepository boardRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CrawledNoticeRepository crawledNoticeRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private Board mockBoard;

    @Mock
    private User mockUser;

    @Mock
    private CrawledNotice mockNotice;

    @Mock
    private CrawledFileLink mockFileLink;

    @Captor
    private ArgumentCaptor<Post> postCaptor;

    @Nested
    @DisplayName("정상 동작 테스트")
    class SuccessTests {
        @BeforeEach
        void setUp() {
            given(boardRepository.findByName("소프트웨어학부 학부 공지"))
                    .willReturn(Optional.of(mockBoard));
            given(userRepository.findByStudentId("20220881"))
                    .willReturn(Optional.of(mockUser));
        }

        @Test
        @DisplayName("신규 공지 변환 및 저장")
        void transfer_NewNotices_SavesPosts() {
            // given
            given(mockNotice.getTitle()).willReturn("공지제목");
            given(mockNotice.getContent()).willReturn("공지내용");
            given(mockNotice.getImageLink()).willReturn("http://img");
            given(mockNotice.getLink()).willReturn("http://link");
            given(mockNotice.getCrawledFileLinks()).willReturn(List.of(mockFileLink));
            given(mockFileLink.getFileName()).willReturn("file.pdf");
            given(mockFileLink.getFileLink()).willReturn("http://file");
            given(postRepository.existsByBoardAndContentContains(mockBoard, "http://link"))
                    .willReturn(false);
            given(crawledNoticeRepository.findTop20ByOrderByAnnounceDateDesc())
                    .willReturn(List.of(mockNotice));

            // when
            service.transferCrawledNoticesToPosts();

            // then
            verify(postRepository, times(1)).save(postCaptor.capture());
            Post saved = postCaptor.getValue();

            assertThat(saved.getBoard()).isEqualTo(mockBoard);
            assertThat(saved.getWriter()).isEqualTo(mockUser);
            assertThat(saved.getTitle()).isEqualTo("공지제목");
            String content = saved.getContent();
            assertThat(content).contains("공지내용");
            assertThat(content).contains("![공지 이미지](http://img)");
            assertThat(content).contains("[첨부파일: file.pdf](http://file)");
            assertThat(content).contains("[공지 링크](http://link)");
        }

        @Test
        @DisplayName("이미 존재하는 공지는 저장하지 않음")
        void transfer_ExistingNotice_SkipsSave() {
            given(crawledNoticeRepository.findTop20ByOrderByAnnounceDateDesc())
                    .willReturn(List.of(mockNotice));
            given(postRepository.existsByBoardAndContentContains(mockBoard, "http://link"))
                    .willReturn(true);
            given(mockNotice.getLink()).willReturn("http://link");

            service.transferCrawledNoticesToPosts();

            verify(postRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("예외 상황 테스트")
    class ExceptionTests {

        @Test
        @DisplayName("게시판이 없으면 BadRequestException")
        void transfer_NoBoard_Throws() {
            given(boardRepository.findByName(anyString())).willReturn(Optional.empty());

            assertThatThrownBy(() -> service.transferCrawledNoticesToPosts())
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining(MessageUtil.BOARD_NOT_FOUND);

            verify(userRepository, never()).findByStudentId(anyString());
            verify(crawledNoticeRepository, never()).findTop20ByOrderByAnnounceDateDesc();
        }
    }
}