package net.causw.application.comment;

import lombok.RequiredArgsConstructor;
import net.causw.application.dto.comment.CommentCreateRequestDto;
import net.causw.application.dto.comment.CommentResponseDto;
import net.causw.application.dto.comment.CommentUpdateRequestDto;
import net.causw.application.spi.ChildCommentPort;
import net.causw.application.spi.CircleMemberPort;
import net.causw.application.spi.CommentPort;
import net.causw.application.spi.PostPort;
import net.causw.application.spi.UserPort;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.exceptions.InternalServerException;
import net.causw.domain.exceptions.UnauthorizedException;
import net.causw.domain.model.circle.CircleMemberDomainModel;
import net.causw.domain.model.enums.CircleMemberStatus;
import net.causw.domain.model.comment.CommentDomainModel;
import net.causw.domain.model.post.PostDomainModel;
import net.causw.domain.model.enums.Role;
import net.causw.domain.model.util.StaticValue;
import net.causw.domain.model.user.UserDomainModel;
import net.causw.domain.validation.CircleMemberStatusValidator;
import net.causw.domain.validation.ConstraintValidator;
import net.causw.domain.validation.ContentsAdminValidator;
import net.causw.domain.validation.TargetIsDeletedValidator;
import net.causw.domain.validation.UserEqualValidator;
import net.causw.domain.validation.UserRoleIsNoneValidator;
import net.causw.domain.validation.UserStateValidator;
import net.causw.domain.validation.ValidatorBucket;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.Validator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentPort commentPort;
    private final UserPort userPort;
    private final PostPort postPort;
    private final CircleMemberPort circleMemberPort;
    private final ChildCommentPort childCommentPort;
    private final Validator validator;

    @Transactional
    public CommentResponseDto createComment(String loginUserId, CommentCreateRequestDto commentCreateDto) {
        ValidatorBucket validatorBucket = ValidatorBucket.of();

        UserDomainModel creatorDomainModel = this.userPort.findById(loginUserId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "로그인된 사용자를 찾을 수 없습니다."
                )
        );

        validatorBucket
                .consistOf(UserStateValidator.of(creatorDomainModel.getState()))
                .consistOf(UserRoleIsNoneValidator.of(creatorDomainModel.getRole()));

        PostDomainModel postDomainModel = this.postPort.findPostById(commentCreateDto.getPostId()).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "게시글을 찾을 수 없습니다."
                )
        );

        CommentDomainModel commentDomainModel = CommentDomainModel.of(
                commentCreateDto.getContent(),
                creatorDomainModel,
                postDomainModel.getId()
        );

        validatorBucket
                .consistOf(TargetIsDeletedValidator.of(postDomainModel.getBoard().getIsDeleted(), StaticValue.DOMAIN_BOARD))
                .consistOf(TargetIsDeletedValidator.of(postDomainModel.getIsDeleted(), StaticValue.DOMAIN_POST))
                .consistOf(ConstraintValidator.of(commentDomainModel, this.validator));

        postDomainModel.getBoard().getCircle()
                .filter(circleDomainModel -> !creatorDomainModel.getRole().equals(Role.ADMIN) && !creatorDomainModel.getRole().getValue().contains("PRESIDENT"))
                .ifPresent(
                        circleDomainModel -> {
                            CircleMemberDomainModel circleMemberDomainModel = this.circleMemberPort.findByUserIdAndCircleId(
                                    loginUserId,
                                    circleDomainModel.getId()
                            ).orElseThrow(
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

        return CommentResponseDto.from(
                this.commentPort.create(commentDomainModel, postDomainModel),
                creatorDomainModel,
                postDomainModel.getBoard(),
                this.childCommentPort.countByParentComment(commentDomainModel.getId())
        );
    }

    @Transactional(readOnly = true)
    public Page<CommentResponseDto> findAllComments(String loginUserId, String postId, Integer pageNum) {
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
                .consistOf(UserRoleIsNoneValidator.of(userDomainModel.getRole()))
                .consistOf(TargetIsDeletedValidator.of(postDomainModel.getBoard().getIsDeleted(), StaticValue.DOMAIN_BOARD))
                .consistOf(TargetIsDeletedValidator.of(postDomainModel.getIsDeleted(), StaticValue.DOMAIN_POST));

        postDomainModel.getBoard().getCircle()
                .filter(circleDomainModel -> !userDomainModel.getRole().equals(Role.ADMIN) && !userDomainModel.getRole().getValue().contains("PRESIDENT"))
                .ifPresent(
                        circleDomainModel -> {
                            CircleMemberDomainModel circleMemberDomainModel = this.circleMemberPort.findByUserIdAndCircleId(
                                    loginUserId,
                                    circleDomainModel.getId()
                            ).orElseThrow(
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

        return this.commentPort.findByPostId(postId, pageNum)
                .map(commentDomainModel ->
                        CommentResponseDto.from(
                                commentDomainModel,
                                userDomainModel,
                                postDomainModel.getBoard(),
                                this.childCommentPort.countByParentComment(commentDomainModel.getId())
                        )
                );
    }

    @Transactional
    public CommentResponseDto updateComment(
            String loginUserId,
            String commentId,
            CommentUpdateRequestDto commentUpdateRequestDto
    ) {
        ValidatorBucket validatorBucket = ValidatorBucket.of();

        UserDomainModel requestUser = this.userPort.findById(loginUserId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "로그인된 사용자를 찾을 수 없습니다."
                )
        );

        CommentDomainModel commentDomainModel = this.commentPort.findById(commentId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "수정할 댓글을 찾을 수 없습니다."
                )
        );

        PostDomainModel postDomainModel = this.postPort.findPostById(commentDomainModel.getPostId()).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "게시글을 찾을 수 없습니다."
                )
        );

        commentDomainModel.update(
                commentUpdateRequestDto.getContent()
        );

        validatorBucket
                .consistOf(UserStateValidator.of(requestUser.getState()))
                .consistOf(UserRoleIsNoneValidator.of(requestUser.getRole()))
                .consistOf(TargetIsDeletedValidator.of(postDomainModel.getBoard().getIsDeleted(), StaticValue.DOMAIN_BOARD))
                .consistOf(TargetIsDeletedValidator.of(postDomainModel.getIsDeleted(), StaticValue.DOMAIN_POST))
                .consistOf(TargetIsDeletedValidator.of(commentDomainModel.getIsDeleted(), StaticValue.DOMAIN_COMMENT))
                .consistOf(ConstraintValidator.of(commentDomainModel, this.validator))
                .consistOf(ContentsAdminValidator.of(
                        requestUser.getRole(),
                        loginUserId,
                        commentDomainModel.getWriter().getId(),
                        List.of()
                ));

        postDomainModel.getBoard().getCircle()
                .filter(circleDomainModel -> !requestUser.getRole().equals(Role.ADMIN) && !requestUser.getRole().getValue().contains("PRESIDENT"))
                .ifPresent(
                        circleDomainModel -> {
                            CircleMemberDomainModel circleMemberDomainModel = this.circleMemberPort.findByUserIdAndCircleId(
                                    loginUserId,
                                    circleDomainModel.getId()
                            ).orElseThrow(
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

        return CommentResponseDto.from(
                this.commentPort.update(commentId, commentDomainModel).orElseThrow(
                        () -> new InternalServerException(
                                ErrorCode.INTERNAL_SERVER,
                                "Comment id checked, but exception occurred"
                        )
                ),
                requestUser,
                postDomainModel.getBoard(),
                this.childCommentPort.countByParentComment(commentId)
        );
    }

    @Transactional
    public CommentResponseDto deleteComment(String loginUserId, String commentId) {
        ValidatorBucket validatorBucket = ValidatorBucket.of();

        UserDomainModel deleterDomainModel = this.userPort.findById(loginUserId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "로그인된 사용자를 찾을 수 없습니다."
                )
        );

        CommentDomainModel commentDomainModel = this.commentPort.findById(commentId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "삭제할 댓글을 찾을 수 없습니다."
                )
        );

        PostDomainModel postDomainModel = this.postPort.findPostById(commentDomainModel.getPostId()).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "게시글을 찾을 수 없습니다."
                )
        );

        validatorBucket
                .consistOf(UserStateValidator.of(deleterDomainModel.getState()))
                .consistOf(UserRoleIsNoneValidator.of(deleterDomainModel.getRole()))
                .consistOf(TargetIsDeletedValidator.of(commentDomainModel.getIsDeleted(), StaticValue.DOMAIN_COMMENT));

        postDomainModel.getBoard().getCircle()
                .filter(circleDomainModel -> !deleterDomainModel.getRole().equals(Role.ADMIN) && !deleterDomainModel.getRole().getValue().contains("PRESIDENT"))
                .ifPresentOrElse(
                        circleDomainModel -> {
                            CircleMemberDomainModel circleMemberDomainModel = this.circleMemberPort.findByUserIdAndCircleId(
                                    loginUserId,
                                    circleDomainModel.getId()
                            ).orElseThrow(
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
                                            loginUserId,
                                            commentDomainModel.getWriter().getId(),
                                            List.of(Role.LEADER_CIRCLE)
                                    ));

                            if (deleterDomainModel.getRole().getValue().contains("LEADER_CIRCLE") && !commentDomainModel.getWriter().getId().equals(loginUserId)) {
                                validatorBucket
                                        .consistOf(UserEqualValidator.of(
                                                circleDomainModel.getLeader().map(UserDomainModel::getId).orElseThrow(
                                                        () -> new InternalServerException(
                                                                ErrorCode.INTERNAL_SERVER,
                                                                "The board has circle without circle leader"
                                                        )
                                                ),
                                                loginUserId
                                        ));
                            }
                        },
                        () -> validatorBucket
                                .consistOf(ContentsAdminValidator.of(
                                        deleterDomainModel.getRole(),
                                        loginUserId,
                                        commentDomainModel.getWriter().getId(),
                                        List.of()
                                ))
                );

        validatorBucket
                .validate();

        return CommentResponseDto.from(
                this.commentPort.delete(commentId).orElseThrow(
                        () -> new InternalServerException(
                                ErrorCode.INTERNAL_SERVER,
                                "Comment id checked, but exception occurred"
                        )
                ),
                deleterDomainModel,
                postDomainModel.getBoard(),
                this.childCommentPort.countByParentComment(commentId)
        );
    }
}
