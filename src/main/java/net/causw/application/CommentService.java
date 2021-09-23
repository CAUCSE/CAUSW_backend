package net.causw.application;

import net.causw.application.dto.CommentCreateRequestDto;
import net.causw.application.dto.CommentResponseDto;
import net.causw.application.spi.CommentPort;
import net.causw.application.spi.PostPort;
import net.causw.application.spi.UserPort;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.model.CommentDomainModel;
import net.causw.domain.model.PostDomainModel;
import net.causw.domain.model.UserDomainModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CommentService {
    private final CommentPort commentPort;
    private final UserPort userPort;
    private final PostPort postPort;

    public CommentService(
            CommentPort commentPort,
            UserPort userPort,
            PostPort postPort
    ) {
        this.commentPort = commentPort;
        this.userPort = userPort;
        this.postPort = postPort;
    }

    @Transactional(readOnly = true)
    public CommentResponseDto findById(String id) {
        return CommentResponseDto.from(this.commentPort.findById(id).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "Invalid comment id"
                )
        ));
    }

    @Transactional
    public CommentResponseDto create(String creatorId, CommentCreateRequestDto commentCreateDto) {
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

        // TODO : GHJANG : Creator user have at least read role for this board

        CommentDomainModel parentCommentDomainModel = commentCreateDto.getParentCommentId().map(
                parentCommentId -> {
                    CommentDomainModel parentComment = this.commentPort.findById(parentCommentId).orElseThrow(
                            () -> new BadRequestException(
                                    ErrorCode.ROW_DOES_NOT_EXIST,
                                    "Invalid parent comment id"
                            )
                    );
                    // TODO : GHJANG : Impl Validation

                    return parentComment;
                }
        ).orElseGet(
                () -> {
                    // TODO : GHJANG : Impl Validation
                    return null;
                }
        );

        CommentDomainModel commentDomainModel = CommentDomainModel.of(
                commentCreateDto.getContent(),
                creatorDomainModel,
                postDomainModel.getId(),
                parentCommentDomainModel
        );

        return CommentResponseDto.from(this.commentPort.create(commentDomainModel, postDomainModel));
    }
}
