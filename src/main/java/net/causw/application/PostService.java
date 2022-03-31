package net.causw.application;

import net.causw.application.dto.comment.CommentResponseDto;
import net.causw.application.dto.post.BoardPostsResponseDto;
import net.causw.application.dto.post.PostCreateRequestDto;
import net.causw.application.dto.post.PostResponseDto;
import net.causw.application.dto.post.PostUpdateRequestDto;
import net.causw.application.dto.post.PostsResponseDto;
import net.causw.application.spi.BoardPort;
import net.causw.application.spi.ChildCommentPort;
import net.causw.application.spi.CircleMemberPort;
import net.causw.application.spi.CommentPort;
import net.causw.application.spi.FavoriteBoardPort;
import net.causw.application.spi.PostPort;
import net.causw.application.spi.UserPort;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.exceptions.InternalServerException;
import net.causw.domain.exceptions.UnauthorizedException;
import net.causw.domain.model.BoardDomainModel;
import net.causw.domain.model.CircleMemberDomainModel;
import net.causw.domain.model.CircleMemberStatus;
import net.causw.domain.model.PostDomainModel;
import net.causw.domain.model.Role;
import net.causw.domain.model.SearchOption;
import net.causw.domain.model.StaticValue;
import net.causw.domain.model.UserDomainModel;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.Validator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PostService {
    private final PostPort postPort;
    private final UserPort userPort;
    private final BoardPort boardPort;
    private final CircleMemberPort circleMemberPort;
    private final CommentPort commentPort;
    private final ChildCommentPort childCommentPort;
    private final FavoriteBoardPort favoriteBoardPort;
    private final Validator validator;

    public PostService(
            PostPort postPort,
            UserPort userPort,
            BoardPort boardPort,
            CircleMemberPort circleMemberPort,
            CommentPort commentPort,
            ChildCommentPort childCommentPort,
            FavoriteBoardPort favoriteBoardPort,
            Validator validator
    ) {
        this.postPort = postPort;
        this.userPort = userPort;
        this.boardPort = boardPort;
        this.circleMemberPort = circleMemberPort;
        this.commentPort = commentPort;
        this.childCommentPort = childCommentPort;
        this.favoriteBoardPort = favoriteBoardPort;
        this.validator = validator;
    }

    @Transactional(readOnly = true)
    public PostResponseDto findById(String requestUserId, String postId) {
        ValidatorBucket validatorBucket = ValidatorBucket.of();

        UserDomainModel userDomainModel = this.userPort.findById(requestUserId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "로그인된 사용자를 찾을 수 없습니다."
                )
        );

        PostDomainModel postDomainModel = this.postPort.findById(postId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "게시글을 찾을 수 없습니다."
                )
        );

        validatorBucket
                .consistOf(UserStateValidator.of(userDomainModel.getState()))
                .consistOf(UserRoleIsNoneValidator.of(userDomainModel.getRole()))
                .consistOf(TargetIsDeletedValidator.of(postDomainModel.getBoard().getIsDeleted(), StaticValue.DOMAIN_BOARD))
                .consistOf(TargetIsDeletedValidator.of(postDomainModel.getIsDeleted(), StaticValue.DOMAIN_POST));

        postDomainModel.getBoard().getCircle()
                .filter(circleDomainModel -> !userDomainModel.getRole().equals(Role.ADMIN))
                .ifPresent(
                        circleDomainModel -> {
                            CircleMemberDomainModel circleMemberDomainModel = this.circleMemberPort.findByUserIdAndCircleId(
                                    userDomainModel.getId(),
                                    circleDomainModel.getId()
                            ).orElseThrow(
                                    () -> new UnauthorizedException(
                                            ErrorCode.NOT_MEMBER,
                                            "로그인된 사용자가 소모임 멤버가 아닙니다."
                                    )
                            );

                            validatorBucket
                                    .consistOf(TargetIsDeletedValidator.of(circleDomainModel.getIsDeleted(), StaticValue.DOMAIN_CIRCLE))
                                    .consistOf(CircleMemberStatusValidator.of(
                                            circleMemberDomainModel.getStatus(),
                                            List.of(CircleMemberStatus.MEMBER)
                                    ));
                        }
                );

        validatorBucket
                .validate();

        return PostResponseDto.from(
                postDomainModel,
                userDomainModel,
                this.commentPort.findByPostId(postId, 0)
                        .map(
                                commentDomainModel -> CommentResponseDto.from(
                                        commentDomainModel,
                                        userDomainModel,
                                        postDomainModel.getBoard(),
                                        this.childCommentPort.countByParentComment(commentDomainModel.getId())
                                )
                        ),
                this.commentPort.countByPostId(postDomainModel.getId())
        );
    }

    @Transactional(readOnly = true)
    public BoardPostsResponseDto findAll(
            String requestUserId,
            String boardId,
            Integer pageNum
    ) {
        ValidatorBucket validatorBucket = ValidatorBucket.of();

        UserDomainModel userDomainModel = this.userPort.findById(requestUserId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "로그인된 사용자를 찾을 수 없습니다."
                )
        );

        validatorBucket
                .consistOf(UserStateValidator.of(userDomainModel.getState()))
                .consistOf(UserRoleIsNoneValidator.of(userDomainModel.getRole()));

        BoardDomainModel boardDomainModel = this.boardPort.findById(boardId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "게시판을 찾을 수 없습니다."
                )
        );

        boardDomainModel.getCircle()
                .filter(circleDomainModel -> !userDomainModel.getRole().equals(Role.ADMIN))
                .ifPresent(
                        circleDomainModel -> {
                            CircleMemberDomainModel circleMemberDomainModel = this.circleMemberPort.findByUserIdAndCircleId(
                                    userDomainModel.getId(),
                                    circleDomainModel.getId()
                            ).orElseThrow(
                                    () -> new UnauthorizedException(
                                            ErrorCode.NOT_MEMBER,
                                            "로그인된 사용자가 소모임 멤버가 아닙니다."
                                    )
                            );

                            validatorBucket
                                    .consistOf(TargetIsDeletedValidator.of(circleDomainModel.getIsDeleted(), StaticValue.DOMAIN_CIRCLE))
                                    .consistOf(CircleMemberStatusValidator.of(
                                            circleMemberDomainModel.getStatus(),
                                            List.of(CircleMemberStatus.MEMBER)
                                    ));
                        }
                );

        validatorBucket
                .consistOf(TargetIsDeletedValidator.of(boardDomainModel.getIsDeleted(), StaticValue.DOMAIN_BOARD))
                .validate();

        return BoardPostsResponseDto.from(
                boardDomainModel,
                userDomainModel.getRole(),
                this.favoriteBoardPort.findByUserId(requestUserId)
                        .stream()
                        .anyMatch(favoriteBoardDomainModel -> favoriteBoardDomainModel.getBoardDomainModel().getId().equals(boardDomainModel.getId())),
                this.postPort.findAll(boardId, pageNum)
                        .map(postDomainModel -> PostsResponseDto.from(
                                postDomainModel,
                                this.commentPort.countByPostId(postDomainModel.getId())
                        ))
        );
    }

    @Transactional(readOnly = true)
    public BoardPostsResponseDto search(
            String requestUserId,
            String boardId,
            String option,
            String keyword,
            Integer pageNum
    ) {
        ValidatorBucket validatorBucket = ValidatorBucket.of();

        UserDomainModel userDomainModel = this.userPort.findById(requestUserId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "로그인된 사용자를 찾을 수 없습니다."
                )
        );

        validatorBucket
                .consistOf(UserStateValidator.of(userDomainModel.getState()))
                .consistOf(UserRoleIsNoneValidator.of(userDomainModel.getRole()));

        BoardDomainModel boardDomainModel = this.boardPort.findById(boardId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "게시판을 찾을 수 없습니다."
                )
        );

        boardDomainModel.getCircle()
                .filter(circleDomainModel -> !userDomainModel.getRole().equals(Role.ADMIN))
                .ifPresent(
                        circleDomainModel -> {
                            CircleMemberDomainModel circleMemberDomainModel = this.circleMemberPort.findByUserIdAndCircleId(
                                            userDomainModel.getId(),
                                            circleDomainModel.getId()
                                    )
                                    .orElseThrow(
                                            () -> new UnauthorizedException(
                                                    ErrorCode.NOT_MEMBER,
                                                    "로그인된 사용자가 소모임 멤버가 아닙니다."
                                            )
                                    );

                            validatorBucket
                                    .consistOf(TargetIsDeletedValidator.of(circleDomainModel.getIsDeleted(), StaticValue.DOMAIN_CIRCLE))
                                    .consistOf(CircleMemberStatusValidator.of(
                                            circleMemberDomainModel.getStatus(),
                                            List.of(CircleMemberStatus.MEMBER)
                                    ));
                        }
                );

        validatorBucket
                .consistOf(TargetIsDeletedValidator.of(boardDomainModel.getIsDeleted(), StaticValue.DOMAIN_BOARD))
                .validate();

        SearchOption searchOption = Optional.ofNullable(SearchOption.of(option)).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.INVALID_PARAMETER,
                        "잘못된 검색 옵션입니다."
                )
        );

        return BoardPostsResponseDto.from(
                boardDomainModel,
                userDomainModel.getRole(),
                this.favoriteBoardPort.findByUserId(requestUserId)
                        .stream()
                        .anyMatch(favoriteBoardDomainModel -> favoriteBoardDomainModel.getBoardDomainModel().getId().equals(boardDomainModel.getId())),
                this.postPort.search(searchOption, keyword, pageNum)
                        .map(postDomainModel -> PostsResponseDto.from(
                                postDomainModel,
                                this.commentPort.countByPostId(postDomainModel.getId())
                        ))
        );
    }

    @Transactional(readOnly = true)
    public BoardPostsResponseDto findAllAppNotice(String requestUserId, Integer pageNum) {
        UserDomainModel userDomainModel = this.userPort.findById(requestUserId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "로그인된 사용자를 찾을 수 없습니다."
                )
        );

        BoardDomainModel boardDomainModel = this.boardPort.findAppNotice().orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "앱 공지 게시판을 찾을 수 없습니다."
                )
        );

        return BoardPostsResponseDto.from(
                boardDomainModel,
                userDomainModel.getRole(),
                this.favoriteBoardPort.findByUserId(requestUserId)
                        .stream()
                        .anyMatch(favoriteBoardDomainModel -> favoriteBoardDomainModel.getBoardDomainModel().getId().equals(boardDomainModel.getId())),
                this.postPort.findAll(boardDomainModel.getId(), pageNum)
                        .map(postDomainModel -> PostsResponseDto.from(
                                postDomainModel,
                                this.commentPort.countByPostId(postDomainModel.getId())
                        ))
        );
    }

    @Transactional
    public PostResponseDto create(String requestUserId, PostCreateRequestDto postCreateRequestDto) {
        ValidatorBucket validatorBucket = ValidatorBucket.of();

        UserDomainModel creatorDomainModel = this.userPort.findById(requestUserId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "로그인된 사용자를 찾을 수 없습니다."
                )
        );

        BoardDomainModel boardDomainModel = this.boardPort.findById(postCreateRequestDto.getBoardId()).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "게시판을 찾을 수 없습니다."
                )
        );

        if (boardDomainModel.getCategory().equals(StaticValue.BOARD_NAME_APP_NOTICE)) {
            validatorBucket
                    .consistOf(UserRoleValidator.of(
                            creatorDomainModel.getRole(),
                            List.of()
                    ));
        }

        validatorBucket
                .consistOf(UserStateValidator.of(creatorDomainModel.getState()))
                .consistOf(UserRoleIsNoneValidator.of(creatorDomainModel.getRole()))
                .consistOf(PostNumberOfAttachmentsValidator.of(postCreateRequestDto.getAttachmentList()))
                .consistOf(TargetIsDeletedValidator.of(boardDomainModel.getIsDeleted(), StaticValue.DOMAIN_BOARD))
                .consistOf(UserRoleValidator.of(
                        creatorDomainModel.getRole(),
                        boardDomainModel.getCreateRoleList()
                                .stream()
                                .map(Role::of)
                                .collect(Collectors.toList())
                ));

        boardDomainModel.getCircle()
                .filter(circleDomainModel -> !creatorDomainModel.getRole().equals(Role.ADMIN))
                .ifPresent(
                        circleDomainModel -> {
                            CircleMemberDomainModel circleMemberDomainModel = this.circleMemberPort.findByUserIdAndCircleId(
                                    requestUserId,
                                    circleDomainModel.getId()
                            ).orElseThrow(
                                    () -> new UnauthorizedException(
                                            ErrorCode.NOT_MEMBER,
                                            "로그인된 사용자가 소모임 멤버가 아닙니다."
                                    )
                            );

                            validatorBucket
                                    .consistOf(TargetIsDeletedValidator.of(circleDomainModel.getIsDeleted(), StaticValue.DOMAIN_CIRCLE))
                                    .consistOf(CircleMemberStatusValidator.of(
                                            circleMemberDomainModel.getStatus(),
                                            List.of(CircleMemberStatus.MEMBER)
                                    ));
                        }
                );

        PostDomainModel postDomainModel = PostDomainModel.of(
                postCreateRequestDto.getTitle(),
                postCreateRequestDto.getContent(),
                creatorDomainModel,
                boardDomainModel,
                postCreateRequestDto.getAttachmentList()
        );

        validatorBucket
                .consistOf(ConstraintValidator.of(postDomainModel, this.validator))
                .validate();

        return PostResponseDto.from(
                this.postPort.create(postDomainModel),
                creatorDomainModel
        );
    }

    @Transactional
    public PostResponseDto delete(String requestUserId, String postId) {
        ValidatorBucket validatorBucket = ValidatorBucket.of();

        UserDomainModel requestUser = this.userPort.findById(requestUserId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "로그인된 사용자를 찾을 수 없습니다."
                )
        );

        PostDomainModel postDomainModel = this.postPort.findById(postId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "게시글을 찾을 수 없습니다."
                )
        );

        if (postDomainModel.getBoard().getCategory().equals(StaticValue.BOARD_NAME_APP_NOTICE)) {
            validatorBucket
                    .consistOf(UserRoleValidator.of(
                            requestUser.getRole(),
                            List.of()
                    ));
        }

        validatorBucket
                .consistOf(UserStateValidator.of(requestUser.getState()))
                .consistOf(UserRoleIsNoneValidator.of(requestUser.getRole()))
                .consistOf(TargetIsDeletedValidator.of(postDomainModel.getIsDeleted(), StaticValue.DOMAIN_POST));

        postDomainModel.getBoard().getCircle()
                .filter(circleDomainModel -> !requestUser.getRole().equals(Role.ADMIN))
                .ifPresentOrElse(
                        circleDomainModel -> {
                            CircleMemberDomainModel circleMemberDomainModel = this.circleMemberPort.findByUserIdAndCircleId(
                                    requestUser.getId(),
                                    circleDomainModel.getId()
                            ).orElseThrow(
                                    () -> new UnauthorizedException(
                                            ErrorCode.NOT_MEMBER,
                                            "로그인된 사용자가 소모임 멤버가 아닙니다."
                                    )
                            );

                            validatorBucket
                                    .consistOf(TargetIsDeletedValidator.of(circleDomainModel.getIsDeleted(), StaticValue.DOMAIN_CIRCLE))
                                    .consistOf(CircleMemberStatusValidator.of(
                                            circleMemberDomainModel.getStatus(),
                                            List.of(CircleMemberStatus.MEMBER)
                                    ))
                                    .consistOf(ContentsAdminValidator.of(
                                            requestUser.getRole(),
                                            requestUserId,
                                            postDomainModel.getWriter().getId(),
                                            List.of(Role.LEADER_CIRCLE)
                                    ));

                            if (requestUser.getRole().equals(Role.LEADER_CIRCLE)) {
                                validatorBucket
                                        .consistOf(UserEqualValidator.of(
                                                circleDomainModel.getLeader().map(UserDomainModel::getId).orElseThrow(
                                                        () -> new InternalServerException(
                                                                ErrorCode.INTERNAL_SERVER,
                                                                "The board has circle without circle leader"
                                                        )
                                                ),
                                                requestUserId
                                        ));
                            }
                        },
                        () -> validatorBucket
                                .consistOf(ContentsAdminValidator.of(
                                        requestUser.getRole(),
                                        requestUserId,
                                        postDomainModel.getWriter().getId(),
                                        List.of(Role.PRESIDENT)
                                ))
                );

        validatorBucket
                .validate();

        return PostResponseDto.from(
                this.postPort.delete(postId).orElseThrow(
                        () -> new InternalServerException(
                                ErrorCode.INTERNAL_SERVER,
                                "Post id checked, but exception occurred"
                        )
                ),
                requestUser
        );
    }

    @Transactional
    public PostResponseDto update(
            String requestUserId,
            String postId,
            PostUpdateRequestDto postUpdateRequestDto
    ) {
        ValidatorBucket validatorBucket = ValidatorBucket.of();

        UserDomainModel requestUser = this.userPort.findById(requestUserId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "로그인된 사용자를 찾을 수 없습니다."
                )
        );

        PostDomainModel postDomainModel = this.postPort.findById(postId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "게시글을 찾을 수 없습니다."
                )
        );

        if (postDomainModel.getBoard().getCategory().equals(StaticValue.BOARD_NAME_APP_NOTICE)) {
            validatorBucket
                    .consistOf(UserRoleValidator.of(
                            requestUser.getRole(),
                            List.of()
                    ));
        }

        validatorBucket
                .consistOf(UserStateValidator.of(requestUser.getState()))
                .consistOf(UserRoleIsNoneValidator.of(requestUser.getRole()))
                .consistOf(PostNumberOfAttachmentsValidator.of(postUpdateRequestDto.getAttachmentList()))
                .consistOf(TargetIsDeletedValidator.of(postDomainModel.getBoard().getIsDeleted(), StaticValue.DOMAIN_BOARD))
                .consistOf(TargetIsDeletedValidator.of(postDomainModel.getIsDeleted(), StaticValue.DOMAIN_POST));

        postDomainModel.getBoard().getCircle()
                .filter(circleDomainModel -> !requestUser.getRole().equals(Role.ADMIN))
                .ifPresent(
                        circleDomainModel -> {
                            CircleMemberDomainModel circleMemberDomainModel = this.circleMemberPort.findByUserIdAndCircleId(
                                    requestUserId,
                                    circleDomainModel.getId()
                            ).orElseThrow(
                                    () -> new UnauthorizedException(
                                            ErrorCode.NOT_MEMBER,
                                            "로그인된 사용자가 소모임 멤버가 아닙니다."
                                    )
                            );

                            validatorBucket
                                    .consistOf(TargetIsDeletedValidator.of(circleDomainModel.getIsDeleted(), StaticValue.DOMAIN_CIRCLE))
                                    .consistOf(CircleMemberStatusValidator.of(
                                            circleMemberDomainModel.getStatus(),
                                            List.of(CircleMemberStatus.MEMBER)
                                    ));
                        }
                );

        postDomainModel.update(
                postUpdateRequestDto.getTitle(),
                postUpdateRequestDto.getContent(),
                postUpdateRequestDto.getAttachmentList()
        );

        validatorBucket
                .consistOf(ContentsAdminValidator.of(
                        requestUser.getRole(),
                        requestUserId,
                        postDomainModel.getWriter().getId(),
                        List.of()
                ))
                .consistOf(ConstraintValidator.of(postDomainModel, this.validator))
                .validate();

        PostDomainModel updatedPostDomainModel = this.postPort.update(postId, postDomainModel).orElseThrow(
                () -> new InternalServerException(
                        ErrorCode.INTERNAL_SERVER,
                        "Post id checked, but exception occurred"
                )
        );

        return PostResponseDto.from(
                postDomainModel,
                requestUser,
                this.commentPort.findByPostId(postId, 0)
                        .map(commentDomainModel -> CommentResponseDto.from(
                                commentDomainModel,
                                requestUser,
                                updatedPostDomainModel.getBoard(),
                                this.childCommentPort.countByParentComment(commentDomainModel.getId())
                        )),
                this.commentPort.countByPostId(postDomainModel.getId())
        );
    }
}
