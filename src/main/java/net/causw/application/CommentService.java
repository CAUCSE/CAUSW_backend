package net.causw.application;

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
import net.causw.domain.model.CommentDomainModel;
import net.causw.domain.model.PostDomainModel;
import net.causw.domain.model.Role;
import net.causw.domain.model.StaticValue;
import net.causw.domain.model.UserDomainModel;
import net.causw.domain.validation.ConstraintValidator;
import net.causw.domain.validation.ContentsAdminValidator;
import net.causw.domain.validation.TargetIsDeletedValidator;
import net.causw.domain.validation.UserRoleIsNoneValidator;
import net.causw.domain.validation.UserStateValidator;
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
    private final ChildCommentPort childCommentPort;
    private final Validator validator;

    public CommentService(
            CommentPort commentPort,
            UserPort userPort,
            PostPort postPort,
            CircleMemberPort circleMemberPort,
            ChildCommentPort childCommentPort,
            Validator validator
    ) {
        this.commentPort = commentPort;
        this.userPort = userPort;
        this.postPort = postPort;
        this.circleMemberPort = circleMemberPort;
        this.childCommentPort = childCommentPort;
        this.validator = validator;
    }

    @Transactional
    public CommentResponseDto create(String creatorId, CommentCreateRequestDto commentCreateDto) {
        UserDomainModel creatorDomainModel = this.userPort.findById(creatorId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "로그인된 사용자를 찾을 수 없습니다."
                )
        );

        PostDomainModel postDomainModel = this.postPort.findById(commentCreateDto.getPostId()).orElseThrow(
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

        ValidatorBucket.of()
                .consistOf(UserStateValidator.of(creatorDomainModel.getState()))
                .consistOf(UserRoleIsNoneValidator.of(creatorDomainModel.getRole()))
                .consistOf(TargetIsDeletedValidator.of(postDomainModel.getBoard().getIsDeleted(), StaticValue.DOMAIN_BOARD))
                .consistOf(TargetIsDeletedValidator.of(postDomainModel.getIsDeleted(), StaticValue.DOMAIN_POST))
                .consistOf(ConstraintValidator.of(commentDomainModel, this.validator))
                .validate();

        CircleMemberAuthentication
                .authenticate(this.circleMemberPort, creatorDomainModel, postDomainModel.getBoard().getCircle());

        return CommentResponseDto.from(
                this.commentPort.create(commentDomainModel, postDomainModel),
                creatorDomainModel,
                postDomainModel.getBoard(),
                this.childCommentPort.countByParentComment(commentDomainModel.getId())
        );
    }

    @Transactional(readOnly = true)
    public Page<CommentResponseDto> findAll(String userId, String postId, Integer pageNum) {
        UserDomainModel userDomainModel = this.userPort.findById(userId).orElseThrow(
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

        ValidatorBucket.of()
                .consistOf(UserStateValidator.of(userDomainModel.getState()))
                .consistOf(UserRoleIsNoneValidator.of(userDomainModel.getRole()))
                .consistOf(TargetIsDeletedValidator.of(postDomainModel.getBoard().getIsDeleted(), StaticValue.DOMAIN_BOARD))
                .consistOf(TargetIsDeletedValidator.of(postDomainModel.getIsDeleted(), StaticValue.DOMAIN_POST))
                .validate();

        CircleMemberAuthentication
                .authenticate(this.circleMemberPort, userDomainModel, postDomainModel.getBoard().getCircle());

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
    public CommentResponseDto update(
            String requestUserId,
            String commentId,
            CommentUpdateRequestDto commentUpdateRequestDto
    ) {
        UserDomainModel requestUser = this.userPort.findById(requestUserId).orElseThrow(
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

        PostDomainModel postDomainModel = this.postPort.findById(commentDomainModel.getPostId()).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "게시글을 찾을 수 없습니다."
                )
        );

        commentDomainModel.update(
                commentUpdateRequestDto.getContent()
        );

        ValidatorBucket.of()
                .consistOf(UserStateValidator.of(requestUser.getState()))
                .consistOf(UserRoleIsNoneValidator.of(requestUser.getRole()))
                .consistOf(TargetIsDeletedValidator.of(postDomainModel.getBoard().getIsDeleted(), StaticValue.DOMAIN_BOARD))
                .consistOf(TargetIsDeletedValidator.of(postDomainModel.getIsDeleted(), StaticValue.DOMAIN_POST))
                .consistOf(TargetIsDeletedValidator.of(commentDomainModel.getIsDeleted(), StaticValue.DOMAIN_COMMENT))
                .consistOf(ConstraintValidator.of(commentDomainModel, this.validator))
                .consistOf(ContentsAdminValidator.of(
                        requestUser.getRole(),
                        requestUserId,
                        commentDomainModel.getWriter().getId(),
                        List.of()
                ))
                .validate();

        CircleMemberAuthentication
                .authenticate(this.circleMemberPort, requestUser, postDomainModel.getBoard().getCircle());

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
    public CommentResponseDto delete(String deleterId, String commentId) {
        UserDomainModel deleterDomainModel = this.userPort.findById(deleterId).orElseThrow(
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

        PostDomainModel postDomainModel = this.postPort.findById(commentDomainModel.getPostId()).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "게시글을 찾을 수 없습니다."
                )
        );

        ValidatorBucket.of()
                .consistOf(UserStateValidator.of(deleterDomainModel.getState()))
                .consistOf(UserRoleIsNoneValidator.of(deleterDomainModel.getRole()))
                .consistOf(TargetIsDeletedValidator.of(commentDomainModel.getIsDeleted(), StaticValue.DOMAIN_COMMENT))
                .consistOf(ContentsAdminValidator.of(
                        deleterDomainModel.getRole(),
                        deleterId,
                        commentDomainModel.getWriter().getId(),
                        postDomainModel.getBoard().getCircle()
                                .map(circle -> List.of(Role.LEADER_CIRCLE))
                                .orElse(List.of(Role.PRESIDENT))
                ))
                .validate();

        CircleMemberAuthentication
                .authenticateLeader(this.circleMemberPort, deleterDomainModel, postDomainModel.getBoard().getCircle());

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
