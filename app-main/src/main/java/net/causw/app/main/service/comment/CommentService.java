package net.causw.app.main.service.comment;

import lombok.RequiredArgsConstructor;
import net.causw.app.main.domain.model.entity.board.Board;
import net.causw.app.main.domain.model.entity.circle.Circle;
import net.causw.app.main.domain.model.entity.circle.CircleMember;
import net.causw.app.main.domain.model.entity.comment.ChildComment;
import net.causw.app.main.domain.model.entity.comment.Comment;
import net.causw.app.main.domain.model.entity.comment.LikeComment;
import net.causw.app.main.domain.model.entity.notification.UserCommentSubscribe;
import net.causw.app.main.domain.model.enums.user.UserState;
import net.causw.app.main.repository.notification.UserCommentSubscribeRepository;
import net.causw.app.main.dto.comment.*;
import net.causw.app.main.service.notification.PostNotificationService;
import net.causw.app.main.service.pageable.PageableFactory;
import net.causw.app.main.domain.model.entity.post.Post;
import net.causw.app.main.repository.circle.CircleMemberRepository;
import net.causw.app.main.repository.comment.ChildCommentRepository;
import net.causw.app.main.repository.comment.CommentRepository;
import net.causw.app.main.repository.comment.LikeChildCommentRepository;
import net.causw.app.main.repository.comment.LikeCommentRepository;
import net.causw.app.main.repository.post.PostRepository;
import net.causw.app.main.repository.user.UserRepository;
import net.causw.app.main.domain.model.entity.user.User;
import net.causw.app.main.dto.util.dtoMapper.CommentDtoMapper;
import net.causw.app.main.domain.policy.StatusPolicy;
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
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.validation.Validator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
@MeasureTime
@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CircleMemberRepository circleMemberRepository;
    private final ChildCommentRepository childCommentRepository;
    private final LikeCommentRepository likeCommentRepository;
    private final LikeChildCommentRepository likeChildCommentRepository;
    private final PageableFactory pageableFactory;
    private final Validator validator;
    private final UserCommentSubscribeRepository userCommentSubscribeRepository;
    private final PostNotificationService postNotificationService;
    private final PostService postService;

    @Transactional
    public CommentResponseDto createComment(User creator, CommentCreateRequestDto commentCreateDto) {
        Post post = getPost(commentCreateDto.getPostId());
        Comment comment = Comment.of(commentCreateDto.getContent(), false, commentCreateDto.getIsAnonymous(), creator, post);



        ValidatorBucket validatorBucket = initializeValidator(creator, post);
        validatorBucket.
                consistOf(ConstraintValidator.of(comment, this.validator));
        validatorBucket.validate();
        //1. comment의 구독 여부 저장
        CommentResponseDto commentResponseDto = toCommentResponseDto(commentRepository.save(comment), creator, post.getBoard());

        createCommentSubscribe(creator, comment.getId());

        //2. comment가 달린 게시글의 구독자에게 전송
        postNotificationService.sendByPostIsSubscribed(post, comment);


        return commentResponseDto;
    }

    @Transactional(readOnly = true)
    public Page<CommentResponseDto> findAllComments(User user, String postId, Integer pageNum) {
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
            User updater,
            String commentId,
            CommentUpdateRequestDto commentUpdateRequestDto
    ) {
        Set<Role> roles = updater.getRoles();
        Comment comment = getComment(commentId);
        Post post = getPost(comment.getPost().getId());

        ValidatorBucket validatorBucket = initializeValidator(updater, post);
        validatorBucket
                .consistOf(TargetIsDeletedValidator.of(comment.getIsDeleted(), StaticValue.DOMAIN_COMMENT))
                .consistOf(ConstraintValidator.of(comment, this.validator))
                .consistOf(ContentsAdminValidator.of(
                        roles,
                        updater.getId(),
                        comment.getWriter().getId(),
                        List.of()
                ));
        validatorBucket.validate();

        comment.update(commentUpdateRequestDto.getContent());

        return toCommentResponseDto(commentRepository.save(comment), updater, post.getBoard());
    }


    @Transactional
    public CommentResponseDto deleteComment(User deleter, String commentId) {
        Set<Role> roles = deleter.getRoles();
        Comment comment = getComment(commentId);
        Post post = getPost(comment.getPost().getId());

        ValidatorBucket validatorBucket = initializeValidator(deleter, post);
        validatorBucket
                .consistOf(UserStateValidator.of(deleter.getState()))
                .consistOf(UserRoleIsNoneValidator.of(roles))
                .consistOf(TargetIsDeletedValidator.of(comment.getIsDeleted(), StaticValue.DOMAIN_COMMENT));

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
                                            comment.getWriter().getId(),
                                            List.of(Role.LEADER_CIRCLE)
                                    ));

                            if (roles.contains(Role.LEADER_CIRCLE) && !comment.getWriter().getId().equals(deleter.getId())) {
                                validatorBucket
                                        .consistOf(UserEqualValidator.of(
                                                circle.getLeader().map(User::getId).orElseThrow(
                                                        () -> new InternalServerException(
                                                                ErrorCode.ROW_DOES_NOT_EXIST,
                                                                MessageUtil.CIRCLE_WITHOUT_LEADER

                                                        )
                                                ),
                                                deleter.getId()
                                        ));
                            }
                        },
                        () -> validatorBucket
                                .consistOf(ContentsAdminValidator.of(
                                        roles,
                                        deleter.getId(),
                                        comment.getWriter().getId(),
                                        List.of()
                                ))
                );
        validatorBucket.validate();

        comment.delete();

        return toCommentResponseDto(commentRepository.save(comment), deleter, post.getBoard());
    }

    @Transactional
    public void likeComment(User user, String commentId) {
        Comment comment = getComment(commentId);

        ValidatorBucket validatorBucket = ValidatorBucket.of();
        validatorBucket
                .consistOf(UserStateIsDeletedValidator.of(comment.getWriter().getState()))
                .validate();

        if (isCommentAlreadyLike(user, commentId)) {
            throw new BadRequestException(ErrorCode.ROW_ALREADY_EXIST, MessageUtil.COMMENT_ALREADY_LIKED);
        }

        LikeComment likeComment = LikeComment.of(comment, user);
        likeCommentRepository.save(likeComment);
    }


    public void createCommentSubscribe(User user, String commentId){
        Comment comment = getComment(commentId);

        UserCommentSubscribe userCommentSubscribe = UserCommentSubscribe.of(user, comment, true);
        userCommentSubscribeRepository.save(userCommentSubscribe);
    }

    @Transactional
    public CommentSubscribeResponseDto setCommentSubscribe(User user, String commentId, Boolean isSubscribed) {
        Comment comment = getComment(commentId);

        UserCommentSubscribe subscription = userCommentSubscribeRepository.findByUserAndComment(user, comment)
                .map(existing -> {
                    existing.setIsSubscribed(isSubscribed);
                    return existing;
                })
                .orElseGet(() -> userCommentSubscribeRepository.save(UserCommentSubscribe.of(user, comment, isSubscribed)));

        return CommentDtoMapper.INSTANCE.toCommentSubscribeResponseDto(subscription);
    }


    private Boolean isCommentAlreadyLike(User user, String commentId) {
        return likeCommentRepository.existsByCommentIdAndUserId(commentId, user.getId());
    }

    private Boolean isChildCommentAlreadyLike(User user, String childCommentId) {
        return likeChildCommentRepository.existsByChildCommentIdAndUserId(childCommentId, user.getId());
    }

    private CommentResponseDto toCommentResponseDto(Comment comment, User user, Board board) {
        CommentResponseDto commentResponseDto = CommentDtoMapper.INSTANCE.toCommentResponseDto(
                comment,
                childCommentRepository.countByParentComment_IdAndIsDeletedIsFalse(comment.getId()),
                getNumOfCommentLikes(comment),
                isCommentAlreadyLike(user, comment.getId()),
                StatusPolicy.isCommentOwner(comment, user),
                comment.getChildCommentList().stream()
                        .map(childComment -> toChildCommentResponseDto(childComment, user, board))
                        .collect(Collectors.toList()),
                StatusPolicy.isUpdatable(comment, user),
                StatusPolicy.isDeletable(comment, user, board),
                isCommentSubscribed(user, comment)
        );

        // 화면에 표시될 닉네임 설정
        User writer = comment.getWriter();
        commentResponseDto.setDisplayWriterNickname(postService.getDisplayWriterNickname(writer, commentResponseDto.getIsAnonymous(), commentResponseDto.getWriterNickname()));

        return commentResponseDto;
    }

    private ChildCommentResponseDto toChildCommentResponseDto(ChildComment childComment, User user, Board board) {
        ChildCommentResponseDto childCommentResponseDto = CommentDtoMapper.INSTANCE.toChildCommentResponseDto(
                childComment,
                getNumOfChildCommentLikes(childComment),
                isChildCommentAlreadyLike(user, childComment.getId()),
                StatusPolicy.isChildCommentOwner(childComment, user),
                StatusPolicy.isUpdatable(childComment, user),
                StatusPolicy.isDeletable(childComment, user, board)
        );

        // 화면에 표시될 닉네임 설정
        User writer = childComment.getWriter();
        childCommentResponseDto.setDisplayWriterNickname(postService.getDisplayWriterNickname(writer, childCommentResponseDto.getIsAnonymous(), childCommentResponseDto.getWriterNickname()));

        return childCommentResponseDto;
    }

    private Long getNumOfCommentLikes(Comment comment){
        return likeCommentRepository.countByCommentId(comment.getId());
    }

    private Long getNumOfChildCommentLikes(ChildComment childComment) {
        return likeChildCommentRepository.countByChildCommentId(childComment.getId());
    }

    private ValidatorBucket initializeValidator(User user, Post post) {
        ValidatorBucket validatorBucket = ValidatorBucket.of();
        Set<Role> roles = user.getRoles();
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

    private Boolean isCommentSubscribed(User user, Comment comment){
        return userCommentSubscribeRepository.findByUserAndComment(user, comment)
                .map(UserCommentSubscribe::getIsSubscribed)
                .orElse(false);
    }

}
