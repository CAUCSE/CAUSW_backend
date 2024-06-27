package net.causw.application.post;

import lombok.RequiredArgsConstructor;
import net.causw.adapter.persistence.board.Board;
import net.causw.adapter.persistence.circle.Circle;
import net.causw.adapter.persistence.circle.CircleMember;
import net.causw.adapter.persistence.page.PageableFactory;
import net.causw.adapter.persistence.post.Post;
import net.causw.adapter.persistence.repository.*;
import net.causw.adapter.persistence.user.User;
import net.causw.application.dto.comment.CommentResponseDto;
import net.causw.application.dto.post.*;
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
import net.causw.domain.validation.PostNumberOfAttachmentsValidator;
import net.causw.domain.validation.TargetIsDeletedValidator;
import net.causw.domain.validation.UserEqualValidator;
import net.causw.domain.validation.UserRoleIsNoneValidator;
import net.causw.domain.validation.UserRoleValidator;
import net.causw.domain.validation.UserStateValidator;
import net.causw.domain.validation.ValidatorBucket;
import net.causw.domain.validation.TargetIsNotDeletedValidator;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.Validator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final BoardRepository boardRepository;
    private final CircleMemberRepository circleMemberRepository;
    private final CommentRepository commentRepository;
    private final ChildCommentRepository childCommentRepository;
    private final FavoriteBoardRepository favoriteBoardRepository;
    private final PageableFactory pageableFactory;
    private final Validator validator;

    @Transactional(readOnly = true)
    public PostResponseDto findPostById(String loginUserId, String postId) {
        User user = getUser(loginUserId);
        Post post = getPost(postId);

        ValidatorBucket validatorBucket = initializeValidator(user, post.getBoard());
        validatorBucket.validate();

        return toPostResponseDtoExtended(post, user);
    }

    @Transactional(readOnly = true)
    public BoardPostsResponseDto findAllPost(
            String loginUserId,
            String boardId,
            Integer pageNum
    ) {
        User user = getUser(loginUserId);
        Board board = getBoard(boardId);

        ValidatorBucket validatorBucket = initializeValidator(user, board);
        validatorBucket.validate();

        boolean isCircleLeader = false;
        if (user.getRole().getValue().contains("LEADER_CIRCLE")) {
            isCircleLeader = getCircleLeader(board.getCircle()).getId().equals(loginUserId);
        }

        if (isCircleLeader || user.getRole().equals(Role.ADMIN) || user.getRole().getValue().contains("PRESIDENT")) {
            return toBoardPostsResponseDto(
                    board,
                    user.getRole(),
                    isFavorite(loginUserId, board.getId()),
                    postRepository.findAllByBoard_IdOrderByCreatedAtDesc(boardId, pageableFactory.create(pageNum, StaticValue.DEFAULT_POST_PAGE_SIZE))
                            .map(this::toPostsResponseDto)
            );
        } else {
            return toBoardPostsResponseDto(
                    board,
                    user.getRole(),
                    isFavorite(loginUserId, board.getId()),
                    postRepository.findAllByBoard_IdAndIsDeletedOrderByCreatedAtDesc(boardId, pageableFactory.create(pageNum, StaticValue.DEFAULT_POST_PAGE_SIZE), false)
                            .map(this::toPostsResponseDto)
            );
        }
    }

    @Transactional(readOnly = true)
    public BoardPostsResponseDto searchPost(
            String loginUserId,
            String boardId,
            String keyword,
            Integer pageNum
    ) {
        User user = getUser(loginUserId);
        Board board = getBoard(boardId);

        ValidatorBucket validatorBucket = initializeValidator(user, board);
        validatorBucket
                .consistOf(TargetIsDeletedValidator.of(board.getIsDeleted(), StaticValue.DOMAIN_BOARD))
                .validate();

        boolean isCircleLeader = false;
        if (user.getRole().getValue().contains("LEADER_CIRCLE")) {
            isCircleLeader = getCircleLeader(board.getCircle()).getId().equals(loginUserId);
        }

        if (isCircleLeader || user.getRole().equals(Role.ADMIN) || user.getRole().getValue().contains("PRESIDENT")) {
            return toBoardPostsResponseDto(
                    board,
                    user.getRole(),
                    isFavorite(loginUserId, board.getId()),
                    postRepository.findAllByBoard_IdOrderByCreatedAtDesc(boardId, pageableFactory.create(pageNum, StaticValue.DEFAULT_POST_PAGE_SIZE))
                            .map(this::toPostsResponseDto));
        } else {
            return toBoardPostsResponseDto(
                    board,
                    user.getRole(),
                    isFavorite(loginUserId, board.getId()),
                    postRepository.searchByTitle(keyword, boardId, pageableFactory.create(pageNum, StaticValue.DEFAULT_POST_PAGE_SIZE), false)
                            .map(this::toPostsResponseDto));
        }
    }

    @Transactional(readOnly = true)
    public BoardPostsResponseDto findAllAppNotice(String loginUserId, Integer pageNum) {
        User user = getUser(loginUserId);
        Board board = boardRepository.findAppNotice().orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.BOARD_NOT_FOUND
                )
        );

        return toBoardPostsResponseDto(
                board,
                user.getRole(),
                isFavorite(loginUserId, board.getId()),
                postRepository.findAllByBoard_IdOrderByCreatedAtDesc(board.getId(), pageableFactory.create(pageNum, StaticValue.DEFAULT_POST_PAGE_SIZE))
                        .map(this::toPostsResponseDto));
    }

    @Transactional
    public PostResponseDto createPost(String loginUserId, PostCreateRequestDto postCreateRequestDto) {
        ValidatorBucket validatorBucket = ValidatorBucket.of();

        User creator = getUser(loginUserId);
        Board board = getBoard(postCreateRequestDto.getBoardId());
        List<String> createRoles = new ArrayList<>(Arrays.asList(board.getCreateRoles().split(",")));
        if (board.getCategory().equals(StaticValue.BOARD_NAME_APP_NOTICE)) {
            validatorBucket
                    .consistOf(UserRoleValidator.of(
                            creator.getRole(),
                            List.of()
                    ));
        }

        Post post = Post.of(
                postCreateRequestDto.getTitle(),
                postCreateRequestDto.getContent(),
                creator,
                false,
                board,
                String.join(":::", postCreateRequestDto.getAttachmentList())
        );

        validatorBucket
                .consistOf(UserStateValidator.of(creator.getState()))
                .consistOf(UserRoleIsNoneValidator.of(creator.getRole()))
                .consistOf(PostNumberOfAttachmentsValidator.of(postCreateRequestDto.getAttachmentList()))
                .consistOf(TargetIsDeletedValidator.of(board.getIsDeleted(), StaticValue.DOMAIN_BOARD))
                .consistOf(UserRoleValidator.of(
                        creator.getRole(),
                        createRoles.stream()
                                .map(Role::of)
                                .collect(Collectors.toList())
                ));

        Optional<Circle> circles = Optional.ofNullable(board.getCircle());
        circles
                .filter(circle -> !creator.getRole().equals(Role.ADMIN) && !creator.getRole().getValue().contains("PRESIDENT"))
                .ifPresent(
                        circle -> {
                            CircleMember member = getCircleMember(creator.getId(), circle.getId());

                            validatorBucket
                                    .consistOf(TargetIsDeletedValidator.of(circle.getIsDeleted(), StaticValue.DOMAIN_CIRCLE))
                                    .consistOf(CircleMemberStatusValidator.of(
                                            member.getStatus(),
                                            List.of(CircleMemberStatus.MEMBER)
                                    ));

                            if (creator.getRole().getValue().contains("LEADER_CIRCLE") && !createRoles.contains("COMMON")) {
                                validatorBucket
                                        .consistOf(UserEqualValidator.of(
                                                getCircleLeader(circle).getId(),
                                                loginUserId
                                        ));
                            }
                        }
                );
        validatorBucket
                .consistOf(ConstraintValidator.of(post, this.validator))
                .validate();

        return toPostResponseDto(postRepository.save(post), creator);
    }

    @Transactional
    public PostResponseDto deletePost(String loginUserId, String postId) {
        User deleter = getUser(loginUserId);
        Post post = getPost(postId);

        ValidatorBucket validatorBucket = ValidatorBucket.of();
        if (post.getBoard().getCategory().equals(StaticValue.BOARD_NAME_APP_NOTICE)) {
            validatorBucket
                    .consistOf(UserRoleValidator.of(
                            deleter.getRole(),
                            List.of()
                    ));
        }
        validatorBucket
                .consistOf(UserStateValidator.of(deleter.getState()))
                .consistOf(UserRoleIsNoneValidator.of(deleter.getRole()))
                .consistOf(TargetIsDeletedValidator.of(post.getIsDeleted(), StaticValue.DOMAIN_POST));

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
                                    )).consistOf(ContentsAdminValidator.of(
                                            deleter.getRole(),
                                            loginUserId,
                                            post.getWriter().getId(),
                                            List.of(Role.LEADER_CIRCLE)
                                    ));

                            if (deleter.getRole().getValue().contains("LEADER_CIRCLE") && !post.getWriter().getId().equals(loginUserId)) {
                                validatorBucket
                                        .consistOf(UserEqualValidator.of(
                                                getCircleLeader(circle).getId(),
                                                loginUserId
                                        ));
                            }
                        },
                        () -> validatorBucket
                                .consistOf(ContentsAdminValidator.of(
                                        deleter.getRole(),
                                        loginUserId,
                                        post.getWriter().getId(),
                                        List.of())
                                )
                );
        validatorBucket.validate();

        post.setIsDeleted(true);

        return toPostResponseDto(postRepository.save(post), deleter);
    }

    @Transactional
    public PostResponseDto updatePost(
            String loginUserId,
            String postId,
            PostUpdateRequestDto postUpdateRequestDto
    ) {
        User updater = getUser(loginUserId);
        Post post = getPost(postId);

        ValidatorBucket validatorBucket = initializeValidator(updater, post.getBoard());
        if (post.getBoard().getCategory().equals(StaticValue.BOARD_NAME_APP_NOTICE)) {
            validatorBucket
                    .consistOf(UserRoleValidator.of(
                            updater.getRole(),
                            List.of()
                    ));
        }
        validatorBucket
                .consistOf(PostNumberOfAttachmentsValidator.of(postUpdateRequestDto.getAttachmentList()))
                .consistOf(TargetIsDeletedValidator.of(post.getBoard().getIsDeleted(), StaticValue.DOMAIN_BOARD))
                .consistOf(TargetIsDeletedValidator.of(post.getIsDeleted(), StaticValue.DOMAIN_POST))
                .consistOf(ContentsAdminValidator.of(
                        updater.getRole(),
                        loginUserId,
                        post.getWriter().getId(),
                        List.of()
                ))
                .consistOf(ConstraintValidator.of(post, this.validator));
        validatorBucket.validate();

        post.update(
                postUpdateRequestDto.getTitle(),
                postUpdateRequestDto.getContent(),
                String.join(":::", postUpdateRequestDto.getAttachmentList())
        );

        return toPostResponseDtoExtended(post, updater);
    }

    @Transactional
    public PostResponseDto restorePost(String loginUserId, String postId) {
        User restorer = getUser(loginUserId);
        Post post = getPost(postId);

        ValidatorBucket validatorBucket = ValidatorBucket.of();
        if (post.getBoard().getCategory().equals(StaticValue.BOARD_NAME_APP_NOTICE)) {
            validatorBucket
                    .consistOf(UserRoleValidator.of(
                            restorer.getRole(),
                            List.of()
                    ));
        }
        validatorBucket
                .consistOf(UserStateValidator.of(restorer.getState()))
                .consistOf(UserRoleIsNoneValidator.of(restorer.getRole()))
                .consistOf(TargetIsDeletedValidator.of(post.getBoard().getIsDeleted(), StaticValue.DOMAIN_BOARD))
                .consistOf(TargetIsNotDeletedValidator.of(post.getIsDeleted(), StaticValue.DOMAIN_POST));

        Optional<Circle> circles = Optional.ofNullable(post.getBoard().getCircle());
        circles
                .filter(circle -> !restorer.getRole().equals(Role.ADMIN) && !restorer.getRole().getValue().contains("PRESIDENT"))
                .ifPresentOrElse(
                        circle -> {
                            CircleMember member = getCircleMember(restorer.getId(), circle.getId());

                            validatorBucket
                                    .consistOf(TargetIsDeletedValidator.of(circle.getIsDeleted(), StaticValue.DOMAIN_CIRCLE))
                                    .consistOf(CircleMemberStatusValidator.of(
                                            member.getStatus(),
                                            List.of(CircleMemberStatus.MEMBER)
                                    ))
                                    .consistOf(ContentsAdminValidator.of(
                                            restorer.getRole(),
                                            loginUserId,
                                            post.getWriter().getId(),
                                            List.of(Role.LEADER_CIRCLE)
                                    ));

                            if (restorer.getRole().getValue().contains("LEADER_CIRCLE") && !post.getWriter().getId().equals(loginUserId)) {
                                validatorBucket
                                        .consistOf(UserEqualValidator.of(
                                                getCircleLeader(circle).getId(),
                                                loginUserId
                                        ));
                            }
                        },
                        () -> validatorBucket
                                .consistOf(ContentsAdminValidator.of(
                                        restorer.getRole(),
                                        loginUserId,
                                        post.getWriter().getId(),
                                        List.of()
                                ))
                );

        validatorBucket
                .consistOf(ContentsAdminValidator.of(
                        restorer.getRole(),
                        loginUserId,
                        post.getWriter().getId(),
                        List.of(Role.LEADER_CIRCLE)
                ))
                .validate();

        post.setIsDeleted(false);

        return toPostResponseDtoExtended(postRepository.save(post), restorer);
    }

    private ValidatorBucket initializeValidator(User user, Board board) {
        ValidatorBucket validatorBucket = ValidatorBucket.of();
        validatorBucket
                .consistOf(UserStateValidator.of(user.getState()))
                .consistOf(UserRoleIsNoneValidator.of(user.getRole()));

        Optional<Circle> circles = Optional.ofNullable(board.getCircle());
        circles
                .filter(circle -> !user.getRole().equals(Role.ADMIN) && !user.getRole().getValue().contains("PRESIDENT"))
                .ifPresent(
                        circle -> {
                            CircleMember member = getCircleMember(user.getId(), circle.getId());
                            validatorBucket
                                    .consistOf(TargetIsDeletedValidator.of(circle.getIsDeleted(), StaticValue.DOMAIN_CIRCLE))
                                    .consistOf(CircleMemberStatusValidator.of(
                                            member.getStatus(),
                                            List.of(CircleMemberStatus.MEMBER)
                                    ));
                        }
                );
        return validatorBucket;
    }

    private BoardPostsResponseDto toBoardPostsResponseDto(Board board, Role userRole, boolean isFavorite, Page<PostsResponseDto> post) {
        List<String> roles = new ArrayList<>(Arrays.asList(board.getCreateRoles().split(",")));
        Boolean writable = roles.stream().anyMatch(str -> userRole.getValue().contains(str));
        return DtoMapper.INSTANCE.toBoardPostsResponseDto(
                board,
                userRole,
                writable,
                isFavorite,
                post
        );
    }

    private PostsResponseDto toPostsResponseDto(Post post) {
        return DtoMapper.INSTANCE.toPostsResponseDto(
                post,
                postRepository.countAllCommentByPost_Id(post.getId())
        );
    }

    private PostResponseDto toPostResponseDto(Post post, User user) {
        return DtoMapper.INSTANCE.toPostResponseDto(
                post,
                StatusUtil.isUpdatable(post, user),
                StatusUtil.isDeletable(post, user, post.getBoard())
        );
    }

    private PostResponseDto toPostResponseDtoExtended(Post post, User user) {
        return DtoMapper.INSTANCE.toPostResponseDtoExtended(
                postRepository.save(post),
                findCommentsByPostIdByPage(user, post, 0),
                postRepository.countAllCommentByPost_Id(post.getId()),
                StatusUtil.isUpdatable(post, user),
                StatusUtil.isDeletable(post, user, post.getBoard())
        );
    }

    private Page<CommentResponseDto> findCommentsByPostIdByPage(User user, Post post, Integer pageNum) {
        return commentRepository.findByPost_IdOrderByCreatedAt(
                post.getId(),
                pageableFactory.create(pageNum, StaticValue.DEFAULT_COMMENT_PAGE_SIZE)
        ).map(comment -> CommentResponseDto.of(
                        comment,
                        childCommentRepository.countByParentComment_IdAndIsDeletedIsFalse(comment.getId()),
                        comment.getChildCommentList().stream()
                                .map(childComment -> DtoMapper.INSTANCE.toChildCommentResponseDto(
                                        childComment,
                                        StatusUtil.isUpdatable(childComment, user),
                                        StatusUtil.isDeletable(childComment, user, post.getBoard()))
                                )
                                .collect(Collectors.toList()),
                        StatusUtil.isUpdatable(comment, user),
                        StatusUtil.isDeletable(comment, user, post.getBoard())
                )
        );
    }

    private boolean isFavorite(String userId, String boardId) {
        return favoriteBoardRepository.findByUser_Id(userId)
                .stream()
                .filter(favoriteBoard -> !favoriteBoard.getBoard().getIsDeleted())
                .anyMatch(favoriteboard -> favoriteboard.getBoard().getId().equals(boardId));
    }

    private Post getPost(String postId) {
        return postRepository.findById(postId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.POST_NOT_FOUND
                )
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

    private Board getBoard(String boardId) {
        return boardRepository.findById(boardId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.BOARD_NOT_FOUND
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

    private User getCircleLeader(Circle circle) {
        User leader = circle.getLeader().orElse(null);
        if (leader == null) {
            throw new InternalServerException(
                    ErrorCode.INTERNAL_SERVER,
                    MessageUtil.CIRCLE_WITHOUT_LEADER
            );
        }
        return leader;
    }
}
