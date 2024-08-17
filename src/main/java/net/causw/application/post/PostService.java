package net.causw.application.post;

import lombok.RequiredArgsConstructor;
import net.causw.adapter.persistence.board.Board;
import net.causw.adapter.persistence.circle.Circle;
import net.causw.adapter.persistence.circle.CircleMember;
import net.causw.adapter.persistence.comment.ChildComment;
import net.causw.adapter.persistence.comment.Comment;
import net.causw.adapter.persistence.page.PageableFactory;
import net.causw.adapter.persistence.post.FavoritePost;
import net.causw.adapter.persistence.post.LikePost;
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
    private final LikePostRepository likePostRepository;
    private final FavoritePostRepository favoritePostRepository;
    private final LikeCommentRepository likeCommentRepository;
    private final LikeChildCommentRepository likeChildCommentRepository;
    private final PageableFactory pageableFactory;
    private final Validator validator;

    @Transactional(readOnly = true)
    public PostResponseDto findPostById(User user, String postId) {
        Post post = getPost(postId);

        ValidatorBucket validatorBucket = initializeValidator(user, post.getBoard());
        validatorBucket.validate();

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

        ValidatorBucket validatorBucket = initializeValidator(user, board);
        validatorBucket.validate();

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

        ValidatorBucket validatorBucket = initializeValidator(user, board);
        validatorBucket
                .consistOf(TargetIsDeletedValidator.of(board.getIsDeleted(), StaticValue.DOMAIN_BOARD))
                .validate();

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
    public PostResponseDto createPost(User creator, PostCreateRequestDto postCreateRequestDto) {
        ValidatorBucket validatorBucket = ValidatorBucket.of();
        Set<Role> roles = creator.getRoles();

        Board board = getBoard(postCreateRequestDto.getBoardId());
        List<String> createRoles = new ArrayList<>(Arrays.asList(board.getCreateRoles().split(",")));
        if (board.getCategory().equals(StaticValue.BOARD_NAME_APP_NOTICE)) {
            validatorBucket
                    .consistOf(UserRoleValidator.of(
                            roles,
                            Set.of()
                    ));
        }

        Post post = Post.of(
                postCreateRequestDto.getTitle(),
                postCreateRequestDto.getContent(),
                creator,
                false,
                postCreateRequestDto.getIsAnonymous(),
                postCreateRequestDto.getIsQuestion(),
                board,
                String.join(":::", postCreateRequestDto.getAttachmentList())
        );

        validatorBucket
                .consistOf(UserStateValidator.of(creator.getState()))
                .consistOf(UserRoleIsNoneValidator.of(roles))
                .consistOf(PostNumberOfAttachmentsValidator.of(postCreateRequestDto.getAttachmentList()))
                .consistOf(TargetIsDeletedValidator.of(board.getIsDeleted(), StaticValue.DOMAIN_BOARD))
                .consistOf(UserRoleValidator.of(
                        roles,
                        createRoles.stream()
                                .map(Role::of)
                                .collect(Collectors.toSet())
                ));

        Optional<Circle> circles = Optional.ofNullable(board.getCircle());
        circles
                .filter(circle -> !roles.contains(Role.ADMIN) && !roles.contains(Role.PRESIDENT) && !roles.contains(Role.VICE_PRESIDENT))
                .ifPresent(
                        circle -> {
                            CircleMember member = getCircleMember(creator.getId(), circle.getId());

                            validatorBucket
                                    .consistOf(TargetIsDeletedValidator.of(circle.getIsDeleted(), StaticValue.DOMAIN_CIRCLE))
                                    .consistOf(CircleMemberStatusValidator.of(
                                            member.getStatus(),
                                            List.of(CircleMemberStatus.MEMBER)
                                    ));

                            if (roles.contains(Role.LEADER_CIRCLE) && !createRoles.contains("COMMON")) {
                                validatorBucket
                                        .consistOf(UserEqualValidator.of(
                                                getCircleLeader(circle).getId(),
                                                creator.getId()
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
    public PostResponseDto deletePost(User deleter, String postId) {
        Post post = getPost(postId);
        Set<Role> roles = deleter.getRoles();

        ValidatorBucket validatorBucket = ValidatorBucket.of();
        if (post.getBoard().getCategory().equals(StaticValue.BOARD_NAME_APP_NOTICE)) {
            validatorBucket
                    .consistOf(UserRoleValidator.of(
                            roles,
                            Set.of()
                    ));
        }
        validatorBucket
                .consistOf(UserStateValidator.of(deleter.getState()))
                .consistOf(UserRoleIsNoneValidator.of(roles))
                .consistOf(TargetIsDeletedValidator.of(post.getIsDeleted(), StaticValue.DOMAIN_POST));

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
                                    )).consistOf(ContentsAdminValidator.of(
                                            roles,
                                            deleter.getId(),
                                            post.getWriter().getId(),
                                            List.of(Role.LEADER_CIRCLE)
                                    ));

                            if (roles.contains(Role.LEADER_CIRCLE) && !post.getWriter().getId().equals(deleter.getId())) {
                                validatorBucket
                                        .consistOf(UserEqualValidator.of(
                                                getCircleLeader(circle).getId(),
                                                deleter.getId()
                                        ));
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
            PostUpdateRequestDto postUpdateRequestDto
    ) {
        Set<Role> roles = updater.getRoles();
        Post post = getPost(postId);

        ValidatorBucket validatorBucket = initializeValidator(updater, post.getBoard());
        if (post.getBoard().getCategory().equals(StaticValue.BOARD_NAME_APP_NOTICE)) {
            validatorBucket
                    .consistOf(UserRoleValidator.of(
                            roles,
                            Set.of()
                    ));
        }
        validatorBucket
                .consistOf(PostNumberOfAttachmentsValidator.of(postUpdateRequestDto.getAttachmentList()))
                .consistOf(TargetIsDeletedValidator.of(post.getBoard().getIsDeleted(), StaticValue.DOMAIN_BOARD))
                .consistOf(TargetIsDeletedValidator.of(post.getIsDeleted(), StaticValue.DOMAIN_POST))
                .consistOf(ContentsAdminValidator.of(
                        roles,
                        updater.getId(),
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
    public PostResponseDto restorePost(User restorer, String postId) {
        Set<Role> roles = restorer.getRoles();
        Post post = getPost(postId);

        ValidatorBucket validatorBucket = ValidatorBucket.of();
        if (post.getBoard().getCategory().equals(StaticValue.BOARD_NAME_APP_NOTICE)) {
            validatorBucket
                    .consistOf(UserRoleValidator.of(
                            roles,
                            Set.of()
                    ));
        }
        validatorBucket
                .consistOf(UserStateValidator.of(restorer.getState()))
                .consistOf(UserRoleIsNoneValidator.of(roles))
                .consistOf(TargetIsDeletedValidator.of(post.getBoard().getIsDeleted(), StaticValue.DOMAIN_BOARD))
                .consistOf(TargetIsNotDeletedValidator.of(post.getIsDeleted(), StaticValue.DOMAIN_POST));

        Optional<Circle> circles = Optional.ofNullable(post.getBoard().getCircle());
        circles
                .filter(circle -> !roles.contains(Role.ADMIN) && !roles.contains(Role.PRESIDENT) && !roles.contains(Role.VICE_PRESIDENT))
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
                                            roles,
                                            restorer.getId(),
                                            post.getWriter().getId(),
                                            List.of(Role.LEADER_CIRCLE)
                                    ));

                            if (roles.contains(Role.LEADER_CIRCLE) && !post.getWriter().getId().equals(restorer.getId())) {
                                validatorBucket
                                        .consistOf(UserEqualValidator.of(
                                                getCircleLeader(circle).getId(),
                                                restorer.getId()
                                        ));
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

    @Transactional
    public void likePost(User user, String postId) {
        Post post = getPost(postId);

        if (isPostAlreadyLike(user, postId)) {
            throw new BadRequestException(ErrorCode.ROW_ALREADY_EXIST, MessageUtil.POST_ALREADY_LIKED);
        }

        LikePost likePost = LikePost.of(post, user);
        likePostRepository.save(likePost);
    }

    @Transactional
    public void favoritePost(User user, String postId) {
        Post post = getPost(postId);

        //FIXME : Validator 리팩토링 통합 후 해당 검사 로직을 해당방식으로 수정.
        if (isPostDeleted(post)) {
            throw new BadRequestException(ErrorCode.TARGET_DELETED, MessageUtil.POST_DELETED);
        }

        FavoritePost favoritePost;
        if (isPostAlreadyFavorite(user, postId)) {
            favoritePost = getFavoritePost(user, postId);
            if (favoritePost.getIsDeleted()) {
                favoritePost.setIsDeleted(false);
            } else {
                throw new BadRequestException(ErrorCode.ROW_ALREADY_EXIST, MessageUtil.POST_ALREADY_FAVORITED);
            }
        } else {
            favoritePost = FavoritePost.of(post, user, false);
        }

        favoritePostRepository.save(favoritePost);
    }


    @Transactional
    public void cancelFavoritePost(User user, String postId) {
        Post post = getPost(postId);

        //FIXME : Validator 리팩토링 통합 후 해당 검사 로직을 해당방식으로 수정.
        if (isPostDeleted(post)) {
            throw new BadRequestException(ErrorCode.TARGET_DELETED, MessageUtil.POST_DELETED);
        }

        FavoritePost favoritePost = getFavoritePost(user, postId);
        if (favoritePost.getIsDeleted()) {
            throw new BadRequestException(ErrorCode.ROW_ALREADY_EXIST, MessageUtil.FAVORITE_POST_ALREADY_DELETED);
        } else {
            favoritePost.setIsDeleted(true);
        }

        favoritePostRepository.save(favoritePost);
    }

    private boolean isPostAlreadyLike(User user, String postId) {
        return likePostRepository.existsByPostIdAndUserId(postId, user.getId());
    }

    private boolean isPostAlreadyFavorite(User user, String postId) {
        return favoritePostRepository.existsByPostIdAndUserId(postId, user.getId());
    }

    private boolean isPostDeleted(Post post) {
        return post.getIsDeleted();
    }

    private ValidatorBucket initializeValidator(User user, Board board) {
        Set<Role> roles = user.getRoles();
        ValidatorBucket validatorBucket = ValidatorBucket.of();
        validatorBucket
                .consistOf(UserStateValidator.of(user.getState()))
                .consistOf(UserRoleIsNoneValidator.of(roles));

        Optional<Circle> circles = Optional.ofNullable(board.getCircle());
        circles
                .filter(circle -> !roles.contains(Role.ADMIN) && !roles.contains(Role.PRESIDENT) && !roles.contains(Role.VICE_PRESIDENT))
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
                postRepository.countAllCommentByPost_Id(post.getId()),
                getNumOfPostLikes(post),
                getNumOfPostLikes(post)
        );
    }

    private PostResponseDto toPostResponseDto(Post post, User user) {
        return DtoMapper.INSTANCE.toPostResponseDto(
                post,
                getNumOfPostLikes(post),
                getNumOfPostFavorites(post),
                StatusUtil.isUpdatable(post, user),
                StatusUtil.isDeletable(post, user, post.getBoard())
        );
    }

    private PostResponseDto toPostResponseDtoExtended(Post post, User user) {
        return DtoMapper.INSTANCE.toPostResponseDtoExtended(
                postRepository.save(post),
                findCommentsByPostIdByPage(user, post, 0),
                postRepository.countAllCommentByPost_Id(post.getId()),
                getNumOfPostLikes(post),
                getNumOfPostFavorites(post),
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
                        getNumOfCommentLikes(comment),
                        comment.getChildCommentList().stream()
                                .map(childComment -> DtoMapper.INSTANCE.toChildCommentResponseDto(
                                        childComment,
                                        getNumOfChildCommentLikes(childComment),
                                        StatusUtil.isUpdatable(childComment, user),
                                        StatusUtil.isDeletable(childComment, user, post.getBoard()))
                                )
                                .collect(Collectors.toList()),
                        StatusUtil.isUpdatable(comment, user),
                        StatusUtil.isDeletable(comment, user, post.getBoard()),
                        comment.getIsAnonymous()
                )
        );
    }

    private Long getNumOfPostLikes(Post post){
        return likePostRepository.countByPostId(post.getId());
    }

    private Long getNumOfPostFavorites(Post post){
        return favoritePostRepository.countByPostIdAndIsDeletedFalse(post.getId());
    }

    private Long getNumOfCommentLikes(Comment comment){
        return likeCommentRepository.countByCommentId(comment.getId());
    }

    private Long getNumOfChildCommentLikes(ChildComment childComment) {
        return likeChildCommentRepository.countByChildCommentId(childComment.getId());
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

    private FavoritePost getFavoritePost(User user, String postId) {
        return favoritePostRepository.findByPostIdAndUserId(postId, user.getId()).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.FAVORITE_POST_NOT_FOUND
                )
        );
    }

}
