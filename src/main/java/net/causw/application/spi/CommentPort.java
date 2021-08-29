package net.causw.application.spi;

import net.causw.application.dto.CommentCreateRequestDto;
import net.causw.application.dto.CommentFullDto;
import net.causw.application.dto.CommentResponseDto;
import net.causw.application.dto.PostDetailDto;
import net.causw.application.dto.UserFullDto;

import java.util.Optional;

public interface CommentPort {
    Optional<CommentFullDto> findById(String id);

    CommentResponseDto create(
            CommentCreateRequestDto commentCreateDto,
            UserFullDto writer,
            PostDetailDto post
    );

    CommentResponseDto create(
            CommentCreateRequestDto commentCreateDto,
            UserFullDto writer,
            PostDetailDto post,
            CommentFullDto parentComment
    );
}
