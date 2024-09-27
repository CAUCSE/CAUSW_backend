package net.causw.application.comment;

import lombok.RequiredArgsConstructor;
import net.causw.adapter.persistence.board.Board;
import net.causw.adapter.persistence.circle.Circle;
import net.causw.adapter.persistence.circle.CircleMember;
import net.causw.adapter.persistence.comment.ChildComment;
import net.causw.adapter.persistence.comment.Comment;
import net.causw.adapter.persistence.comment.LikeChildComment;
import net.causw.adapter.persistence.notification.Notification;
import net.causw.adapter.persistence.post.Post;
import net.causw.adapter.persistence.repository.circle.CircleMemberRepository;
import net.causw.adapter.persistence.repository.comment.ChildCommentRepository;
import net.causw.adapter.persistence.repository.comment.CommentRepository;
import net.causw.adapter.persistence.repository.comment.LikeChildCommentRepository;
import net.causw.adapter.persistence.repository.notification.NotificationRepository;
import net.causw.adapter.persistence.repository.post.PostRepository;
import net.causw.adapter.persistence.repository.user.UserRepository;
import net.causw.adapter.persistence.user.User;
import net.causw.application.dto.comment.ChildCommentCreateRequestDto;
import net.causw.application.dto.comment.ChildCommentResponseDto;
import net.causw.application.dto.comment.ChildCommentUpdateRequestDto;
import net.causw.application.dto.util.dtoMapper.CommentDtoMapper;
import net.causw.application.dto.util.StatusUtil;
import net.causw.domain.aop.annotation.MeasureTime;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.exceptions.InternalServerException;
import net.causw.domain.exceptions.UnauthorizedException;
import net.causw.domain.model.enums.CircleMemberStatus;
import net.causw.domain.model.enums.NoticeType;
import net.causw.domain.model.enums.Role;
import net.causw.domain.model.util.MessageUtil;
import net.causw.domain.model.util.StaticValue;
import net.causw.domain.validation.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.validation.Validator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@MeasureTime
@Service
@RequiredArgsConstructor
public class ChildCommentService {

    private final ChildCommentRepository childCommentRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final CircleMemberRepository circleMemberRepository;
    private final PostRepository postRepository;
    private final LikeChildCommentRepository likeChildCommentRepository;
    private final NotificationRepository notificationRepository;
    private final Validator validator;

    @Transactional
    public ChildCommentResponseDto createChildComment(User creator, ChildCommentCreateRequestDto childCommentCreateRequestDto) {
        Comment parentComment = getComment(childCommentCreateRequestDto.getParentCommentId());
        Post post = getPost(parentComment.getPost().getId());
        ChildComment childComment = ChildComment.of(
                childCommentCreateRequestDto.getContent(),
                false,
                childCommentCreateRequestDto.getIsAnonymous(),
                creator,
                parentComment
        );

        ValidatorBucket validatorBucket = initializeValidator(creator, post);
        validatorBucket
                .consistOf(ConstraintValidator.of(childComment, this.validator))
                .consistOf(UserStateIsDeletedValidator.of(parentComment.getWriter().getState()));
        validatorBucket.validate();

        if (!creator.getId().equals(childComment.getWriter().getId())) {
            notificationRepository.save(
                    Notification.of(
                            parentComment.getWriter(),
                            childComment.getContent(),
                            NoticeType.COMMENT,
                            false
                    )
            );
        }

        return toChildCommentResponseDto(
                childCommentRepository.save(childComment),
                creator,
                post.getBoard()
        );
    }

    @Transactional
    public ChildCommentResponseDto updateChildComment(
            User updater,
            String childCommentId,
            ChildCommentUpdateRequestDto childCommentUpdateRequestDto
    ) {
        Set<Role> roles = updater.getRoles();
        ChildComment childComment = getChildComment(childCommentId);
        Post post = getPost(childComment.getParentComment().getPost().getId());
        childComment.update(childCommentUpdateRequestDto.getContent());

        ValidatorBucket validatorBucket = initializeValidator(updater, post);
        validatorBucket
                .consistOf(TargetIsDeletedValidator.of(childComment.getIsDeleted(), StaticValue.DOMAIN_CHILD_COMMENT))
                .consistOf(ConstraintValidator.of(childComment, this.validator))
                .consistOf(ContentsAdminValidator.of(
                        roles,
                        updater.getId(),
                        childComment.getWriter().getId(),
                        List.of()
                ));
        validatorBucket.validate();

        return toChildCommentResponseDto(
                childCommentRepository.save(childComment),
                updater,
                post.getBoard()
        );
    }

    @Transactional
    public ChildCommentResponseDto deleteChildComment(User deleter, String childCommentId) {
        Set<Role> roles = deleter.getRoles();
        ChildComment childComment = getChildComment(childCommentId);
        Post post = getPost(childComment.getParentComment().getPost().getId());

        ValidatorBucket validatorBucket = initializeValidator(deleter, post);
        validatorBucket.consistOf(TargetIsDeletedValidator.of(childComment.getIsDeleted(), StaticValue.DOMAIN_CHILD_COMMENT));
        Optional<Circle> circles = Optional.ofNullable(post.getBoard().getCircle());
        circles
                .filter(circle -> !roles.contains(Role.ADMIN) && !roles.contains(Role.PRESIDENT) && !roles.contains(Role.VICE_PRESIDENT))
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
                                            roles,
                                            deleter.getId(),
                                            childComment.getWriter().getId(),
                                            List.of(Role.LEADER_CIRCLE)
                                    ));

                            if (roles.contains(Role.LEADER_CIRCLE) && !childComment.getWriter().getId().equals(deleter.getId())) {
                                User leader = circle.getLeader().orElse(null);
                                if (leader == null) {
                                    throw new InternalServerException(
                                            ErrorCode.INTERNAL_SERVER,
                                            MessageUtil.CIRCLE_WITHOUT_LEADER
                                    );
                                }
                                validatorBucket
                                        .consistOf(UserEqualValidator.of(
                                                leader.getId(),
                                                deleter.getId()
                                        ));
                            }
                        },
                        () -> validatorBucket
                                .consistOf(ContentsAdminValidator.of(
                                        roles,
                                        deleter.getId(),
                                        childComment.getWriter().getId(),
                                        List.of()
                                ))

                );
        validatorBucket.validate();

        childComment.delete();

        return toChildCommentResponseDto(
                childCommentRepository.save(childComment),
                deleter,
                post.getBoard()
        );
    }

    @Transactional
    public void likeChildComment(User user, String childCommentId) {
        ChildComment childComment = getChildComment(childCommentId);

        ValidatorBucket validatorBucket = ValidatorBucket.of();
        validatorBucket
                .consistOf(UserStateIsDeletedValidator.of(childComment.getWriter().getState()))
                .validate();

        if (isChildCommentAlreadyLike(user, childCommentId)) {
            throw new BadRequestException(ErrorCode.ROW_ALREADY_EXIST, MessageUtil.CHILD_COMMENT_ALREADY_LIKED);
        }

        LikeChildComment likeChildComment = LikeChildComment.of(childComment, user);
        likeChildCommentRepository.save(likeChildComment);
    }

    private Boolean isChildCommentAlreadyLike(User user, String childCommentId) {
        return likeChildCommentRepository.existsByChildCommentIdAndUserId(childCommentId, user.getId());
    }

    private Long getNumOfChildCommentLikes(ChildComment childComment) {
        return likeChildCommentRepository.countByChildCommentId(childComment.getId());
    }

    private ValidatorBucket initializeValidator(User user, Post post) {
        Set<Role> roles = user.getRoles();
        ValidatorBucket validatorBucket = ValidatorBucket.of();
        validatorBucket
                .consistOf(UserStateValidator.of(user.getState()))
                .consistOf(UserRoleIsNoneValidator.of(roles))
                .consistOf(TargetIsDeletedValidator.of(post.getBoard().getIsDeleted(), StaticValue.DOMAIN_BOARD))
                .consistOf(TargetIsDeletedValidator.of(post.getIsDeleted(), StaticValue.DOMAIN_POST));

        Optional<Circle> circles = Optional.ofNullable(post.getBoard().getCircle());
        circles
                .filter(circle -> !roles.contains(Role.ADMIN) && !roles.contains(Role.PRESIDENT) && !roles.contains(Role.VICE_PRESIDENT))
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

    private ChildCommentResponseDto  toChildCommentResponseDto(ChildComment childComment, User user, Board board) {
        return CommentDtoMapper.INSTANCE.toChildCommentResponseDto(
                childComment,
                getNumOfChildCommentLikes(childComment),
                isChildCommentAlreadyLike(user, childComment.getId()),
                StatusUtil.isUpdatable(childComment, user),
                StatusUtil.isDeletable(childComment, user, board)
        );
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
