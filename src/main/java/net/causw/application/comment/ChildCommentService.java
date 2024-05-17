package net.causw.application.comment;

import lombok.RequiredArgsConstructor;
import net.causw.adapter.persistence.circle.Circle;
import net.causw.adapter.persistence.circle.CircleMember;
import net.causw.adapter.persistence.comment.ChildComment;
import net.causw.adapter.persistence.comment.Comment;
import net.causw.adapter.persistence.post.Post;
import net.causw.adapter.persistence.repository.*;
import net.causw.adapter.persistence.user.User;
import net.causw.application.dto.comment.ChildCommentCreateRequestDto;
import net.causw.application.dto.comment.ChildCommentResponseDto;
import net.causw.application.dto.comment.ChildCommentUpdateRequestDto;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.Validator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChildCommentService {

    private final ChildCommentRepository childCommentRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final CircleMemberRepository circleMemberRepository;
    private final PostRepository postRepository;
    private final Validator validator;

    @Transactional
    public ChildCommentResponseDto createChildComment(String creatorId, ChildCommentCreateRequestDto childCommentCreateRequestDto) {
        User user = getUser(creatorId);
        Comment parentComment = getComment(childCommentCreateRequestDto.getParentCommentId());
        Post post = getPost(parentComment.getPost().getId());
        Optional<ChildComment> refChildComment = childCommentCreateRequestDto.getRefChildComment().map(this::getChildComment);
        ChildComment childComment = ChildComment.of(
                childCommentCreateRequestDto.getContent(),
                false,
                refChildComment.map(refChild -> refChild.getWriter().getName()).orElse(null),
                childCommentCreateRequestDto.getRefChildComment().orElse(null),
                user,
                parentComment
        );

        ValidatorBucket validatorBucket = initializeValidator(user, post);
        validatorBucket.consistOf(ConstraintValidator.of(childComment, this.validator));
        refChildComment.ifPresent(
                refChild -> validatorBucket.consistOf(TargetIsDeletedValidator.of(refChild.getIsDeleted(), StaticValue.DOMAIN_CHILD_COMMENT))
        );
        validatorBucket.validate();

        return toChildCommentResponseDto(
                childCommentRepository.save(childComment),
                StatusUtil.isUpdatable(childComment, user),
                StatusUtil.isDeletable(childComment, user, post.getBoard())
        );
    }

    @Transactional
    public ChildCommentResponseDto updateChildComment(
            String updaterId,
            String childCommentId,
            ChildCommentUpdateRequestDto childCommentUpdateRequestDto
    ) {
        User updater = getUser(updaterId);
        ChildComment childComment = getChildComment(childCommentId);
        Post post = getPost(childComment.getParentComment().getPost().getId());
        childComment.update(childCommentUpdateRequestDto.getContent());

        ValidatorBucket validatorBucket = initializeValidator(updater, post);
        validatorBucket
                .consistOf(TargetIsDeletedValidator.of(childComment.getIsDeleted(), StaticValue.DOMAIN_CHILD_COMMENT))
                .consistOf(ConstraintValidator.of(childComment, this.validator))
                .consistOf(ContentsAdminValidator.of(
                        updater.getRole(),
                        updaterId,
                        childComment.getWriter().getId(),
                        List.of()
                ));
        validatorBucket.validate();

        return toChildCommentResponseDto(
                childCommentRepository.save(childComment),
                StatusUtil.isUpdatable(childComment, updater),
                StatusUtil.isDeletable(childComment, updater, post.getBoard())
        );
    }

    @Transactional
    public ChildCommentResponseDto deleteChildComment(String deleterId, String childCommentId) {
        User deleter = getUser(deleterId);
        ChildComment childComment = getChildComment(childCommentId);
        Post post = getPost(childComment.getParentComment().getPost().getId());

        ValidatorBucket validatorBucket = initializeValidator(deleter, post);
        validatorBucket.consistOf(TargetIsDeletedValidator.of(childComment.getIsDeleted(), StaticValue.DOMAIN_CHILD_COMMENT));
        Optional<Circle> circles = Optional.ofNullable(post.getBoard().getCircle());
        circles
                .filter(circle -> !deleter.getRole().equals(Role.ADMIN) && !deleter.getRole().getValue().contains("PRESIDENT"))
                .ifPresentOrElse(
                        circle -> {
                            CircleMember member = getCircleMember(deleterId, circle.getId());

                            validatorBucket
                                    .consistOf(TargetIsDeletedValidator.of(circle.getIsDeleted(), StaticValue.DOMAIN_CIRCLE))
                                    .consistOf(CircleMemberStatusValidator.of(
                                            member.getStatus(),
                                            List.of(CircleMemberStatus.MEMBER)
                                    ))
                                    .consistOf(ContentsAdminValidator.of(
                                            deleter.getRole(),
                                            deleterId,
                                            childComment.getWriter().getId(),
                                            List.of(Role.LEADER_CIRCLE)
                                    ));

                            if (deleter.getRole().getValue().contains("LEADER_CIRCLE") && !childComment.getWriter().getId().equals(deleterId)) {
                                validatorBucket
                                        .consistOf(UserEqualValidator.of(
                                                circle.getLeader().map(User::getId).orElseThrow(
                                                        () -> new InternalServerException(
                                                                ErrorCode.INTERNAL_SERVER,
                                                                MessageUtil.CIRCLE_WITHOUT_LEADER
                                                        )
                                                ),
                                                deleterId
                                        ));
                            }
                        },
                        () -> validatorBucket
                                .consistOf(ContentsAdminValidator.of(
                                        deleter.getRole(),
                                        deleterId,
                                        childComment.getWriter().getId(),
                                        List.of()
                                ))

                );
        validatorBucket.validate();

        childComment.delete();

        return toChildCommentResponseDto(
                childCommentRepository.save(childComment),
                StatusUtil.isUpdatable(childComment, deleter),
                StatusUtil.isDeletable(childComment, deleter, post.getBoard())
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

    private ChildCommentResponseDto toChildCommentResponseDto(ChildComment comment, Boolean updatable, Boolean deletable) {
        return DtoMapper.INSTANCE.toChildCommentResponseDto(comment, updatable, deletable);
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

    private ChildComment getChildComment(String childCommentId) {
        return childCommentRepository.findById(childCommentId).orElseThrow(
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