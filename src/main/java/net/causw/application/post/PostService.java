package net.causw.application.post;

import lombok.RequiredArgsConstructor;
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
import net.causw.domain.model.board.BoardDomainModel;
import net.causw.domain.model.circle.CircleMemberDomainModel;
import net.causw.domain.model.enums.CircleMemberStatus;
import net.causw.domain.model.post.PostDomainModel;
import net.causw.domain.model.enums.Role;
import net.causw.domain.model.util.StaticValue;
import net.causw.domain.model.user.UserDomainModel;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.Validator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostPort postPort;
    private final UserPort userPort;
    private final BoardPort boardPort;
    private final CircleMemberPort circleMemberPort;
    private final CommentPort commentPort;
    private final ChildCommentPort childCommentPort;
    private final FavoriteBoardPort favoriteBoardPort;
    private final Validator validator;

    @Transactional(readOnly = true)
    public PostResponseDto findPostById(String loginUserId, String postId) {
        ValidatorBucket validatorBucket = ValidatorBucket.of();

        UserDomainModel userDomainModel = this.userPort.findById(loginUserId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "로그인된 사용자를 찾을 수 없습니다."
                )
        );

        PostDomainModel postDomainModel = this.postPort.findPostById(postId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "게시글을 찾을 수 없습니다."
                )
        );

        validatorBucket
                .consistOf(UserStateValidator.of(userDomainModel.getState()))
                .consistOf(UserRoleIsNoneValidator.of(userDomainModel.getRole()));

        postDomainModel.getBoard().getCircle()
                .filter(circleDomainModel -> !userDomainModel.getRole().equals(Role.ADMIN) && !userDomainModel.getRole().getValue().contains("PRESIDENT"))
                .ifPresent(
                        circleDomainModel -> {
                            CircleMemberDomainModel circleMemberDomainModel = this.circleMemberPort.findByUserIdAndCircleId(
                                    userDomainModel.getId(),
                                    circleDomainModel.getId()
                            ).orElseThrow(
                                    () -> new UnauthorizedException(
                                            ErrorCode.NOT_MEMBER,
                                            "로그인된 사용자가 동아리 멤버가 아닙니다."
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
    public BoardPostsResponseDto findAllPost(
            String loginUserId,
            String boardId,
            Integer pageNum
    ) {
        ValidatorBucket validatorBucket = ValidatorBucket.of();

        UserDomainModel userDomainModel = this.userPort.findById(loginUserId).orElseThrow(
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
                .filter(circleDomainModel -> !userDomainModel.getRole().equals(Role.ADMIN) && !userDomainModel.getRole().getValue().contains("PRESIDENT"))
                .ifPresent(
                        circleDomainModel -> {
                            CircleMemberDomainModel circleMemberDomainModel = this.circleMemberPort.findByUserIdAndCircleId(
                                    userDomainModel.getId(),
                                    circleDomainModel.getId()
                            ).orElseThrow(
                                    () -> new UnauthorizedException(
                                            ErrorCode.NOT_MEMBER,
                                            "로그인된 사용자가 동아리 멤버가 아닙니다."
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

        validatorBucket.validate();

        boolean isCircleLeader = false;
        if(userDomainModel.getRole().getValue().contains("LEADER_CIRCLE")){
            isCircleLeader = boardDomainModel.getCircle().get()
                .getLeader().map(UserDomainModel::getId).orElse("").equals(loginUserId);
        }

        if (isCircleLeader || userDomainModel.getRole().equals(Role.ADMIN) || userDomainModel.getRole().getValue().contains("PRESIDENT")) {
            return BoardPostsResponseDto.from(
                    boardDomainModel,
                    userDomainModel.getRole(),
                    this.favoriteBoardPort.findByUserId(loginUserId)
                            .stream()
                            .anyMatch(favoriteBoardDomainModel -> favoriteBoardDomainModel.getBoardDomainModel().getId().equals(boardDomainModel.getId())),
                    this.postPort.findAllPost(boardId, pageNum)
                            .map(postDomainModel -> PostsResponseDto.from(
                                    postDomainModel,
                                    this.commentPort.countByPostId(postDomainModel.getId())
                            ))
            );
        }
        else{
            return BoardPostsResponseDto.from(
                    boardDomainModel,
                    userDomainModel.getRole(),
                    this.favoriteBoardPort.findByUserId(loginUserId)
                            .stream()
                            .anyMatch(favoriteBoardDomainModel -> favoriteBoardDomainModel.getBoardDomainModel().getId().equals(boardDomainModel.getId())),
                    this.postPort.findAllPost(boardId, pageNum, false)
                            .map(postDomainModel -> PostsResponseDto.from(
                                    postDomainModel,
                                    this.commentPort.countByPostId(postDomainModel.getId())
                            ))
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
        ValidatorBucket validatorBucket = ValidatorBucket.of();

        UserDomainModel userDomainModel = this.userPort.findById(loginUserId).orElseThrow(
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
                .filter(circleDomainModel -> !userDomainModel.getRole().equals(Role.ADMIN) && !userDomainModel.getRole().getValue().contains("PRESIDENT"))
                .ifPresent(
                        circleDomainModel -> {
                            CircleMemberDomainModel circleMemberDomainModel = this.circleMemberPort.findByUserIdAndCircleId(
                                            userDomainModel.getId(),
                                            circleDomainModel.getId()
                                    )
                                    .orElseThrow(
                                            () -> new UnauthorizedException(
                                                    ErrorCode.NOT_MEMBER,
                                                    "로그인된 사용자가 동아리 멤버가 아닙니다."
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


        boolean isCircleLeader = false;
        if(userDomainModel.getRole().getValue().contains("LEADER_CIRCLE")){
            isCircleLeader = boardDomainModel.getCircle().get()
                    .getLeader().map(UserDomainModel::getId).orElse("").equals(loginUserId);
        }

        if (isCircleLeader || userDomainModel.getRole().equals(Role.ADMIN) || userDomainModel.getRole().getValue().contains("PRESIDENT")) {
            return BoardPostsResponseDto.from(
                    boardDomainModel,
                    userDomainModel.getRole(),
                    this.favoriteBoardPort.findByUserId(loginUserId)
                            .stream()
                            .anyMatch(favoriteBoardDomainModel -> favoriteBoardDomainModel.getBoardDomainModel().getId().equals(boardDomainModel.getId())),
                    this.postPort.searchPost(keyword, boardId, pageNum)
                            .map(postDomainModel -> PostsResponseDto.from(
                                    postDomainModel,
                                    this.commentPort.countByPostId(postDomainModel.getId())
                            ))
            );
        }
        else{
            return BoardPostsResponseDto.from(
                    boardDomainModel,
                    userDomainModel.getRole(),
                    this.favoriteBoardPort.findByUserId(loginUserId)
                            .stream()
                            .anyMatch(favoriteBoardDomainModel -> favoriteBoardDomainModel.getBoardDomainModel().getId().equals(boardDomainModel.getId())),
                    this.postPort.searchPost(keyword, boardId, pageNum, false)
                            .map(postDomainModel -> PostsResponseDto.from(
                                    postDomainModel,
                                    this.commentPort.countByPostId(postDomainModel.getId())
                            ))
            );
        }


    }

    @Transactional(readOnly = true)
    public BoardPostsResponseDto findAllAppNotice(String loginUserId, Integer pageNum) {
        UserDomainModel userDomainModel = this.userPort.findById(loginUserId).orElseThrow(
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
                this.favoriteBoardPort.findByUserId(loginUserId)
                        .stream()
                        .anyMatch(favoriteBoardDomainModel -> favoriteBoardDomainModel.getBoardDomainModel().getId().equals(boardDomainModel.getId())),
                this.postPort.findAllPost(boardDomainModel.getId(), pageNum)
                        .map(postDomainModel -> PostsResponseDto.from(
                                postDomainModel,
                                this.commentPort.countByPostId(postDomainModel.getId())
                        ))
        );
    }

    @Transactional
    public PostResponseDto createPost(String loginUserId, PostCreateRequestDto postCreateRequestDto) {
        ValidatorBucket validatorBucket = ValidatorBucket.of();

        UserDomainModel creatorDomainModel = this.userPort.findById(loginUserId).orElseThrow(
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
                .filter(circleDomainModel -> !creatorDomainModel.getRole().equals(Role.ADMIN) && !creatorDomainModel.getRole().getValue().contains("PRESIDENT"))
                .ifPresent(
                        circleDomainModel -> {
                            CircleMemberDomainModel circleMemberDomainModel = this.circleMemberPort.findByUserIdAndCircleId(
                                    loginUserId,
                                    circleDomainModel.getId()
                            ).orElseThrow(
                                    () -> new UnauthorizedException(
                                            ErrorCode.NOT_MEMBER,
                                            "로그인된 사용자가 동아리 멤버가 아닙니다."
                                    )
                            );

                            validatorBucket
                                    .consistOf(TargetIsDeletedValidator.of(circleDomainModel.getIsDeleted(), StaticValue.DOMAIN_CIRCLE))
                                    .consistOf(CircleMemberStatusValidator.of(
                                            circleMemberDomainModel.getStatus(),
                                            List.of(CircleMemberStatus.MEMBER)
                                    ));

                            if (creatorDomainModel.getRole().getValue().contains("LEADER_CIRCLE") && !boardDomainModel.getCreateRoleList().contains("COMMON")) {
                                validatorBucket
                                        .consistOf(UserEqualValidator.of(
                                                circleDomainModel.getLeader().map(UserDomainModel::getId).orElseThrow(
                                                        () -> new UnauthorizedException(
                                                                ErrorCode.API_NOT_ALLOWED,
                                                                "사용자가 해당 동아리의 동아리장이 아닙니다."
                                                        )
                                                ),
                                                loginUserId
                                        ));
                            }
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
                this.postPort.createPost(postDomainModel),
                creatorDomainModel
        );
    }

    @Transactional
    public PostResponseDto deletePost(String loginUserId, String postId) {
        ValidatorBucket validatorBucket = ValidatorBucket.of();

        UserDomainModel deleterDomainModel = this.userPort.findById(loginUserId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "로그인된 사용자를 찾을 수 없습니다."
                )
        );

        PostDomainModel postDomainModel = this.postPort.findPostById(postId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "게시글을 찾을 수 없습니다."
                )
        );

        if (postDomainModel.getBoard().getCategory().equals(StaticValue.BOARD_NAME_APP_NOTICE)) {
            validatorBucket
                    .consistOf(UserRoleValidator.of(
                            deleterDomainModel.getRole(),
                            List.of()
                    ));
        }

        validatorBucket
                .consistOf(UserStateValidator.of(deleterDomainModel.getState()))
                .consistOf(UserRoleIsNoneValidator.of(deleterDomainModel.getRole()))
                .consistOf(TargetIsDeletedValidator.of(postDomainModel.getIsDeleted(), StaticValue.DOMAIN_POST));

        postDomainModel.getBoard().getCircle()
                .filter(circleDomainModel -> !deleterDomainModel.getRole().equals(Role.ADMIN) && !deleterDomainModel.getRole().getValue().contains("PRESIDENT"))
                .ifPresent(
                        circleDomainModel -> {
                            CircleMemberDomainModel circleMemberDomainModel = this.circleMemberPort.findByUserIdAndCircleId(
                                    deleterDomainModel.getId(),
                                    circleDomainModel.getId()
                            ).orElseThrow(
                                    () -> new UnauthorizedException(
                                            ErrorCode.NOT_MEMBER,
                                            "로그인된 사용자가 동아리 멤버가 아닙니다."
                                    )
                            );

                            validatorBucket
                                    .consistOf(TargetIsDeletedValidator.of(circleDomainModel.getIsDeleted(), StaticValue.DOMAIN_CIRCLE))
                                    .consistOf(CircleMemberStatusValidator.of(
                                            circleMemberDomainModel.getStatus(),
                                            List.of(CircleMemberStatus.MEMBER)
                                    ));

                            if (deleterDomainModel.getRole().getValue().contains("LEADER_CIRCLE") && !postDomainModel.getWriter().getId().equals(loginUserId)) {
                                validatorBucket
                                        .consistOf(UserEqualValidator.of(
                                                circleDomainModel.getLeader().map(UserDomainModel::getId).orElseThrow(
                                                        () -> new UnauthorizedException(
                                                                ErrorCode.API_NOT_ALLOWED,
                                                                "사용자가 해당 동아리의 동아리장이 아닙니다."
                                                        )
                                                ),
                                                loginUserId
                                        ));
                            }
                        }
                );

        validatorBucket
                .consistOf(ContentsAdminValidator.of(
                        deleterDomainModel.getRole(),
                        loginUserId,
                        postDomainModel.getWriter().getId(),
                        List.of(Role.LEADER_CIRCLE,
                                Role.VICE_PRESIDENT_N_LEADER_CIRCLE,
                                Role.COUNCIL_N_LEADER_CIRCLE,
                                Role.LEADER_1_N_LEADER_CIRCLE,
                                Role.LEADER_2_N_LEADER_CIRCLE,
                                Role.LEADER_3_N_LEADER_CIRCLE,
                                Role.LEADER_4_N_LEADER_CIRCLE
                        ))
                )
                .validate();

        return PostResponseDto.from(
                this.postPort.deletePost(postId).orElseThrow(
                        () -> new InternalServerException(
                                ErrorCode.INTERNAL_SERVER,
                                "Post id checked, but exception occurred"
                        )
                ),
                deleterDomainModel
        );
    }

    @Transactional
    public PostResponseDto updatePost(
            String loginUserId,
            String postId,
            PostUpdateRequestDto postUpdateRequestDto
    ) {
        ValidatorBucket validatorBucket = ValidatorBucket.of();

        UserDomainModel updaterDomainModel = this.userPort.findById(loginUserId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "로그인된 사용자를 찾을 수 없습니다."
                )
        );

        PostDomainModel postDomainModel = this.postPort.findPostById(postId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "게시글을 찾을 수 없습니다."
                )
        );

        if (postDomainModel.getBoard().getCategory().equals(StaticValue.BOARD_NAME_APP_NOTICE)) {
            validatorBucket
                    .consistOf(UserRoleValidator.of(
                            updaterDomainModel.getRole(),
                            List.of()
                    ));
        }

        validatorBucket
                .consistOf(UserStateValidator.of(updaterDomainModel.getState()))
                .consistOf(UserRoleIsNoneValidator.of(updaterDomainModel.getRole()))
                .consistOf(PostNumberOfAttachmentsValidator.of(postUpdateRequestDto.getAttachmentList()))
                .consistOf(TargetIsDeletedValidator.of(postDomainModel.getBoard().getIsDeleted(), StaticValue.DOMAIN_BOARD))
                .consistOf(TargetIsDeletedValidator.of(postDomainModel.getIsDeleted(), StaticValue.DOMAIN_POST));

        postDomainModel.getBoard().getCircle()
                .filter(circleDomainModel -> !updaterDomainModel.getRole().equals(Role.ADMIN) && !updaterDomainModel.getRole().getValue().contains("PRESIDENT"))
                .ifPresent(
                        circleDomainModel -> {
                            CircleMemberDomainModel circleMemberDomainModel = this.circleMemberPort.findByUserIdAndCircleId(
                                    loginUserId,
                                    circleDomainModel.getId()
                            ).orElseThrow(
                                    () -> new UnauthorizedException(
                                            ErrorCode.NOT_MEMBER,
                                            "로그인된 사용자가 동아리 멤버가 아닙니다."
                                    )
                            );

                            validatorBucket
                                    .consistOf(TargetIsDeletedValidator.of(circleDomainModel.getIsDeleted(), StaticValue.DOMAIN_CIRCLE))
                                    .consistOf(CircleMemberStatusValidator.of(
                                            circleMemberDomainModel.getStatus(),
                                            List.of(CircleMemberStatus.MEMBER)
                                    ));

                            if (updaterDomainModel.getRole().getValue().contains("LEADER_CIRCLE") && !postDomainModel.getWriter().getId().equals(loginUserId)) {
                                validatorBucket
                                        .consistOf(UserEqualValidator.of(
                                                circleDomainModel.getLeader().map(UserDomainModel::getId).orElseThrow(
                                                        () -> new UnauthorizedException(
                                                                ErrorCode.API_NOT_ALLOWED,
                                                                "사용자가 해당 동아리의 동아리장이 아닙니다."
                                                        )
                                                ),
                                                loginUserId
                                        ));
                            }
                        }
                );

        postDomainModel.update(
                postUpdateRequestDto.getTitle(),
                postUpdateRequestDto.getContent(),
                postUpdateRequestDto.getAttachmentList()
        );

        validatorBucket
                .consistOf(ContentsAdminValidator.of(
                        updaterDomainModel.getRole(),
                        loginUserId,
                        postDomainModel.getWriter().getId(),
                        List.of(Role.LEADER_CIRCLE,
                                Role.VICE_PRESIDENT_N_LEADER_CIRCLE,
                                Role.COUNCIL_N_LEADER_CIRCLE,
                                Role.LEADER_1_N_LEADER_CIRCLE,
                                Role.LEADER_2_N_LEADER_CIRCLE,
                                Role.LEADER_3_N_LEADER_CIRCLE,
                                Role.LEADER_4_N_LEADER_CIRCLE
                        )
                ))
                .consistOf(ConstraintValidator.of(postDomainModel, this.validator))
                .validate();

        PostDomainModel updatedPostDomainModel = this.postPort.updatePost(postId, postDomainModel).orElseThrow(
                () -> new InternalServerException(
                        ErrorCode.INTERNAL_SERVER,
                        "Post id checked, but exception occurred"
                )
        );

        return PostResponseDto.from(
                postDomainModel,
                updaterDomainModel,
                this.commentPort.findByPostId(postId, 0)
                        .map(commentDomainModel -> CommentResponseDto.from(
                                commentDomainModel,
                                updaterDomainModel,
                                updatedPostDomainModel.getBoard(),
                                this.childCommentPort.countByParentComment(commentDomainModel.getId())
                        )),
                this.commentPort.countByPostId(postDomainModel.getId())
        );
    }

    public PostResponseDto restorePost(String loginUserId, String postId) {

        ValidatorBucket validatorBucket = ValidatorBucket.of();

        UserDomainModel restorerDomainModel = this.userPort.findById(loginUserId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "로그인된 사용자를 찾을 수 없습니다."
                )
        );

        PostDomainModel postDomainModel = this.postPort.findPostById(postId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "게시글을 찾을 수 없습니다."
                )
        );

        if (postDomainModel.getBoard().getCategory().equals(StaticValue.BOARD_NAME_APP_NOTICE)) {
            validatorBucket
                    .consistOf(UserRoleValidator.of(
                            restorerDomainModel.getRole(),
                            List.of()
                    ));
        }

        validatorBucket
                .consistOf(UserStateValidator.of(restorerDomainModel.getState()))
                .consistOf(UserRoleIsNoneValidator.of(restorerDomainModel.getRole()))
                .consistOf(TargetIsDeletedValidator.of(postDomainModel.getBoard().getIsDeleted(), StaticValue.DOMAIN_BOARD))
                .consistOf(TargetIsNotDeletedValidator.of(postDomainModel.getIsDeleted(), StaticValue.DOMAIN_POST));

        postDomainModel.getBoard().getCircle()
                .filter(circleDomainModel -> !restorerDomainModel.getRole().equals(Role.ADMIN) && !restorerDomainModel.getRole().getValue().contains("PRESIDENT"))
                .ifPresent(
                        circleDomainModel -> {
                            CircleMemberDomainModel circleMemberDomainModel = this.circleMemberPort.findByUserIdAndCircleId(
                                    loginUserId,
                                    circleDomainModel.getId()
                            ).orElseThrow(
                                    () -> new UnauthorizedException(
                                            ErrorCode.NOT_MEMBER,
                                            "로그인된 사용자가 동아리 멤버가 아닙니다."
                                    )
                            );

                            validatorBucket
                                    .consistOf(TargetIsDeletedValidator.of(circleDomainModel.getIsDeleted(), StaticValue.DOMAIN_CIRCLE))
                                    .consistOf(CircleMemberStatusValidator.of(
                                            circleMemberDomainModel.getStatus(),
                                            List.of(CircleMemberStatus.MEMBER)
                                    ));

                            if (restorerDomainModel.getRole().getValue().contains("LEADER_CIRCLE") && !postDomainModel.getWriter().getId().equals(loginUserId)) {
                                validatorBucket
                                        .consistOf(UserEqualValidator.of(
                                                circleDomainModel.getLeader().map(UserDomainModel::getId).orElseThrow(
                                                        () -> new UnauthorizedException(
                                                                ErrorCode.API_NOT_ALLOWED,
                                                                "사용자가 해당 동아리의 동아리장이 아닙니다."
                                                        )
                                                ),
                                                loginUserId
                                        ));
                            }
                        }
                );

        validatorBucket
                .consistOf(ContentsAdminValidator.of(
                        restorerDomainModel.getRole(),
                        loginUserId,
                        postDomainModel.getWriter().getId(),
                        List.of(Role.LEADER_CIRCLE,
                                Role.VICE_PRESIDENT_N_LEADER_CIRCLE,
                                Role.COUNCIL_N_LEADER_CIRCLE,
                                Role.LEADER_1_N_LEADER_CIRCLE,
                                Role.LEADER_2_N_LEADER_CIRCLE,
                                Role.LEADER_3_N_LEADER_CIRCLE,
                                Role.LEADER_4_N_LEADER_CIRCLE
                        )
                ))
                .validate();

        PostDomainModel restoredPostDomainModel = this.postPort.restorePost(postId, postDomainModel).orElseThrow(
                () -> new InternalServerException(
                        ErrorCode.INTERNAL_SERVER,
                        "Post id checked, but exception occurred"
                )
        );

        return PostResponseDto.from(
                postDomainModel,
                restorerDomainModel,
                this.commentPort.findByPostId(postId, 0)
                        .map(commentDomainModel -> CommentResponseDto.from(
                                commentDomainModel,
                                restorerDomainModel,
                                restoredPostDomainModel.getBoard(),
                                this.childCommentPort.countByParentComment(commentDomainModel.getId())
                        )),
                this.commentPort.countByPostId(postDomainModel.getId())
        );
    }
}
