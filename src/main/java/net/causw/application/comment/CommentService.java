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
import net.causw.application.util.ServiceProxy;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.exceptions.InternalServerException;
import net.causw.domain.model.enums.CircleMemberStatus;
import net.causw.domain.model.enums.Role;
import net.causw.domain.model.util.MessageUtil;
import net.causw.domain.model.util.StaticValue;
import net.causw.domain.validation.ConstraintValidator;
import net.causw.domain.validation.ContentsAdminValidator;
import net.causw.domain.validation.TargetIsDeletedValidator;
import net.causw.domain.validation.UserEqualValidator;
import net.causw.domain.validation.ValidatorBucket;
import net.causw.domain.validation.valid.CircleMemberValid;
import net.causw.domain.validation.valid.UserValid;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.validation.Validator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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
    private final ServiceProxy serviceProxy;

    @Transactional
    public CommentResponseDto createComment(User creator, CommentCreateRequestDto commentCreateDto) {
        Post post = getPost(commentCreateDto.getPostId());
        Comment comment = Comment.of(commentCreateDto.getContent(), false, creator, post);
        initializeValidator(creator, post);

        ValidatorBucket validatorBucket = ValidatorBucket.of();
        validatorBucket.
                consistOf(ConstraintValidator.of(comment, this.validator));
        validatorBucket.validate();

        return toCommentResponseDto(commentRepository.save(comment), creator, post.getBoard());
    }

    @Transactional(readOnly = true)
    public Page<CommentResponseDto> findAllComments(User user, String postId, Integer pageNum) {
        Post post = getPost(postId);
        initializeValidator(user, post);

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
        initializeValidator(updater, post);

        ValidatorBucket validatorBucket = ValidatorBucket.of();
        validatorBucket
                .consistOf(ConstraintValidator.of(comment, this.validator))
                .consistOf(ContentsAdminValidator.of(
                        roles,
                        updater.getId(),
                        comment.getWriter().getId(),
                        List.of()
                ));
        validatorBucket.validate();
        new TargetIsDeletedValidator().validate(comment.getIsDeleted(), StaticValue.DOMAIN_COMMENT);

        comment.update(commentUpdateRequestDto.getContent());

        return toCommentResponseDto(commentRepository.save(comment), updater, post.getBoard());
    }


    @Transactional
    public CommentResponseDto deleteComment(@UserValid User deleter, String commentId) {
        Set<Role> roles = deleter.getRoles();
        Comment comment = getComment(commentId);
        Post post = getPost(comment.getPost().getId());

        initializeValidator(deleter, post);
        new TargetIsDeletedValidator().validate(comment.getIsDeleted(), StaticValue.DOMAIN_COMMENT);

        ValidatorBucket validatorBucket = ValidatorBucket.of();
        Optional<Circle> circles = Optional.ofNullable(post.getBoard().getCircle());
        circles
                .filter(circle -> !roles.contains(Role.ADMIN) && !roles.contains(Role.PRESIDENT) && !roles.contains(Role.VICE_PRESIDENT))
                .ifPresentOrElse(
                        circle -> {
                            CircleMember member = serviceProxy.getCircleMemberComment(deleter.getId(), circle.getId(), List.of(CircleMemberStatus.MEMBER));
                            validatorBucket
                                    .consistOf(ContentsAdminValidator.of(
                                            roles,
                                            deleter.getId(),
                                            comment.getWriter().getId(),
                                            List.of(Role.LEADER_CIRCLE)
                                    ));
                            new TargetIsDeletedValidator().validate(circle.getIsDeleted(), StaticValue.DOMAIN_CIRCLE);

                            if (roles.contains(Role.LEADER_CIRCLE) && !comment.getWriter().getId().equals(deleter.getId())) {
                                new UserEqualValidator().validate(
                                        deleter.getId(),
                                        circle.getLeader().map(User::getId).orElseThrow(
                                                () -> new InternalServerException(
                                                        ErrorCode.ROW_DOES_NOT_EXIST,
                                                        MessageUtil.CIRCLE_WITHOUT_LEADER

                                                )
                                        )
                                );
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

    private void initializeValidator(@UserValid User user, Post post) {
        Set<Role> roles = user.getRoles();
        new TargetIsDeletedValidator().validate(post.getIsDeleted(), StaticValue.DOMAIN_POST);
        new TargetIsDeletedValidator().validate(post.getBoard().getIsDeleted(), StaticValue.DOMAIN_BOARD);

        Optional<Circle> circles = Optional.ofNullable(post.getBoard().getCircle());
        circles
                .filter(circle -> !roles.contains(Role.ADMIN) && !roles.contains(Role.PRESIDENT) && !roles.contains(Role.VICE_PRESIDENT))
                .ifPresent(circle -> {
                    CircleMember member = serviceProxy.getCircleMemberComment(user.getId(), circle.getId(), List.of(CircleMemberStatus.MEMBER));
                    new TargetIsDeletedValidator().validate(circle.getIsDeleted(), StaticValue.DOMAIN_CIRCLE);
                });
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

    @CircleMemberValid(CircleMemberStatusValidator = true)
    public CircleMember getCircleMember(String userId, String circleId, List<CircleMemberStatus> list) {
        return circleMemberRepository.findByUser_IdAndCircle_Id(userId, circleId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.CIRCLE_APPLY_INVALID
                )
        );
    }
}
