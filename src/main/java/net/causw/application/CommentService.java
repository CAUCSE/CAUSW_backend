package net.causw.application;

import net.causw.application.dto.CommentCreateRequestDto;
import net.causw.application.dto.CommentResponseDto;
import net.causw.application.dto.CommentUpdateRequestDto;
import net.causw.application.spi.CircleMemberPort;
import net.causw.application.spi.CommentPort;
import net.causw.application.spi.PostPort;
import net.causw.application.spi.UserPort;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.exceptions.InternalServerException;
import net.causw.domain.exceptions.UnauthorizedException;
import net.causw.domain.model.CircleMemberDomainModel;
import net.causw.domain.model.CircleMemberStatus;
import net.causw.domain.model.CommentDomainModel;
import net.causw.domain.model.PostDomainModel;
import net.causw.domain.model.UserDomainModel;
import net.causw.domain.validation.CircleMemberStatusValidator;
import net.causw.domain.validation.ConstraintValidator;
import net.causw.domain.validation.TargetIsDeletedValidator;
import net.causw.domain.validation.ValidatorBucket;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.Validator;
import java.util.List;

@Service
public class CommentService {
    private final CommentPort commentPort;
    private final UserPort userPort;
    private final PostPort postPort;
    private final CircleMemberPort circleMemberPort;
    private final Validator validator;

    public CommentService(
            CommentPort commentPort,
            UserPort userPort,
            PostPort postPort,
            CircleMemberPort circleMemberPort,
            Validator validator
    ) {
        this.commentPort = commentPort;
        this.userPort = userPort;
        this.postPort = postPort;
        this.circleMemberPort = circleMemberPort;
        this.validator = validator;
    }

    @Transactional
    public CommentResponseDto create(String creatorId, CommentCreateRequestDto commentCreateDto) {
        ValidatorBucket validatorBucket = ValidatorBucket.of();

        UserDomainModel creatorDomainModel = this.userPort.findById(creatorId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "Invalid writer id"
                )
        );

        PostDomainModel postDomainModel = this.postPort.findById(commentCreateDto.getPostId()).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "Invalid post id"
                )
        );

        // Parent comment deleted case is allowed
        CommentDomainModel parentCommentDomainModel = commentCreateDto.getParentCommentId().map(
                parentCommentId -> this.commentPort.findById(parentCommentId).orElseThrow(
                        () -> new BadRequestException(
                                ErrorCode.ROW_DOES_NOT_EXIST,
                                "Invalid parent comment id"
                        )
                )
        ).orElse(null);

        CommentDomainModel commentDomainModel = CommentDomainModel.of(
                commentCreateDto.getContent(),
                creatorDomainModel,
                postDomainModel.getId(),
                parentCommentDomainModel
        );

        postDomainModel.getBoard().getCircle().ifPresent(
                circleDomainModel -> {
                    CircleMemberDomainModel circleMemberDomainModel = this.circleMemberPort.findByUserIdAndCircleId(creatorId, circleDomainModel.getId()).orElseThrow(
                            () -> new UnauthorizedException(
                                    ErrorCode.NOT_MEMBER,
                                    "The user is not a member of circle"
                            )
                    );

                    validatorBucket
                            .consistOf(TargetIsDeletedValidator.of(circleDomainModel.getIsDeleted()))
                            .consistOf(CircleMemberStatusValidator.of(
                                    circleMemberDomainModel.getStatus(),
                                    List.of(CircleMemberStatus.MEMBER)
                            ));
                }
        );

        validatorBucket
                .consistOf(TargetIsDeletedValidator.of(postDomainModel.getIsDeleted()))
                .consistOf(ConstraintValidator.of(commentDomainModel, this.validator))
                .validate();

        return CommentResponseDto.from(
                this.commentPort.create(commentDomainModel, postDomainModel),
                creatorDomainModel,
                postDomainModel.getBoard()
                );
    }

    @Transactional(readOnly = true)
    public Page<CommentResponseDto> findAll(String userId, String postId, Integer pageNum) {
        ValidatorBucket validatorBucket = ValidatorBucket.of();

        UserDomainModel userDomainModel = this.userPort.findById(userId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "Invalid writer id"
                )
        );

        PostDomainModel postDomainModel = this.postPort.findById(postId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "Invalid post id"
                )
        );

        validatorBucket
                .consistOf(TargetIsDeletedValidator.of(postDomainModel.getIsDeleted()));

        postDomainModel.getBoard().getCircle().ifPresent(
                circleDomainModel -> {
                    CircleMemberDomainModel circleMemberDomainModel = this.circleMemberPort.findByUserIdAndCircleId(userId, circleDomainModel.getId()).orElseThrow(
                            () -> new UnauthorizedException(
                                    ErrorCode.NOT_MEMBER,
                                    "The user is not a member of circle"
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

        return this.commentPort.findByPostId(postId, pageNum)
                .map(commentDomainModel ->
                        CommentResponseDto.from(commentDomainModel, userDomainModel, postDomainModel.getBoard())
                );
    }

    @Transactional
    public CommentResponseDto update(
            String requestUserId,
            String commentId,
            CommentUpdateRequestDto commentUpdateRequestDto
    ) {
        ValidatorBucket validatorBucket = ValidatorBucket.of();

        UserDomainModel requestUser = this.userPort.findById(requestUserId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "Invalid request user id"
                )
        );

        CommentDomainModel commentDomainModel = this.commentPort.findById(commentId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "Invalid comment id"
                )
        );

        PostDomainModel postDomainModel = this.postPort.findById(commentDomainModel.getPostId()).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "Invalid post id"
                )
        );

        validatorBucket
                .consistOf(TargetIsDeletedValidator.of(commentDomainModel.getIsDeleted()))
                .consistOf(ContentsAdminValidator.of(
                        requestUser.getRole(),
                        requestUserId,
                        commentDomainModel.getWriter().getId(),
                        List.of()
                ));



        postDomainModel.getBoard().getCircle().ifPresent(
                circleDomainModel -> {
                    CircleMemberDomainModel circleMemberDomainModel = this.circleMemberPort.findByUserIdAndCircleId(requestUserId, circleDomainModel.getId()).orElseThrow(
                            () -> new UnauthorizedException(
                                    ErrorCode.NOT_MEMBER,
                                    "The user is not a member of circle"
                            )
                    );

                    validatorBucket
                            .consistOf(TargetIsDeletedValidator.of(circleDomainModel.getIsDeleted()))
                            .consistOf(CircleMemberStatusValidator.of(
                                    circleMemberDomainModel.getStatus(),
                                    List.of(CircleMemberStatus.MEMBER)
                            ));
                }
        );

        commentDomainModel = CommentDomainModel.of(
                commentDomainModel.getId(),
                commentUpdateRequestDto.getContent(),
                commentDomainModel.getIsDeleted(),
                commentDomainModel.getCreatedAt(),
                commentDomainModel.getUpdatedAt(),
                commentDomainModel.getWriter(),
                commentDomainModel.getPostId()
        );

        validatorBucket
                .consistOf(ConstraintValidator.of(commentDomainModel, this.validator))
                .validate();

        return CommentResponseDto.from(
                this.commentPort.update(commentId, commentDomainModel).orElseThrow(
                        () -> new InternalServerException(
                                ErrorCode.INTERNAL_SERVER,
                                "Comment id checked, but exception occurred"
                        )
                )
        );
    }
}
