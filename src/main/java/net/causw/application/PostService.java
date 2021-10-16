package net.causw.application;

import net.causw.application.dto.CommentResponseDto;
import net.causw.application.dto.PostAllResponseDto;
import net.causw.application.dto.PostCreateRequestDto;
import net.causw.application.dto.PostResponseDto;
import net.causw.application.spi.BoardPort;
import net.causw.application.spi.CircleMemberPort;
import net.causw.application.spi.CommentPort;
import net.causw.application.spi.PostPort;
import net.causw.application.spi.UserPort;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.exceptions.UnauthorizedException;
import net.causw.domain.model.BoardDomainModel;
import net.causw.domain.model.CircleMemberDomainModel;
import net.causw.domain.model.CircleMemberStatus;
import net.causw.domain.model.PostDomainModel;
import net.causw.domain.model.Role;
import net.causw.domain.model.UserDomainModel;
import net.causw.domain.validation.CircleMemberStatusValidator;
import net.causw.domain.validation.ConstraintValidator;
import net.causw.domain.validation.TargetIsDeletedValidator;
import net.causw.domain.validation.UserRoleValidator;
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
    public PostResponseDto findById(String userId, String id) {
        ValidatorBucket validatorBucket = ValidatorBucket.of();

        PostDomainModel postDomainModel = this.postPort.findById(id).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "Invalid post id"
                )
        );

        UserDomainModel userDomainModel = this.userPort.findById(userId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "Invalid user id"
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

        // TODO : GHJANG : Pagination 고려
        // TODO : GHJANG : PostResponseDto에 댓글 갯수 포함 필요
        return PostResponseDto.from(
                postDomainModel,
                userDomainModel,
                this.commentPort.findByPostId(id)
                        .stream()
                        .map(CommentResponseDto::from)
                        .collect(Collectors.toList())
        );
    }

    @Transactional(readOnly = true)
    public List<PostAllResponseDto> findAll(String userId, String boardId) {
        ValidatorBucket validatorBucket = ValidatorBucket.of();

        BoardDomainModel boardDomainModel = this.boardPort.findById(boardId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "Invalid board id"
                )
        );

        boardDomainModel.getCircle().ifPresent(
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
                .consistOf(TargetIsDeletedValidator.of(boardDomainModel.getIsDeleted()))
                .validate();

        // TODO: Pagination
        return this.postPort.findAll(boardId)
                .stream()
                .map(postDomainModel -> PostAllResponseDto.from(
                        postDomainModel,
                        this.commentPort.countByPostId(postDomainModel.getId())
                ))
                .collect(Collectors.toList());
    }

    @Transactional
    public PostResponseDto create(String creatorId, PostCreateRequestDto postCreateRequestDto) {
        ValidatorBucket validatorBucket = ValidatorBucket.of();

        UserDomainModel creatorDomainModel = this.userPort.findById(creatorId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "Invalid request user id"
                )
        );

        BoardDomainModel boardDomainModel = this.boardPort.findById(postCreateRequestDto.getBoardId()).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "Invalid board id"
                )
        );

        PostDomainModel postDomainModel = PostDomainModel.of(
                postCreateRequestDto.getTitle(),
                postCreateRequestDto.getContent(),
                creatorDomainModel,
                boardDomainModel
        );

        validatorBucket
                .consistOf(TargetIsDeletedValidator.of(boardDomainModel.getIsDeleted()))
                .consistOf(UserRoleValidator.of(
                        creatorDomainModel.getRole(),
                        boardDomainModel.getCreateRoleList()
                                .stream()
                                .map(Role::of)
                                .collect(Collectors.toList())
                ));

        boardDomainModel.getCircle().ifPresent(
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
                .consistOf(ConstraintValidator.of(postDomainModel, this.validator))
                .validate();

        return PostResponseDto.from(this.postPort.create(postDomainModel), creatorDomainModel);
    }
}
