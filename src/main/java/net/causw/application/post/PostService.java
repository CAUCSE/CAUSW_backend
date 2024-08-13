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
import net.causw.application.util.ServiceProxy;
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
import net.causw.domain.validation.UserRoleValidator;
import net.causw.domain.validation.ValidatorBucket;
import net.causw.domain.validation.TargetIsNotDeletedValidator;
import net.causw.domain.validation.valid.CircleMemberValid;
import net.causw.domain.validation.valid.PostValid;
import net.causw.domain.validation.valid.UserValid;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.validation.Validator;

import java.util.*;
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
    private final ServiceProxy serviceProxy;

    @Transactional(readOnly = true)
    public PostResponseDto findPostById(User user, String postId) {
        Post post = getPost(postId);
        initializeValidator(user, post.getBoard());
        return toPostResponseDtoExtended(post, user);
    }

    @Transactional(readOnly = true)
    public BoardPostsResponseDto findAllPost(
            User user,
            String boardId,
            Integer pageNum
    ) {
        Set<Role> roles = user.getRoles();
        Board board = getBoard(boardId);
        initializeValidator(user, board);

        boolean isCircleLeader = false;
        if (roles.contains(Role.LEADER_CIRCLE)) {
            isCircleLeader = getCircleLeader(board.getCircle()).getId().equals(user.getId());
        }

        if (isCircleLeader || roles.contains(Role.ADMIN) || roles.contains(Role.PRESIDENT)) {
            return toBoardPostsResponseDto(
                    board,
                    roles,
                    isFavorite(user.getId(), board.getId()),
                    postRepository.findAllByBoard_IdOrderByCreatedAtDesc(boardId, pageableFactory.create(pageNum, StaticValue.DEFAULT_POST_PAGE_SIZE))
                            .map(this::toPostsResponseDto)
            );
        } else {
            return toBoardPostsResponseDto(
                    board,
                    roles,
                    isFavorite(user.getId(), board.getId()),
                    postRepository.findAllByBoard_IdAndIsDeletedOrderByCreatedAtDesc(boardId, pageableFactory.create(pageNum, StaticValue.DEFAULT_POST_PAGE_SIZE), false)
                            .map(this::toPostsResponseDto)
            );
        }
    }

    @Transactional(readOnly = true)
    public BoardPostsResponseDto searchPost(
            User user,
            String boardId,
            String keyword,
            Integer pageNum
    ) {
        Set<Role> roles = user.getRoles();
        Board board = getBoard(boardId);
        new TargetIsDeletedValidator().validate(board.getIsDeleted(), StaticValue.DOMAIN_BOARD);
        initializeValidator(user, board);

        boolean isCircleLeader = false;
        if (roles.contains(Role.LEADER_CIRCLE)) {
            isCircleLeader = getCircleLeader(board.getCircle()).getId().equals(user.getId());
        }

        if (isCircleLeader || roles.contains(Role.ADMIN) || roles.contains(Role.PRESIDENT) || roles.contains(Role.VICE_PRESIDENT)) {
            return toBoardPostsResponseDto(
                    board,
                    roles,
                    isFavorite(user.getId(), board.getId()),
                    postRepository.findAllByBoard_IdOrderByCreatedAtDesc(boardId, pageableFactory.create(pageNum, StaticValue.DEFAULT_POST_PAGE_SIZE))
                            .map(this::toPostsResponseDto));
        } else {
            return toBoardPostsResponseDto(
                    board,
                    roles,
                    isFavorite(user.getId(), board.getId()),
                    postRepository.searchByTitle(keyword, boardId, pageableFactory.create(pageNum, StaticValue.DEFAULT_POST_PAGE_SIZE), false)
                            .map(this::toPostsResponseDto));
        }
    }

    @Transactional(readOnly = true)
    public BoardPostsResponseDto findAllAppNotice(User user, Integer pageNum) {
        Set<Role> roles = user.getRoles();
        Board board = boardRepository.findAppNotice().orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.BOARD_NOT_FOUND
                )
        );

        return toBoardPostsResponseDto(
                board,
                roles,
                isFavorite(user.getId(), board.getId()),
                postRepository.findAllByBoard_IdOrderByCreatedAtDesc(board.getId(), pageableFactory.create(pageNum, StaticValue.DEFAULT_POST_PAGE_SIZE))
                        .map(this::toPostsResponseDto));
    }

    @Transactional
    public PostResponseDto createPost(@UserValid User creator, @PostValid(PostNumberOfAttachmentsValidator = true) PostCreateRequestDto postCreateRequestDto) {
        ValidatorBucket validatorBucket = ValidatorBucket.of();
        Set<Role> roles = creator.getRoles();

        Board board = getBoard(postCreateRequestDto.getBoardId());
        List<String> createRoles = new ArrayList<>(Arrays.asList(board.getCreateRoles().split(",")));
        if (board.getCategory().equals(StaticValue.BOARD_NAME_APP_NOTICE)) {
            new UserRoleValidator().validate(roles, Set.of());
        }

        Post post = Post.of(
                postCreateRequestDto.getTitle(),
                postCreateRequestDto.getContent(),
                creator,
                false,
                board,
                String.join(":::", postCreateRequestDto.getAttachmentList())
        );

        new UserRoleValidator().validate(
                roles,
                createRoles.stream().map(Role::of).collect(Collectors.toSet())
        );
        new TargetIsDeletedValidator().validate(board.getIsDeleted(), StaticValue.DOMAIN_BOARD);

        Optional<Circle> circles = Optional.ofNullable(board.getCircle());
        circles
                .filter(circle -> !roles.contains(Role.ADMIN) && !roles.contains(Role.PRESIDENT) && !roles.contains(Role.VICE_PRESIDENT))
                .ifPresent(
                        circle -> {
                            CircleMember member = serviceProxy.getCircleMemberPost(creator.getId(), circle.getId(), List.of(CircleMemberStatus.MEMBER));

                            new TargetIsDeletedValidator().validate(circle.getIsDeleted(), StaticValue.DOMAIN_CIRCLE);

                            if (roles.contains(Role.LEADER_CIRCLE) && !createRoles.contains("COMMON")) {
                                new UserEqualValidator().validate(creator.getId(), getCircleLeader(circle).getId());
                            }
                        }
                );
        validatorBucket
                .consistOf(ConstraintValidator.of(post, this.validator))
                .validate();

        return toPostResponseDto(postRepository.save(post), creator);
    }

    @Transactional
    public PostResponseDto deletePost(@UserValid User deleter, String postId) {
        Post post = getPost(postId);
        Set<Role> roles = deleter.getRoles();

        ValidatorBucket validatorBucket = ValidatorBucket.of();
        if (post.getBoard().getCategory().equals(StaticValue.BOARD_NAME_APP_NOTICE)) {
            new UserRoleValidator().validate(roles, Set.of());
        }
        new TargetIsDeletedValidator().validate(post.getIsDeleted(), StaticValue.DOMAIN_POST);

        Optional<Circle> circles = Optional.ofNullable(post.getBoard().getCircle());
        circles
                .filter(circle -> !roles.contains(Role.ADMIN) && !roles.contains(Role.PRESIDENT) && !roles.contains(Role.VICE_PRESIDENT))
                .ifPresentOrElse(
                        circle -> {
                            CircleMember member = serviceProxy.getCircleMemberPost(deleter.getId(), circle.getId(), List.of(CircleMemberStatus.MEMBER));

                            validatorBucket
                                    .consistOf(ContentsAdminValidator.of(
                                            roles,
                                            deleter.getId(),
                                            post.getWriter().getId(),
                                            List.of(Role.LEADER_CIRCLE)
                                    ));
                            new TargetIsDeletedValidator().validate(circle.getIsDeleted(), StaticValue.DOMAIN_CIRCLE);

                            if (roles.contains(Role.LEADER_CIRCLE) && !post.getWriter().getId().equals(deleter.getId())) {
                                new UserEqualValidator().validate(deleter.getId(), getCircleLeader(circle).getId());
                            }
                        },
                        () -> validatorBucket
                                .consistOf(ContentsAdminValidator.of(
                                        roles,
                                        deleter.getId(),
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
            User updater,
            String postId,
            @PostValid(PostNumberOfAttachmentsValidator = true) PostUpdateRequestDto postUpdateRequestDto
    ) {
        Set<Role> roles = updater.getRoles();
        Post post = getPost(postId);
        initializeValidator(updater, post.getBoard());
        if (post.getBoard().getCategory().equals(StaticValue.BOARD_NAME_APP_NOTICE)) {
            new UserRoleValidator().validate(roles, Set.of());
        }
        ValidatorBucket validatorBucket = ValidatorBucket.of();
        validatorBucket
                .consistOf(ContentsAdminValidator.of(
                        roles,
                        updater.getId(),
                        post.getWriter().getId(),
                        List.of()
                ))
                .consistOf(ConstraintValidator.of(post, this.validator));
        validatorBucket.validate();
        new TargetIsDeletedValidator().validate(post.getIsDeleted(), StaticValue.DOMAIN_POST);
        new TargetIsDeletedValidator().validate(post.getBoard().getIsDeleted(), StaticValue.DOMAIN_BOARD);

        post.update(
                postUpdateRequestDto.getTitle(),
                postUpdateRequestDto.getContent(),
                String.join(":::", postUpdateRequestDto.getAttachmentList())
        );

        return toPostResponseDtoExtended(post, updater);
    }

    @Transactional
    public PostResponseDto restorePost(@UserValid User restorer, String postId) {
        Set<Role> roles = restorer.getRoles();
        Post post = getPost(postId);

        ValidatorBucket validatorBucket = ValidatorBucket.of();
        if (post.getBoard().getCategory().equals(StaticValue.BOARD_NAME_APP_NOTICE)) {
            new UserRoleValidator().validate(roles, Set.of());
        }
        new TargetIsDeletedValidator().validate(post.getBoard().getIsDeleted(), StaticValue.DOMAIN_BOARD);
        new TargetIsNotDeletedValidator().validate(post.getIsDeleted(), StaticValue.DOMAIN_POST);

        Optional<Circle> circles = Optional.ofNullable(post.getBoard().getCircle());
        circles
                .filter(circle -> !roles.contains(Role.ADMIN) && !roles.contains(Role.PRESIDENT) && !roles.contains(Role.VICE_PRESIDENT))
                .ifPresentOrElse(
                        circle -> {
                            CircleMember member = serviceProxy.getCircleMemberPost(restorer.getId(), circle.getId(), List.of(CircleMemberStatus.MEMBER));

                            validatorBucket
                                    .consistOf(ContentsAdminValidator.of(
                                            roles,
                                            restorer.getId(),
                                            post.getWriter().getId(),
                                            List.of(Role.LEADER_CIRCLE)
                                    ));
                            new TargetIsDeletedValidator().validate(circle.getIsDeleted(), StaticValue.DOMAIN_CIRCLE);

                            if (roles.contains(Role.LEADER_CIRCLE) && !post.getWriter().getId().equals(restorer.getId())) {
                                new UserEqualValidator().validate(restorer.getId(), getCircleLeader(circle).getId());
                            }
                        },
                        () -> validatorBucket
                                .consistOf(ContentsAdminValidator.of(
                                        roles,
                                        restorer.getId(),
                                        post.getWriter().getId(),
                                        List.of()
                                ))
                );

        validatorBucket
                .consistOf(ContentsAdminValidator.of(
                        roles,
                        restorer.getId(),
                        post.getWriter().getId(),
                        List.of(Role.LEADER_CIRCLE)
                ))
                .validate();

        post.setIsDeleted(false);

        return toPostResponseDtoExtended(postRepository.save(post), restorer);
    }

    private void initializeValidator(@UserValid User user, Board board) {
        Set<Role> roles = user.getRoles();

        Optional<Circle> circles = Optional.ofNullable(board.getCircle());
        circles
                .filter(circle -> !roles.contains(Role.ADMIN) && !roles.contains(Role.PRESIDENT) && !roles.contains(Role.VICE_PRESIDENT))
                .ifPresent(
                        circle -> {
                            CircleMember member = serviceProxy.getCircleMemberPost(user.getId(), circle.getId(), List.of(CircleMemberStatus.MEMBER));
                            new TargetIsDeletedValidator().validate(circle.getIsDeleted(), StaticValue.DOMAIN_CIRCLE);
                        }
                );
    }

    private BoardPostsResponseDto toBoardPostsResponseDto(Board board, Set<Role> userRoles, boolean isFavorite, Page<PostsResponseDto> post) {
        List<String> roles = Arrays.asList(board.getCreateRoles().split(","));
        Boolean writable = userRoles.stream()
                .map(Role::getValue)
                .anyMatch(roles::contains);
        return DtoMapper.INSTANCE.toBoardPostsResponseDto(
                board,
                userRoles,
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

    private Board getBoard(String boardId) {
        return boardRepository.findById(boardId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.BOARD_NOT_FOUND
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
