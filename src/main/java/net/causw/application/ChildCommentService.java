package net.causw.application;

import net.causw.application.dto.ChildCommentAllResponseDto;
import net.causw.application.dto.ChildCommentCreateRequestDto;
import net.causw.application.dto.ChildCommentResponseDto;
import net.causw.application.dto.ChildCommentUpdateRequestDto;
import net.causw.application.dto.CommentResponseDto;
import net.causw.application.spi.ChildCommentPort;
import net.causw.application.spi.CircleMemberPort;
import net.causw.application.spi.CommentPort;
import net.causw.application.spi.PostPort;
import net.causw.application.spi.UserPort;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.exceptions.InternalServerException;
import net.causw.domain.exceptions.UnauthorizedException;
import net.causw.domain.model.ChildCommentDomainModel;
import net.causw.domain.model.CircleMemberDomainModel;
import net.causw.domain.model.CircleMemberStatus;
import net.causw.domain.model.CommentDomainModel;
import net.causw.domain.model.PostDomainModel;
import net.causw.domain.model.Role;
import net.causw.domain.model.StaticValue;
import net.causw.domain.model.UserDomainModel;
import net.causw.domain.validation.CircleMemberStatusValidator;
import net.causw.domain.validation.ConstraintValidator;
import net.causw.domain.validation.ContentsAdminValidator;
import net.causw.domain.validation.TargetIsDeletedValidator;
import net.causw.domain.validation.UserEqualValidator;
import net.causw.domain.validation.UserNameEqualValidator;
import net.causw.domain.validation.UserRoleIsNoneValidator;
import net.causw.domain.validation.UserStateValidator;
import net.causw.domain.validation.ValidatorBucket;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.Validator;
import java.util.List;
import java.util.Optional;

@Service
public class ChildCommentService {
    private final ChildCommentPort childCommentPort;
    private final CommentPort commentPort;
    private final UserPort userPort;
    private final CircleMemberPort circleMemberPort;
    private final PostPort postPort;
    private final Validator validator;

    public ChildCommentService(
            ChildCommentPort childCommentPort,
            CommentPort commentPort,
            UserPort userPort,
            CircleMemberPort circleMemberPort,
            PostPort postPort,
            Validator validator
    ) {
        this.childCommentPort = childCommentPort;
        this.commentPort = commentPort;
        this.userPort = userPort;
        this.circleMemberPort = circleMemberPort;
        this.postPort = postPort;
        this.validator = validator;
    }

    @Transactional
    public ChildCommentResponseDto create(String creatorId, ChildCommentCreateRequestDto childCommentCreateRequestDto) {
        ValidatorBucket validatorBucket = ValidatorBucket.of();

        UserDomainModel creatorDomainModel = this.userPort.findById(creatorId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "로그인된 사용자를 찾을 수 없습니다."
                )
        );

        validatorBucket
                .consistOf(UserStateValidator.of(creatorDomainModel.getState()))
                .consistOf(UserRoleIsNoneValidator.of(creatorDomainModel.getRole()));

        Optional<ChildCommentDomainModel> refChildCommentDomainModel = childCommentCreateRequestDto.getRefChildComment().map(
                refChildCommentId -> this.childCommentPort.findById(refChildCommentId).orElseThrow(
                        () -> new BadRequestException(
                                ErrorCode.ROW_DOES_NOT_EXIST,
                                "답할 답글을 찾을 수 없습니다."
                        )
                )
        );

        CommentDomainModel parentCommentDomainModel = this.commentPort.findById(childCommentCreateRequestDto.getParentCommentId()).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "상위 댓글을 찾을 수 없습니다."
                )
        );

        PostDomainModel postDomainModel = this.postPort.findById(parentCommentDomainModel.getPostId()).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "게시글을 찾을 수 없습니다."
                )
        );

        ChildCommentDomainModel childCommentDomainModel = ChildCommentDomainModel.of(
                childCommentCreateRequestDto.getContent(),
                refChildCommentDomainModel.map(refChildComment -> refChildComment.getWriter().getName()).orElse(null),
                childCommentCreateRequestDto.getRefChildComment().orElse(null),
                creatorDomainModel,
                parentCommentDomainModel
        );

        validatorBucket
                .consistOf(UserStateValidator.of(creatorDomainModel.getState()))
                .consistOf(UserRoleIsNoneValidator.of(creatorDomainModel.getRole()))
                .consistOf(TargetIsDeletedValidator.of(postDomainModel.getBoard().getIsDeleted(), StaticValue.DOMAIN_BOARD))
                .consistOf(TargetIsDeletedValidator.of(postDomainModel.getIsDeleted(), StaticValue.DOMAIN_POST))
                .consistOf(ConstraintValidator.of(childCommentDomainModel, this.validator));

        refChildCommentDomainModel.ifPresent(
                refChildComment -> validatorBucket
                        .consistOf(TargetIsDeletedValidator.of(refChildComment.getIsDeleted(), StaticValue.DOMAIN_CHILD_COMMENT))
        );

        postDomainModel.getBoard().getCircle().ifPresent(
                circleDomainModel -> {
                    CircleMemberDomainModel circleMemberDomainModel = this.circleMemberPort.findByUserIdAndCircleId(creatorId, circleDomainModel.getId()).orElseThrow(
                            () -> new UnauthorizedException(
                                    ErrorCode.NOT_MEMBER,
                                    "로그인된 사용자가 가입 신청한 소모임이 아닙니다."
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

        return ChildCommentResponseDto.from(
                this.childCommentPort.create(childCommentDomainModel, postDomainModel),
                creatorDomainModel,
                postDomainModel.getBoard()
        );
    }

    @Transactional(readOnly = true)
    public ChildCommentAllResponseDto findAll(String userId, String parentCommentId, Integer pageNum) {
        ValidatorBucket validatorBucket = ValidatorBucket.of();

        UserDomainModel userDomainModel = this.userPort.findById(userId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "로그인된 사용자를 찾을 수 없습니다."
                )
        );

        CommentDomainModel parentCommentDomainModel = this.commentPort.findById(parentCommentId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "상위 댓글을 찾을 수 없습니다."
                )
        );

        PostDomainModel postDomainModel = this.postPort.findById(parentCommentDomainModel.getPostId()).orElseThrow(
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

        postDomainModel.getBoard().getCircle().ifPresent(
                circleDomainModel -> {
                    CircleMemberDomainModel circleMemberDomainModel = this.circleMemberPort.findByUserIdAndCircleId(userId, circleDomainModel.getId()).orElseThrow(
                            () -> new UnauthorizedException(
                                    ErrorCode.NOT_MEMBER,
                                    "로그인된 사용자가 가입 신청한 소모임이 아닙니다."
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

        return ChildCommentAllResponseDto.from(
                CommentResponseDto.from(
                        parentCommentDomainModel,
                        userDomainModel,
                        postDomainModel.getBoard(),
                        this.childCommentPort.countByParentComment(parentCommentDomainModel.getId())
                ),
                this.childCommentPort.findByParentComment(parentCommentId, pageNum)
                        .map(childCommentDomainModel ->
                                ChildCommentResponseDto.from(
                                        childCommentDomainModel,
                                        userDomainModel,
                                        postDomainModel.getBoard()
                                )
                        )
        );
    }

    @Transactional
    public ChildCommentResponseDto update(
            String updaterId,
            String childCommentId,
            ChildCommentUpdateRequestDto childCommentUpdateRequestDto
    ) {
        ValidatorBucket validatorBucket = ValidatorBucket.of();

        UserDomainModel updater = this.userPort.findById(updaterId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "로그인된 사용자를 찾을 수 없습니다."
                )
        );

        ChildCommentDomainModel childCommentDomainModel = this.childCommentPort.findById(childCommentId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "수정할 답글을 찾을 수 없습니다."
                )
        );

        PostDomainModel postDomainModel = this.postPort.findById(childCommentDomainModel.getParentComment().getPostId()).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "게시글을 찾을 수 없습니다."
                )
        );

        childCommentDomainModel.update(
                childCommentUpdateRequestDto.getContent()
        );

        validatorBucket
                .consistOf(UserStateValidator.of(updater.getState()))
                .consistOf(UserRoleIsNoneValidator.of(updater.getRole()))
                .consistOf(TargetIsDeletedValidator.of(postDomainModel.getBoard().getIsDeleted(), StaticValue.DOMAIN_BOARD))
                .consistOf(TargetIsDeletedValidator.of(postDomainModel.getIsDeleted(), StaticValue.DOMAIN_POST))
                .consistOf(TargetIsDeletedValidator.of(childCommentDomainModel.getIsDeleted(), StaticValue.DOMAIN_CHILD_COMMENT))
                .consistOf(ConstraintValidator.of(childCommentDomainModel, this.validator))
                .consistOf(ContentsAdminValidator.of(
                        updater.getRole(),
                        updaterId,
                        childCommentDomainModel.getWriter().getId(),
                        List.of()
                ));

        postDomainModel.getBoard().getCircle().ifPresent(
                circleDomainModel -> {
                    CircleMemberDomainModel circleMemberDomainModel = this.circleMemberPort.findByUserIdAndCircleId(updaterId, circleDomainModel.getId()).orElseThrow(
                            () -> new UnauthorizedException(
                                    ErrorCode.NOT_MEMBER,
                                    "사용자가 가입 신청한 소모임이 아닙니다."
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

        return ChildCommentResponseDto.from(
                this.childCommentPort.update(childCommentId, childCommentDomainModel).orElseThrow(
                        () -> new InternalServerException(
                                ErrorCode.INTERNAL_SERVER,
                                "Comment id checked, but exception occurred"
                        )
                ),
                updater,
                postDomainModel.getBoard()
        );
    }

    @Transactional
    public ChildCommentResponseDto delete(String deleterId, String childCommentId) {
        ValidatorBucket validatorBucket = ValidatorBucket.of();

        UserDomainModel deleterDomainModel = this.userPort.findById(deleterId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "로그인된 사용자를 찾을 수 없습니다."
                )
        );

        ChildCommentDomainModel childCommentDomainModel = this.childCommentPort.findById(childCommentId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "삭제할 답글을 찾을 수 없습니다."
                )
        );

        PostDomainModel postDomainModel = this.postPort.findById(childCommentDomainModel.getParentComment().getPostId()).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "게시글을 찾을 수 없습니다."
                )
        );

        validatorBucket
                .consistOf(UserStateValidator.of(deleterDomainModel.getState()))
                .consistOf(UserRoleIsNoneValidator.of(deleterDomainModel.getRole()))
                .consistOf(TargetIsDeletedValidator.of(childCommentDomainModel.getIsDeleted(), StaticValue.DOMAIN_CHILD_COMMENT));

        postDomainModel.getBoard().getCircle().ifPresentOrElse(
                circleDomainModel -> {
                    CircleMemberDomainModel circleMemberDomainModel = this.circleMemberPort.findByUserIdAndCircleId(deleterId, circleDomainModel.getId()).orElseThrow(
                            () -> new UnauthorizedException(
                                    ErrorCode.NOT_MEMBER,
                                    "로그인된 사용자가 가입 신청한 소모임이 아닙니다."
                            )
                    );

                    validatorBucket
                            .consistOf(TargetIsDeletedValidator.of(circleDomainModel.getIsDeleted(), StaticValue.DOMAIN_CIRCLE))
                            .consistOf(CircleMemberStatusValidator.of(
                                    circleMemberDomainModel.getStatus(),
                                    List.of(CircleMemberStatus.MEMBER)
                            ))
                            .consistOf(ContentsAdminValidator.of(
                                    deleterDomainModel.getRole(),
                                    deleterId,
                                    childCommentDomainModel.getWriter().getId(),
                                    List.of(Role.LEADER_CIRCLE)
                            ));

                    if (deleterDomainModel.getRole().equals(Role.LEADER_CIRCLE)) {
                        validatorBucket
                                .consistOf(UserEqualValidator.of(
                                        circleDomainModel.getLeader().map(UserDomainModel::getId).orElseThrow(
                                                () -> new InternalServerException(
                                                        ErrorCode.INTERNAL_SERVER,
                                                        "The board has circle without circle leader"
                                                )
                                        ),
                                        deleterId
                                ));
                    }
                },
                () -> validatorBucket
                        .consistOf(ContentsAdminValidator.of(
                                deleterDomainModel.getRole(),
                                deleterId,
                                childCommentDomainModel.getWriter().getId(),
                                List.of(Role.PRESIDENT)
                        ))

        );

        validatorBucket
                .validate();

        return ChildCommentResponseDto.from(
                this.childCommentPort.delete(childCommentId).orElseThrow(
                        () -> new InternalServerException(
                                ErrorCode.INTERNAL_SERVER,
                                "Comment id checked, but exception occurred"
                        )
                ),
                deleterDomainModel,
                postDomainModel.getBoard()
        );
    }
}
