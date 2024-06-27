package net.causw.application.comment;

import lombok.RequiredArgsConstructor;
import net.causw.adapter.persistence.board.Board;
import net.causw.adapter.persistence.circle.Circle;
import net.causw.adapter.persistence.circle.CircleMember;
import net.causw.adapter.persistence.comment.Comment;
import net.causw.adapter.persistence.page.PageableFactory;
import net.causw.adapter.persistence.post.Post;
import net.causw.adapter.persistence.repository.*;
import net.causw.adapter.persistence.user.User;
import net.causw.application.dto.comment.CommentCreateRequestDto;
import net.causw.application.dto.comment.CommentResponseDto;
import net.causw.application.dto.comment.CommentUpdateRequestDto;
import net.causw.application.dto.util.DtoMapper;
import net.causw.application.dto.util.StatusUtil;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.exceptions.InternalServerException;
import net.causw.domain.exceptions.UnauthorizedException;
import net.causw.domain.model.enums.CircleMemberStatus;
import net.causw.domain.model.enums.Role;
import net.causw.domain.model.util.MessageUtil;
import net.causw.domain.model.util.StaticValue;
import net.causw.domain.validation.CircleMemberStatusValidator;
import net.causw.domain.validation.ConstraintValidator;
import net.causw.domain.validation.ContentsAdminValidator;
import net.causw.domain.validation.TargetIsDeletedValidator;
import net.causw.domain.validation.UserEqualValidator;
import net.causw.domain.validation.UserRoleIsNoneValidator;
import net.causw.domain.validation.UserStateValidator;
import net.causw.domain.validation.ValidatorBucket;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.Validator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CircleMemberRepository circleMemberRepository;
    private final ChildCommentRepository childCommentRepository;
    private final PageableFactory pageableFactory;
    private final Validator validator;

    @Transactional
    public CommentResponseDto createComment(String loginUserId, CommentCreateRequestDto commentCreateDto) {
        User creator = getUser(loginUserId);
        Post post = getPost(commentCreateDto.getPostId());
        Comment comment = Comment.of(commentCreateDto.getContent(), false, creator, post);

        ValidatorBucket validatorBucket = initializeValidator(creator, post);
        validatorBucket.
                consistOf(ConstraintValidator.of(comment, this.validator));
        validatorBucket.validate();

        return toCommentResponseDto(commentRepository.save(comment), creator, post.getBoard());
    }

    @Transactional(readOnly = true)
    public Page<CommentResponseDto> findAllComments(String loginUserId, String postId, Integer pageNum) {
        User user = getUser(loginUserId);
        Post post = getPost(postId);

        ValidatorBucket validatorBucket = initializeValidator(user, post);
        validatorBucket.validate();

        Page<Comment> comments = commentRepository.findByPost_IdOrderByCreatedAt(
                postId,
                pageableFactory.create(pageNum, StaticValue.DEFAULT_COMMENT_PAGE_SIZE)
        );
        comments.forEach(comment -> comment.setChildCommentList(childCommentRepository.findByParentComment_Id(comment.getId())));

        return comments.map(comment -> toCommentResponseDto(comment, user, post.getBoard()));
    }

    @Transactional
    public CommentResponseDto updateComment(
            String loginUserId,
            String commentId,
            CommentUpdateRequestDto commentUpdateRequestDto
    ) {
        User updater = getUser(loginUserId);
        Comment comment = getComment(commentId);
        Post post = getPost(comment.getPost().getId());

        ValidatorBucket validatorBucket = initializeValidator(updater, post);
        validatorBucket
                .consistOf(TargetIsDeletedValidator.of(comment.getIsDeleted(), StaticValue.DOMAIN_COMMENT))
                .consistOf(ConstraintValidator.of(comment, this.validator))
                .consistOf(ContentsAdminValidator.of(
                        updater.getRole(),
                        loginUserId,
                        comment.getWriter().getId(),
                        List.of()
                ));
        validatorBucket.validate();

        comment.update(commentUpdateRequestDto.getContent());

        return toCommentResponseDto(commentRepository.save(comment), updater, post.getBoard());
    }


    @Transactional
    public CommentResponseDto deleteComment(String loginUserId, String commentId) {
        User deleter = getUser(loginUserId);
        Comment comment = getComment(commentId);
        Post post = getPost(comment.getPost().getId());

        ValidatorBucket validatorBucket = initializeValidator(deleter, post);
        validatorBucket
                .consistOf(UserStateValidator.of(deleter.getState()))
                .consistOf(UserRoleIsNoneValidator.of(deleter.getRole()))
                .consistOf(TargetIsDeletedValidator.of(comment.getIsDeleted(), StaticValue.DOMAIN_COMMENT));

        Optional<Circle> circles = Optional.ofNullable(post.getBoard().getCircle());
        circles
                .filter(circle -> !deleter.getRole().equals(Role.ADMIN) && !deleter.getRole().getValue().contains("PRESIDENT"))
                .ifPresentOrElse(
                        circle -> {
                            CircleMember member = getCircleMember(deleter.getId(), circle.getId());

                            validatorBucket
                                    .consistOf(TargetIsDeletedValidator.of(circle.getIsDeleted(), StaticValue.DOMAIN_CIRCLE))
                                    .consistOf(CircleMemberStatusValidator.of(
                                            member.getStatus(),
                                            List.of(CircleMemberStatus.MEMBER)
                                    ))
                                    .consistOf(ContentsAdminValidator.of(
                                            deleter.getRole(),
                                            loginUserId,
                                            comment.getWriter().getId(),
                                            List.of(Role.LEADER_CIRCLE)
                                    ));

                            if (deleter.getRole().getValue().contains("LEADER_CIRCLE") && !comment.getWriter().getId().equals(loginUserId)) {
                                validatorBucket
                                        .consistOf(UserEqualValidator.of(
                                                circle.getLeader().map(User::getId).orElseThrow(
                                                        () -> new InternalServerException(
                                                                ErrorCode.ROW_DOES_NOT_EXIST,
                                                                MessageUtil.CIRCLE_WITHOUT_LEADER

                                                        )
                                                ),
                                                loginUserId
                                        ));
                            }
                        },
                        () -> validatorBucket
                                .consistOf(ContentsAdminValidator.of(
                                        deleter.getRole(),
                                        loginUserId,
                                        comment.getWriter().getId(),
                                        List.of()
                                ))
                );
        validatorBucket.validate();

        comment.delete();

        return toCommentResponseDto(commentRepository.save(comment), deleter, post.getBoard());
    }

    private CommentResponseDto toCommentResponseDto(Comment comment, User user, Board board) {
        return DtoMapper.INSTANCE.toCommentResponseDto(
                comment,
                childCommentRepository.countByParentComment_IdAndIsDeletedIsFalse(comment.getId()),
                comment.getChildCommentList().stream()
                        .map(childComment -> DtoMapper.INSTANCE.toChildCommentResponseDto(childComment, StatusUtil.isUpdatable(childComment, user), StatusUtil.isDeletable(childComment, user, board)))
                        .collect(Collectors.toList()),
                StatusUtil.isUpdatable(comment, user),
                StatusUtil.isDeletable(comment, user, board)
        );
    }

    private ValidatorBucket initializeValidator(User user, Post post) {
        ValidatorBucket validatorBucket = ValidatorBucket.of();
        validatorBucket
                .consistOf(UserStateValidator.of(user.getState()))
                .consistOf(UserRoleIsNoneValidator.of(user.getRole()))
                .consistOf(TargetIsDeletedValidator.of(post.getBoard().getIsDeleted(), StaticValue.DOMAIN_BOARD))
                .consistOf(TargetIsDeletedValidator.of(post.getIsDeleted(), StaticValue.DOMAIN_POST));

        Optional<Circle> circles = Optional.ofNullable(post.getBoard().getCircle());
        circles
                .filter(circle -> !user.getRole().equals(Role.ADMIN) && !user.getRole().getValue().contains("PRESIDENT"))
                .ifPresent(circle -> {
                    CircleMember member = getCircleMember(user.getId(), circle.getId());

                    validatorBucket
                            .consistOf(TargetIsDeletedValidator.of(circle.getIsDeleted(), StaticValue.DOMAIN_CIRCLE))
                            .consistOf(CircleMemberStatusValidator.of(
                                    member.getStatus(),
                                    List.of(CircleMemberStatus.MEMBER)
                            ));
                });
        return validatorBucket;
    }

    private User getUser(String userId) {
        return userRepository.findById(userId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.USER_NOT_FOUND
                )
        );
    }

    private Post getPost(String postId) {
        return postRepository.findById(postId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.POST_NOT_FOUND
                )
        );
    }

    private Comment getComment(String commentId) {
        return commentRepository.findById(commentId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.COMMENT_NOT_FOUND
                )
        );
    }

    private CircleMember getCircleMember(String userId, String circleId) {
        return circleMemberRepository.findByUser_IdAndCircle_Id(userId, circleId).orElseThrow(
                () -> new UnauthorizedException(
                        ErrorCode.NOT_MEMBER,
                        MessageUtil.CIRCLE_APPLY_INVALID
                )
        );
    }
}
