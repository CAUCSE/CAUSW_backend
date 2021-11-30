package net.causw.application;

import net.causw.application.dto.CommentResponseDto;
import net.causw.application.dto.PostAllResponseDto;
import net.causw.application.dto.PostAllWithBoardResponseDto;
import net.causw.application.dto.PostCreateRequestDto;
import net.causw.application.dto.PostResponseDto;
import net.causw.application.dto.PostUpdateRequestDto;
import net.causw.application.spi.BoardPort;
import net.causw.application.spi.CircleMemberPort;
import net.causw.application.spi.CommentPort;
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
import net.causw.domain.model.UserDomainModel;
import net.causw.domain.validation.CircleMemberStatusValidator;
import net.causw.domain.validation.ConstraintValidator;
import net.causw.domain.validation.ContentsAdminValidator;
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
import java.util.stream.Collectors;

@Service
public class PostService {
    private final PostPort postPort;
    private final UserPort userPort;
    private final BoardPort boardPort;
    private final CircleMemberPort circleMemberPort;
    private final CommentPort commentPort;
    private final Validator validator;

    public PostService(
            PostPort postPort,
            UserPort userPort,
            BoardPort boardPort,
            CircleMemberPort circleMemberPort,
            CommentPort commentPort,
            Validator validator
    ) {
        this.postPort = postPort;
        this.userPort = userPort;
        this.boardPort = boardPort;
        this.circleMemberPort = circleMemberPort;
        this.commentPort = commentPort;
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
                .consistOf(TargetIsDeletedValidator.of(postDomainModel.getBoard().getIsDeleted(), postDomainModel.getBoard().getDOMAIN()))
                .consistOf(TargetIsDeletedValidator.of(postDomainModel.getIsDeleted(), postDomainModel.getDOMAIN()));

        postDomainModel.getBoard().getCircle().ifPresent(
                circleDomainModel -> {
                    CircleMemberDomainModel circleMemberDomainModel = this.circleMemberPort.findByUserIdAndCircleId(userDomainModel.getId(), circleDomainModel.getId())
                            .orElseThrow(
                                    () -> new UnauthorizedException(
                                            ErrorCode.NOT_MEMBER,
                                            "로그인된 사용자가 소모임 멤버가 아닙니다."
                                    )
                            );

                    validatorBucket
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
                                        commentDomainModel, userDomainModel, postDomainModel.getBoard())
                        ),
                this.commentPort.countByPostId(postDomainModel.getId())
        );
    }

    @Transactional(readOnly = true)
    public PostAllWithBoardResponseDto findAll(
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

        boardDomainModel.getCircle().ifPresent(
                circleDomainModel -> {
                    CircleMemberDomainModel circleMemberDomainModel = this.circleMemberPort.findByUserIdAndCircleId(userDomainModel.getId(), circleDomainModel.getId())
                            .orElseThrow(
                                    () -> new UnauthorizedException(
                                            ErrorCode.NOT_MEMBER,
                                            "로그인된 사용자가 소모임 멤버가 아닙니다."
                                    )
                            );

                    validatorBucket
                            .consistOf(TargetIsDeletedValidator.of(circleDomainModel.getIsDeleted(), circleDomainModel.getDOMAIN()))
                            .consistOf(CircleMemberStatusValidator.of(
                                    circleMemberDomainModel.getStatus(),
                                    List.of(CircleMemberStatus.MEMBER)
                            ));
                }
        );

        validatorBucket
                .consistOf(TargetIsDeletedValidator.of(boardDomainModel.getIsDeleted(), boardDomainModel.getDOMAIN()))
                .validate();

        return PostAllWithBoardResponseDto.from(
                boardDomainModel,
                userDomainModel.getRole(),
                this.postPort.findAll(boardId, pageNum)
                        .map(postDomainModel -> PostAllResponseDto.from(
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

        PostDomainModel postDomainModel = PostDomainModel.of(
                postCreateRequestDto.getTitle(),
                postCreateRequestDto.getContent(),
                creatorDomainModel,
                boardDomainModel
        );

        validatorBucket
                .consistOf(UserStateValidator.of(creatorDomainModel.getState()))
                .consistOf(UserRoleIsNoneValidator.of(creatorDomainModel.getRole()))
                .consistOf(TargetIsDeletedValidator.of(boardDomainModel.getIsDeleted(), boardDomainModel.getDOMAIN()))
                .consistOf(UserRoleValidator.of(
                        creatorDomainModel.getRole(),
                        boardDomainModel.getCreateRoleList()
                                .stream()
                                .map(Role::of)
                                .collect(Collectors.toList())
                ));

        boardDomainModel.getCircle().ifPresent(
                circleDomainModel -> {
                    CircleMemberDomainModel circleMemberDomainModel = this.circleMemberPort.findByUserIdAndCircleId(requestUserId, circleDomainModel.getId()).orElseThrow(
                            () -> new UnauthorizedException(
                                    ErrorCode.NOT_MEMBER,
                                    "로그인된 사용자가 소모임 멤버가 아닙니다."
                            )
                    );

                    validatorBucket
                            .consistOf(TargetIsDeletedValidator.of(circleDomainModel.getIsDeleted(), circleDomainModel.getDOMAIN()))
                            .consistOf(CircleMemberStatusValidator.of(
                                    circleMemberDomainModel.getStatus(),
                                    List.of(CircleMemberStatus.MEMBER)
                            ));
                }
        );

        validatorBucket
                .consistOf(ConstraintValidator.of(postDomainModel, this.validator))
                .validate();

        return PostResponseDto.from(this.postPort.create(postDomainModel), creatorDomainModel);
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

        validatorBucket
                .consistOf(UserStateValidator.of(requestUser.getState()))
                .consistOf(UserRoleIsNoneValidator.of(requestUser.getRole()))
                .consistOf(TargetIsDeletedValidator.of(postDomainModel.getIsDeleted(), postDomainModel.getDOMAIN()));

        postDomainModel.getBoard().getCircle().ifPresentOrElse(
                circleDomainModel -> {
                    CircleMemberDomainModel circleMemberDomainModel = this.circleMemberPort.findByUserIdAndCircleId(requestUser.getId(), circleDomainModel.getId())
                            .orElseThrow(
                                    () -> new UnauthorizedException(
                                            ErrorCode.NOT_MEMBER,
                                            "로그인된 사용자가 소모임 멤버가 아닙니다."
                                    )
                            );

                    validatorBucket
                            .consistOf(TargetIsDeletedValidator.of(circleDomainModel.getIsDeleted(), circleDomainModel.getDOMAIN()))
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
                )
                , requestUser
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

        validatorBucket
                .consistOf(UserStateValidator.of(requestUser.getState()))
                .consistOf(UserRoleIsNoneValidator.of(requestUser.getRole()))
                .consistOf(TargetIsDeletedValidator.of(postDomainModel.getBoard().getIsDeleted(), postDomainModel.getBoard().getDOMAIN()))
                .consistOf(TargetIsDeletedValidator.of(postDomainModel.getIsDeleted(), postDomainModel.getDOMAIN()));

        postDomainModel.getBoard().getCircle().ifPresent(
                circleDomainModel -> {
                    CircleMemberDomainModel circleMemberDomainModel = this.circleMemberPort.findByUserIdAndCircleId(requestUserId, circleDomainModel.getId()).orElseThrow(
                            () -> new UnauthorizedException(
                                    ErrorCode.NOT_MEMBER,
                                    "로그인된 사용자가 소모임 멤버가 아닙니다."
                            )
                    );

                    validatorBucket
                            .consistOf(TargetIsDeletedValidator.of(circleDomainModel.getIsDeleted(), circleDomainModel.getDOMAIN()))
                            .consistOf(CircleMemberStatusValidator.of(
                                    circleMemberDomainModel.getStatus(),
                                    List.of(CircleMemberStatus.MEMBER)
                            ));
                }
        );

        postDomainModel = PostDomainModel.of(
                postDomainModel.getId(),
                postUpdateRequestDto.getTitle(),
                postUpdateRequestDto.getContent(),
                postDomainModel.getWriter(),
                postDomainModel.getIsDeleted(),
                postDomainModel.getBoard(),
                postDomainModel.getCreatedAt(),
                postDomainModel.getUpdatedAt()
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
                        .map(
                                commentDomainModel -> CommentResponseDto.from(
                                        commentDomainModel, requestUser, updatedPostDomainModel.getBoard())
                        ),
                this.commentPort.countByPostId(postDomainModel.getId())
        );
    }
}
