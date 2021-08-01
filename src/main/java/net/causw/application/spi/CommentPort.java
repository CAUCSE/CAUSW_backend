package net.causw.application.spi;

import net.causw.application.dto.CommentDetailDto;

public interface CommentPort {
    CommentDetailDto findById(String id);
}
