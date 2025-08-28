package net.causw.app.main.service.comment;

import lombok.RequiredArgsConstructor;
import net.causw.app.main.domain.model.entity.board.Board;
import net.causw.app.main.domain.model.entity.circle.Circle;
import net.causw.app.main.domain.model.entity.circle.CircleMember;
import net.causw.app.main.domain.model.entity.comment.ChildComment;
import net.causw.app.main.domain.model.entity.comment.Comment;
import net.causw.app.main.domain.model.entity.comment.LikeChildComment;
import net.causw.app.main.domain.model.entity.post.Post;
import net.causw.app.main.repository.circle.CircleMemberRepository;
import net.causw.app.main.repository.comment.ChildCommentRepository;
import net.causw.app.main.repository.comment.CommentRepository;
import net.causw.app.main.repository.comment.LikeChildCommentRepository;
import net.causw.app.main.repository.notification.NotificationRepository;
import net.causw.app.main.repository.post.PostRepository;
import net.causw.app.main.repository.user.UserRepository;
import net.causw.app.main.domain.model.entity.user.User;
import net.causw.app.main.dto.comment.ChildCommentCreateRequestDto;
import net.causw.app.main.dto.comment.ChildCommentResponseDto;
import net.causw.app.main.dto.comment.ChildCommentUpdateRequestDto;
import net.causw.app.main.dto.util.dtoMapper.CommentDtoMapper;
import net.causw.app.main.domain.policy.StatusPolicy;
import net.causw.app.main.service.notification.CommentNotificationService;
import net.causw.app.main.infrastructure.aop.annotation.MeasureTime;
import net.causw.app.main.service.post.PostService;
import net.causw.global.exception.BadRequestException;
import net.causw.global.exception.ErrorCode;
import net.causw.global.exception.InternalServerException;
import net.causw.global.exception.UnauthorizedException;
import net.causw.app.main.domain.model.enums.circle.CircleMemberStatus;
import net.causw.app.main.domain.model.enums.user.Role;
import net.causw.global.constant.MessageUtil;
import net.causw.global.constant.StaticValue;
import net.causw.app.main.domain.validation.*;
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
    private final CommentNotificationService commentNotificationService;
    private final PostService postService;

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

        ChildCommentResponseDto childCommentResponseDto = toChildCommentResponseDto(
                childCommentRepository.save(childComment),
                creator,
                post.getBoard()
        );

        //1. 여기선 그냥 댓글 달리면 알람을 보내게 하면됨
        commentNotificationService.sendByCommentIsSubscribed(parentComment, childComment);

        return childCommentResponseDto;
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

        validateWriterNotDeleted(childComment);

        if (isChildCommentLiked(user, childCommentId)) {
            throw new BadRequestException(ErrorCode.ROW_ALREADY_EXIST, MessageUtil.CHILD_COMMENT_ALREADY_LIKED);
        }

        LikeChildComment likeChildComment = LikeChildComment.of(childComment, user);
        likeChildCommentRepository.save(likeChildComment);
    }

    @Transactional
    public void cancelLikeChildComment(final User user, final String childCommentId) {
        ChildComment childComment = getChildComment(childCommentId);

        this.validateWriterNotDeleted(childComment);

        if (!isChildCommentLiked(user, childCommentId)) {
            throw new BadRequestException(ErrorCode.ROW_DOES_NOT_EXIST, MessageUtil.CHILD_COMMENT_NOT_LIKED);
        }

        likeChildCommentRepository.deleteLikeByChildCommentIdAndUserId(childCommentId, user.getId());
    }



    private Boolean isChildCommentLiked(User user, String childCommentId) {
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
        ChildCommentResponseDto childCommentResponseDto = CommentDtoMapper.INSTANCE.toChildCommentResponseDto(
            childComment,
            getNumOfChildCommentLikes(childComment),
            isChildCommentLiked(user, childComment.getId()),
            StatusPolicy.isChildCommentOwner(childComment, user),
            StatusPolicy.isUpdatable(childComment, user),
            StatusPolicy.isDeletable(childComment, user, board),
            false
        );

        // 화면에 표시될 닉네임 설정
        User writer = childComment.getWriter();
        childCommentResponseDto.setDisplayWriterNickname(
            postService.getDisplayWriterNickname(writer, childCommentResponseDto.getIsAnonymous(),
                childCommentResponseDto.getWriterNickname()));

        if (childCommentResponseDto.getIsAnonymous()) {
            childCommentResponseDto.updateAnonymousUserInfo();
        }

        return childCommentResponseDto;
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

    private void validateWriterNotDeleted(final ChildComment childComment) {
        ValidatorBucket validatorBucket = ValidatorBucket.of();
        validatorBucket
                .consistOf(UserStateIsDeletedValidator.of(childComment.getWriter().getState()))
                .validate();
    }
}

