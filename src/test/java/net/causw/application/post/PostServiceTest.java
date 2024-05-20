package net.causw.application.post;

import net.causw.adapter.persistence.board.Board;
import net.causw.adapter.persistence.comment.Comment;
import net.causw.adapter.persistence.page.PageableFactory;
import net.causw.adapter.persistence.post.Post;
import net.causw.adapter.persistence.repository.*;
import net.causw.adapter.persistence.user.User;
import net.causw.application.dto.post.BoardPostsResponseDto;
import net.causw.application.dto.post.PostCreateRequestDto;
import net.causw.application.dto.post.PostResponseDto;
import net.causw.application.dto.post.PostUpdateRequestDto;
import net.causw.application.util.ObjectFixtures;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.model.enums.Role;
import net.causw.domain.model.util.StaticValue;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import javax.validation.Validator;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@SpringBootTest(classes = {PostService.class})
class PostServiceTest {

    @Autowired
    PostService postService;

    @MockBean
    PostRepository postRepository;

    @MockBean
    UserRepository userRepository;

    @MockBean
    BoardRepository boardRepository;

    @MockBean
    CircleMemberRepository circleMemberRepository;

    @MockBean
    CommentRepository commentRepository;

    @MockBean
    ChildCommentRepository childCommentRepository;

    @MockBean
    FavoriteBoardRepository favoriteBoardRepository;

    @MockBean
    PageableFactory pageableFactory;

    @MockBean
    Validator validator;

    User user;
    Post post;
    Board board;
    PostCreateRequestDto postCreateRequestDto;
    PostUpdateRequestDto postUpdateRequestDto;

    @BeforeEach
    void setUp() {
        user = ObjectFixtures.getUser();
        post = ObjectFixtures.getPost(false);
        board = ObjectFixtures.getBoard();
        postCreateRequestDto = new PostCreateRequestDto("title", "content", board.getId(), List.of("attachment"));
        postUpdateRequestDto = new PostUpdateRequestDto("updatedTitle", "updatedContent", List.of());
        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
        given(postRepository.findById(post.getId())).willReturn(Optional.of(post));
        given(boardRepository.findById(board.getId())).willReturn(Optional.of(board));
        given(pageableFactory.create(0, StaticValue.DEFAULT_COMMENT_PAGE_SIZE)).willReturn(PageRequest.of(0, StaticValue.DEFAULT_COMMENT_PAGE_SIZE));
    }

    @DisplayName("게시글 생성")
    @Test
    void createPost() {
        // Given
        PostCreateRequestDto postCreateRequestDto = new PostCreateRequestDto("title", "content", post.getBoard().getId(), List.of("attachment"));
        given(boardRepository.findById(post.getBoard().getId())).willReturn(Optional.of(post.getBoard()));
        given(postRepository.save(any())).willReturn(post);

        // When
        PostResponseDto result = postService.createPost(user.getId(), postCreateRequestDto);

        // Then
        assertEquals(postCreateRequestDto.getTitle(), result.getTitle());
        assertEquals(postCreateRequestDto.getContent(), result.getContent());
        assertEquals(post.getBoard().getName(), result.getBoardName());
    }

    @DisplayName("게시글 삭제")
    @Test
    void deletePost() {
        // Given
        Post deleted = ObjectFixtures.getPost(true);
        given(postRepository.save(post)).willReturn(deleted);

        // When
        PostResponseDto result = postService.deletePost(user.getId(), post.getId());

        // Then
        assertTrue(result.getIsDeleted());
        assertFalse(result.getUpdatable());
        assertFalse(result.getDeletable());
    }

    @DisplayName("특정 게시글 조회")
    @Test
    void findPostById() {
        // Given
        Comment comment = ObjectFixtures.getComment("content");
        Comment comment2 = ObjectFixtures.getComment("content2");
        Comment comment3 = ObjectFixtures.getComment("content3");
        Page<Comment> comments = new PageImpl<>(List.of(comment, comment2, comment3));
        given(postRepository.save(post)).willReturn(post);
        given(postRepository.countAllCommentByPost_Id(post.getId())).willReturn(3L);
        given(commentRepository.findByPost_IdOrderByCreatedAt(post.getId(), PageRequest.of(0, StaticValue.DEFAULT_COMMENT_PAGE_SIZE))).willReturn(comments);
        given(childCommentRepository.countByParentComment_IdAndIsDeletedIsFalse(any())).willReturn(0L);

        // When
        PostResponseDto postResponseDto = postService.findPostById(user.getId(), post.getId());

        // Then
        assertEquals(post.getTitle(), postResponseDto.getTitle());
        assertEquals(post.getContent(), postResponseDto.getContent());
        assertEquals(post.getBoard().getName(), postResponseDto.getBoardName());
        assertEquals(3, postResponseDto.getNumComment());
        assertEquals(comment.getContent(), postResponseDto.getCommentList().getContent().get(0).getContent());
        assertEquals(comment2.getContent(), postResponseDto.getCommentList().getContent().get(1).getContent());
        assertEquals(comment3.getContent(), postResponseDto.getCommentList().getContent().get(2).getContent());
    }

    @DisplayName("게시글 수정")
    @Test
    void updatePost() {
        // Given
        Post updated = ObjectFixtures.getPost("updatedTitle", "updatedContent");
        given(postRepository.save(post)).willReturn(updated);
        given(postRepository.countAllCommentByPost_Id(post.getId())).willReturn(0L);
        given(commentRepository.findByPost_IdOrderByCreatedAt(post.getId(), PageRequest.of(0, StaticValue.DEFAULT_COMMENT_PAGE_SIZE))).willReturn(new PageImpl<>(List.of()));
        given(childCommentRepository.countByParentComment_IdAndIsDeletedIsFalse(any())).willReturn(0L);

        // When
        PostResponseDto result = postService.updatePost(user.getId(), post.getId(), postUpdateRequestDto);

        // Then
        assertEquals(postUpdateRequestDto.getTitle(), result.getTitle());
        assertEquals(postUpdateRequestDto.getContent(), result.getContent());
    }

    @DisplayName("게시글 복구")
    @Test
    void restorePost() {
        // Given
        Post deleted = ObjectFixtures.getPost(true);
        Post restored = ObjectFixtures.getPost(false);
        given(postRepository.findById(deleted.getId())).willReturn(Optional.of(deleted));
        given(postRepository.save(any())).willReturn(restored);
        given(postRepository.countAllCommentByPost_Id(deleted.getId())).willReturn(0L);
        given(commentRepository.findByPost_IdOrderByCreatedAt(deleted.getId(), PageRequest.of(0, StaticValue.DEFAULT_COMMENT_PAGE_SIZE))).willReturn(new PageImpl<>(List.of()));
        given(childCommentRepository.countByParentComment_IdAndIsDeletedIsFalse(any())).willReturn(0L);

        // When
        PostResponseDto result = postService.restorePost(user.getId(), deleted.getId());

        // Then
        assertFalse(result.getIsDeleted());
        assertTrue(result.getUpdatable());
        assertTrue(result.getDeletable());
    }

    @DisplayName("게시글 목록 조회")
    @Test
    void findAllPost() {
        // Given
        given(postRepository.findAllByBoard_IdOrderByCreatedAtDesc(board.getId(), pageableFactory.create(0, StaticValue.DEFAULT_POST_PAGE_SIZE)))
                .willReturn(new PageImpl<>(List.of(post)));

        // When
        BoardPostsResponseDto result = postService.findAllPost(user.getId(), board.getId(), 0);

        // Then
        assertEquals(board.getName(), result.getBoardName());
        assertEquals(1, result.getPost().getTotalElements());
    }

    @DisplayName("게시글 목록 조회 - 삭제된 게시물도 포함")
    @Test
    void findAllPostIncludeDeleted() {
        // Given
        User common = ObjectFixtures.getUser(Role.COMMON);
        given(userRepository.findById(common.getId())).willReturn(Optional.of(common));
        given(postRepository.findAllByBoard_IdAndIsDeletedOrderByCreatedAtDesc(board.getId(), pageableFactory.create(0, StaticValue.DEFAULT_POST_PAGE_SIZE), false))
                .willReturn(new PageImpl<>(List.of(post)));

        // When
        BoardPostsResponseDto result = postService.findAllPost(common.getId(), board.getId(), 0);

        // Then
        assertEquals(board.getName(), result.getBoardName());
        assertEquals(1, result.getPost().getTotalElements());
    }

    @DisplayName("게시글 검색")
    @Test
    void searchPost() {
        // Given
        given(postRepository.findAllByBoard_IdOrderByCreatedAtDesc(board.getId(), pageableFactory.create(0, StaticValue.DEFAULT_POST_PAGE_SIZE)))
                .willReturn(new PageImpl<>(List.of(post)));

        // When
        BoardPostsResponseDto result = postService.searchPost(user.getId(), board.getId(), "title", 0);

        // Then
        assertEquals(board.getName(), result.getBoardName());
        assertEquals(1, result.getPost().getTotalElements());
    }

    @DisplayName("게시글 검색 - 삭제된 게시물도 포함")
    @Test
    void searchPostIncludeDeleted() {
        // Given
        User common = ObjectFixtures.getUser(Role.COMMON);
        given(userRepository.findById(common.getId())).willReturn(Optional.of(common));
        given(postRepository.searchByTitle("title", board.getId(), pageableFactory.create(0, StaticValue.DEFAULT_POST_PAGE_SIZE), false))
                .willReturn(new PageImpl<>(List.of(post)));

        // When
        BoardPostsResponseDto result = postService.searchPost(common.getId(), board.getId(), "title", 0);

        // Then
        assertEquals(board.getName(), result.getBoardName());
        assertEquals(1, result.getPost().getTotalElements());
    }

    @DisplayName("게시글 공지사항 조회")
    @Test
    void findAllAppNotice() {
        // Given
        given(boardRepository.findAppNotice()).willReturn(Optional.of(board));
        given(postRepository.findAllByBoard_IdOrderByCreatedAtDesc(board.getId(), pageableFactory.create(0, StaticValue.DEFAULT_POST_PAGE_SIZE)))
                .willReturn(new PageImpl<>(List.of()));

        // When
        BoardPostsResponseDto result = postService.findAllAppNotice(user.getId(), 0);

        // Then
        assertEquals(board.getName(), result.getBoardName());
        assertEquals(0, result.getPost().getTotalElements());
    }

    @DisplayName("Exception Handling")
    @Nested
    class ExceptionTest {

        @DisplayName("유저가 존재하지 않을 때")
        @Test
        void userNotFound() {
            // Given
            given(userRepository.findById(user.getId())).willReturn(Optional.empty());

            // When & Then
            assertThrows(BadRequestException.class, () -> postService.createPost(user.getId(), postCreateRequestDto));
            assertThrows(BadRequestException.class, () -> postService.deletePost(user.getId(), post.getId()));
            assertThrows(BadRequestException.class, () -> postService.findPostById(user.getId(), post.getId()));
            assertThrows(BadRequestException.class, () -> postService.updatePost(user.getId(), post.getId(), postUpdateRequestDto));
            assertThrows(BadRequestException.class, () -> postService.restorePost(user.getId(), post.getId()));
            assertThrows(BadRequestException.class, () -> postService.findAllPost(user.getId(), board.getId(), 0));
            assertThrows(BadRequestException.class, () -> postService.searchPost(user.getId(), board.getId(), "title", 0));
            assertThrows(BadRequestException.class, () -> postService.findAllAppNotice(user.getId(), 0));
        }
        
        @DisplayName("게시글이 존재하지 않을 때")
        @Test
        void postNotFound() {
            // Given
            given(postRepository.findById(post.getId())).willReturn(Optional.empty());

            // When & Then
            assertThrows(BadRequestException.class, () -> postService.deletePost(user.getId(), post.getId()));
            assertThrows(BadRequestException.class, () -> postService.findPostById(user.getId(), post.getId()));
            assertThrows(BadRequestException.class, () -> postService.updatePost(user.getId(), post.getId(), postUpdateRequestDto));
            assertThrows(BadRequestException.class, () -> postService.restorePost(user.getId(), post.getId()));
            assertThrows(BadRequestException.class, () -> postService.findAllAppNotice(user.getId(), 0));
        }

        @DisplayName("게시판이 존재하지 않을 때")
        @Test
        void boardNotFound() {
            // Given
            given(boardRepository.findById(board.getId())).willReturn(Optional.empty());

            // When & Then
            assertThrows(BadRequestException.class, () -> postService.createPost(user.getId(), postCreateRequestDto));
            assertThrows(BadRequestException.class, () -> postService.restorePost(user.getId(), post.getId()));
            assertThrows(BadRequestException.class, () -> postService.findAllPost(user.getId(), board.getId(), 0));
            assertThrows(BadRequestException.class, () -> postService.searchPost(user.getId(), board.getId(), "title", 0));
            assertThrows(BadRequestException.class, () -> postService.findAllAppNotice(user.getId(), 0));
        }
    }
}
