package net.causw.application.crawler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import net.causw.adapter.persistence.board.Board;
import net.causw.adapter.persistence.crawled.CrawledNotice;
import net.causw.adapter.persistence.post.Post;
import net.causw.adapter.persistence.repository.board.BoardRepository;
import net.causw.adapter.persistence.repository.crawled.CrawledNoticeRepository;
import net.causw.adapter.persistence.repository.post.PostRepository;
import net.causw.adapter.persistence.repository.user.UserRepository;
import net.causw.adapter.persistence.user.User;
import net.causw.domain.model.util.StaticValue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("CrawledToPostTransferService 테스트")
public class CrawledToPostTransferServiceTest {

    @InjectMocks
    private CrawledToPostTransferService crawledToPostTransferService;

    @Mock
    private CrawledNoticeRepository crawledNoticeRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BoardRepository boardRepository;

    @Test
    @DisplayName("새 공지사항이 Post로 변환되어 저장됨")
    void transferToPosts_shouldCreateNewPost_whenNewNotice() {
        // given
        Board mockBoard = createMockBoard();
        User mockUser = createMockUser();
        CrawledNotice newNotice = CrawledNoticeFixture.newNotice();
        
        given(boardRepository.findByName(StaticValue.CrawlingBoard))
            .willReturn(Optional.of(mockBoard));
        given(userRepository.findByStudentId(StaticValue.ADMIN_STUDENT_ID))
            .willReturn(Optional.of(mockUser));
        given(crawledNoticeRepository.findTop30ByIsUpdatedTrueOrderByLastModifiedDesc())
            .willReturn(List.of(newNotice));
        given(postRepository.findAllByBoardAndIsDeletedIsFalse(mockBoard))
            .willReturn(Collections.emptyList());

        // when
        crawledToPostTransferService.transferToPosts();

        // then
        verify(postRepository).save(any(Post.class));
        verify(crawledNoticeRepository).save(newNotice);
    }

    @Test
    @DisplayName("수정된 공지사항이 기존 Post를 업데이트함")
    void transferToPosts_shouldUpdateExistingPost_whenNoticeUpdated() {
        // given
        Board mockBoard = createMockBoard();
        User mockUser = createMockUser();
        CrawledNotice updatedNotice = CrawledNoticeFixture.updatedNotice();
        Post existingPost = createMockPost("수정된 공지사항");
        
        given(boardRepository.findByName(StaticValue.CrawlingBoard))
            .willReturn(Optional.of(mockBoard));
        given(userRepository.findByStudentId(StaticValue.ADMIN_STUDENT_ID))
            .willReturn(Optional.of(mockUser));
        given(crawledNoticeRepository.findTop30ByIsUpdatedTrueOrderByLastModifiedDesc())
            .willReturn(List.of(updatedNotice));
        given(postRepository.findAllByBoardAndIsDeletedIsFalse(mockBoard))
            .willReturn(List.of(existingPost));

        // when
        crawledToPostTransferService.transferToPosts();

        // then
        verify(postRepository).save(existingPost);
        verify(crawledNoticeRepository).save(updatedNotice);
    }

    @Test
    @DisplayName("업데이트된 공지사항이 없으면 아무것도 처리하지 않음")
    void transferToPosts_shouldDoNothing_whenNoUpdatedNotices() {
        // given
        Board mockBoard = createMockBoard();
        User mockUser = createMockUser();
        
        given(boardRepository.findByName(StaticValue.CrawlingBoard))
            .willReturn(Optional.of(mockBoard));
        given(userRepository.findByStudentId(StaticValue.ADMIN_STUDENT_ID))
            .willReturn(Optional.of(mockUser));
        given(crawledNoticeRepository.findTop30ByIsUpdatedTrueOrderByLastModifiedDesc())
            .willReturn(Collections.emptyList());

        // when
        crawledToPostTransferService.transferToPosts();

        // then
        verify(postRepository, never()).save(any(Post.class));
    }

    private Board createMockBoard() {
        Board board = mock(Board.class);
        when(board.getName()).thenReturn(StaticValue.CrawlingBoard);
        return board;
    }

    private User createMockUser() {
        User user = mock(User.class);
        when(user.getStudentId()).thenReturn(StaticValue.ADMIN_STUDENT_ID);
        return user;
    }

    private Post createMockPost(String title) {
        Post post = mock(Post.class);
        when(post.getTitle()).thenReturn(title);
        return post;
    }
} 