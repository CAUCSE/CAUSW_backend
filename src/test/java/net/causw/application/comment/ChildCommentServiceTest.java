package net.causw.application.comment;

import net.causw.adapter.persistence.comment.ChildComment;
import net.causw.adapter.persistence.comment.Comment;
import net.causw.adapter.persistence.post.Post;
import net.causw.adapter.persistence.repository.*;
import net.causw.adapter.persistence.user.User;
import net.causw.application.dto.comment.ChildCommentCreateRequestDto;
import net.causw.application.dto.comment.ChildCommentResponseDto;
import net.causw.application.dto.comment.ChildCommentUpdateRequestDto;
import net.causw.application.util.ObjectFixtures;
import net.causw.domain.exceptions.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.validation.Validator;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@SpringBootTest(classes = {ChildCommentService.class})
class ChildCommentServiceTest {

    @Autowired
    ChildCommentService childCommentService;

    @MockBean
    ChildCommentRepository childCommentRepository;

    @MockBean
    CommentRepository commentRepository;

    @MockBean
    UserRepository userRepository;

    @MockBean
    CircleMemberRepository circleMemberRepository;

    @MockBean
    PostRepository postRepository;

    @MockBean
    Validator validator;

    User user;
    Comment comment;
    Post post;
    ChildComment childComment;
    ChildCommentCreateRequestDto childCommentCreateRequestDto;
    ChildCommentUpdateRequestDto childCommentUpdateRequestDto;

    @BeforeEach
    void setUp() {
        user = ObjectFixtures.getUser();
        comment = ObjectFixtures.getComment(false);
        post = ObjectFixtures.getPost(false);
        childComment = ObjectFixtures.getChildComment("content", false);
        childCommentCreateRequestDto = new ChildCommentCreateRequestDto("content", comment.getId(), "ref");
        childCommentUpdateRequestDto = new ChildCommentUpdateRequestDto("updated");
        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
        given(childCommentRepository.findById(childComment.getId())).willReturn(Optional.of(childComment));
        given(commentRepository.findById(comment.getId())).willReturn(Optional.of(comment));
        given(postRepository.findById(post.getId())).willReturn(Optional.of(post));
    }

    @DisplayName("대댓글 생성")
    @Test
    void createChildComment() {
        // Given
        given(childCommentRepository.findById("ref")).willReturn(Optional.of(childComment));
        ChildComment newChildComment = ChildComment.of(childCommentCreateRequestDto.getContent(), false, childComment.getWriter().getName(), "ref", user, comment);
        given(childCommentRepository.save(any())).willReturn(newChildComment);

        // When
        ChildCommentResponseDto childCommentResponseDto = childCommentService.createChildComment(user.getId(), childCommentCreateRequestDto);

        // Then
        assertEquals(newChildComment.getContent(), childCommentResponseDto.getContent());
    }

    @DisplayName("대댓글 수정")
    @Test
    void updateChildComment() {
        // Given
        ChildComment updated = ObjectFixtures.getChildComment("updated", false);
        given(childCommentRepository.save(childComment)).willReturn(updated);

        // When
        ChildCommentResponseDto childCommentResponseDto = childCommentService.updateChildComment(user.getId(), childComment.getId(), childCommentUpdateRequestDto);

        // Then
        assertEquals(childCommentResponseDto.getContent(), updated.getContent());
    }

    @DisplayName("대댓글 삭제")
    @Test
    void deleteChildComment() {
        // Given
        ChildComment deleted = ObjectFixtures.getChildComment("content", true);
        given(childCommentRepository.save(childComment)).willReturn(deleted);

        // When
        ChildCommentResponseDto childCommentResponseDto = childCommentService.deleteChildComment(user.getId(), childComment.getId());

        // Then
        assertTrue(childCommentResponseDto.getIsDeleted());
        assertFalse(childCommentResponseDto.getUpdatable());
        assertFalse(childCommentResponseDto.getDeletable());
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
            assertThrows(BadRequestException.class, () -> childCommentService.createChildComment(user.getId(), childCommentCreateRequestDto));
            assertThrows(BadRequestException.class, () -> childCommentService.updateChildComment(user.getId(), childComment.getId(), childCommentUpdateRequestDto));
            assertThrows(BadRequestException.class, () -> childCommentService.deleteChildComment(user.getId(), childComment.getId()));
        }

        @DisplayName("대댓글이 존재하지 않을 때")
        @Test
        void childCommentNotFound() {
            // Given
            given(childCommentRepository.findById(childComment.getId())).willReturn(Optional.empty());

            // When & Then
            assertThrows(BadRequestException.class, () -> childCommentService.createChildComment(user.getId(), childCommentCreateRequestDto));
            assertThrows(BadRequestException.class, () -> childCommentService.updateChildComment(user.getId(), childComment.getId(), childCommentUpdateRequestDto));
            assertThrows(BadRequestException.class, () -> childCommentService.deleteChildComment(user.getId(), childComment.getId()));
        }

        @DisplayName("댓글이 존재하지 않을 때")
        @Test
        void commentNotFound() {
            // Given
            given(commentRepository.findById(comment.getId())).willReturn(Optional.empty());

            // When & Then
            assertThrows(BadRequestException.class, () -> childCommentService.createChildComment(user.getId(), childCommentCreateRequestDto));
        }

        @DisplayName("게시글이 존재하지 않을 때")
        @Test
        void postNotFound() {
            // Given
            given(postRepository.findById(post.getId())).willReturn(Optional.empty());

            // When & Then
            assertThrows(BadRequestException.class, () -> childCommentService.createChildComment(user.getId(), childCommentCreateRequestDto));
            assertThrows(BadRequestException.class, () -> childCommentService.updateChildComment(user.getId(), childComment.getId(), childCommentUpdateRequestDto));
            assertThrows(BadRequestException.class, () -> childCommentService.deleteChildComment(user.getId(), childComment.getId()));
        }
    }
}
