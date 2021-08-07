package net.causw.application.spi;

import net.causw.application.dto.CommentDetailDto;

import java.util.Optional;

public interface CommentPort {
    Optional<CommentDetailDto> findById(String id);
}
