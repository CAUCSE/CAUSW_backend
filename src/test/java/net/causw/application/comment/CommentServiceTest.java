package net.causw.application.comment;

import net.causw.adapter.persistence.comment.Comment;
import net.causw.adapter.persistence.page.PageableFactory;
import net.causw.adapter.persistence.post.Post;
import net.causw.adapter.persistence.repository.*;
import net.causw.adapter.persistence.user.User;
import net.causw.application.dto.comment.CommentCreateRequestDto;
import net.causw.application.dto.comment.CommentResponseDto;
import net.causw.application.dto.comment.CommentUpdateRequestDto;
import net.causw.application.util.ObjectFixtures;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.model.util.StaticValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import javax.validation.Validator;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@SpringBootTest(classes = {CommentService.class})
class CommentServiceTest {

    @Autowired
    CommentService commentService;

    @MockBean
    CommentRepository commentRepository;

    @MockBean
    UserRepository userRepository;

    @MockBean
    PostRepository postRepository;

    @MockBean
    CircleMemberRepository circleMemberRepository;

    @MockBean
    ChildCommentRepository childCommentRepository;

    @MockBean
    PageableFactory pageableFactory;

    @MockBean
    Validator validator;

    User user;
    Post post;
    Comment comment;
    CommentCreateRequestDto commentCreateRequestDto;
    CommentUpdateRequestDto commentUpdateRequestDto;

    @BeforeEach
    void setUp() {
        user = ObjectFixtures.getUser();
        post = ObjectFixtures.getPost();
        comment = ObjectFixtures.getComment("content");
        commentCreateRequestDto = new CommentCreateRequestDto("content", post.getId());
        commentUpdateRequestDto = new CommentUpdateRequestDto("updated");
        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
        given(postRepository.findById(post.getId())).willReturn(Optional.of(post));
    }

    @DisplayName("댓글 생성")
    @Test
    void createComment() {
        // Given
        given(commentRepository.save(any())).willReturn(comment);

        // When
        CommentResponseDto result = commentService.createComment(user.getId(), commentCreateRequestDto);

        // Then
        assertEquals(commentCreateRequestDto.getContent(), result.getContent());
    }

    @DisplayName("댓글 조회")
    @Test
    void findAllComments() {
        // Given
        Comment comment2 = ObjectFixtures.getComment("content2");
        Comment comment3 = ObjectFixtures.getComment("content3");
        Pageable pageable = PageRequest.of(0, StaticValue.DEFAULT_COMMENT_PAGE_SIZE);
        Page<Comment> mockPage = new PageImpl<>(List.of(comment, comment2, comment3), pageable, 3);
        given(pageableFactory.create(0, StaticValue.DEFAULT_COMMENT_PAGE_SIZE)).willReturn(pageable);
        given(commentRepository.findByPost_IdOrderByCreatedAt(post.getId(), pageable)).willReturn(mockPage);
        given(childCommentRepository.findByParentComment_Id(any())).willReturn(List.of());

        // When
        Page<CommentResponseDto> result = commentService.findAllComments(user.getId(), post.getId(), 0);

        // Then
        assertEquals(3, result.getTotalElements());
        assertEquals(comment.getContent(), result.getContent().get(0).getContent());
        assertEquals(comment2.getContent(), result.getContent().get(1).getContent());
        assertEquals(comment3.getContent(), result.getContent().get(2).getContent());
    }

    @DisplayName("댓글 수정")
    @Test
    void updateComment() {
        // Given
        given(commentRepository.findById(comment.getId())).willReturn(Optional.of(comment));
        given(childCommentRepository.countByParentComment_IdAndIsDeletedIsFalse(comment.getId())).willReturn(0L);
        Comment updated = ObjectFixtures.getComment(commentUpdateRequestDto.getContent());
        given(commentRepository.save(comment)).willReturn(updated);

        // When
        CommentResponseDto result = commentService.updateComment(user.getId(), comment.getId(), commentUpdateRequestDto);

        // Then
        assertEquals(commentUpdateRequestDto.getContent(), result.getContent());
    }

    @DisplayName("댓글 삭제")
    @Test
    void deleteComment() {
        // Given
        given(commentRepository.findById(comment.getId())).willReturn(Optional.of(comment));
        given(childCommentRepository.countByParentComment_IdAndIsDeletedIsFalse(comment.getId())).willReturn(0L);
        Comment deleted = ObjectFixtures.getComment(true);
        given(commentRepository.save(comment)).willReturn(deleted);

        // When
        CommentResponseDto result = commentService.deleteComment(user.getId(), comment.getId());

        // Then
        assertTrue(result.getIsDeleted());
        assertFalse(result.getDeletable());
        assertFalse(result.getUpdatable());
    }

    @DisplayName("유저가 존재하지 않을 때")
    @Test
    void userNotFound() {
        // Given
        given(userRepository.findById(user.getId())).willReturn(Optional.empty());

        // When & Then
        assertThrows(BadRequestException.class, () -> commentService.createComment(user.getId(), commentCreateRequestDto));
        assertThrows(BadRequestException.class, () -> commentService.findAllComments(user.getId(), post.getId(), 0));
        assertThrows(BadRequestException.class, () -> commentService.updateComment(user.getId(), comment.getId(), commentUpdateRequestDto));
        assertThrows(BadRequestException.class, () -> commentService.deleteComment(user.getId(), comment.getId()));
    }

    @DisplayName("댓글이 존재하지 않을 때")
    @Test
    void commentNotFound() {
        // Given
        given(commentRepository.findById(comment.getId())).willReturn(Optional.empty());

        // When & Then
        assertThrows(BadRequestException.class, () -> commentService.updateComment(user.getId(), comment.getId(), commentUpdateRequestDto));
        assertThrows(BadRequestException.class, () -> commentService.deleteComment(user.getId(), comment.getId()));
    }

    @DisplayName("게시글이 존재하지 않을 때")
    @Test
    void postNotFound() {
        // Given
        given(postRepository.findById(post.getId())).willReturn(Optional.empty());

        // When & Then
        assertThrows(BadRequestException.class, () -> commentService.createComment(user.getId(), commentCreateRequestDto));
        assertThrows(BadRequestException.class, () -> commentService.findAllComments(user.getId(), post.getId(), 0));
        assertThrows(BadRequestException.class, () -> commentService.updateComment(user.getId(), comment.getId(), commentUpdateRequestDto));
        assertThrows(BadRequestException.class, () -> commentService.deleteComment(user.getId(), comment.getId()));
    }
}
