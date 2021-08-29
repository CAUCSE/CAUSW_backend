package net.causw.application;

import net.causw.application.dto.CommentCreateRequestDto;
import net.causw.application.dto.CommentFullDto;
import net.causw.application.dto.CommentResponseDto;
import net.causw.application.dto.PostDetailDto;
import net.causw.application.dto.UserFullDto;
import net.causw.application.spi.CommentPort;
import net.causw.application.spi.PostPort;
import net.causw.application.spi.UserPort;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
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
    public CommentFullDto findById(String id) {
        return this.commentPort.findById(id).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "Invalid comment id"
                )
        );
    }

    @Transactional
    public CommentResponseDto create(CommentCreateRequestDto commentCreateDto) {
        UserFullDto userDto = this.userPort.findById(commentCreateDto.getWriterId()).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "Invalid writer id"
                )
        );

        PostDetailDto postDto = this.postPort.findById(commentCreateDto.getPostId());

        if (commentCreateDto.getParentCommentId().isEmpty()) {
            return this.commentPort.create(
                    commentCreateDto,
                    userDto,
                    postDto
            );
        }

        CommentFullDto parentCommentDto = this.commentPort.findById(commentCreateDto.getParentCommentId()).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "Invalid parent comment id"
                )
        );

        return this.commentPort.create(
                commentCreateDto,
                userDto,
                postDto,
                parentCommentDto
        );
    }
}
